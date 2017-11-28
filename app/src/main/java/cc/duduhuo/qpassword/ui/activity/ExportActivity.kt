package cc.duduhuo.qpassword.ui.activity

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Export
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener
import cc.duduhuo.qpassword.util.PreferencesUtils
import cc.duduhuo.qpassword.util.aesEncrypt
import cc.duduhuo.qpassword.util.isExistSDCard
import cc.duduhuo.qpassword.util.sha1Hex
import com.alibaba.fastjson.JSON
import kotlinx.android.synthetic.main.activity_export.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExportActivity : BaseActivity() {
    private var mMainBinder: MainBinder? = null
    private var mExportType = EXPORT_NO_ENCRYPTED

    companion object {
        /** 以非加密方式导出 */
        const val EXPORT_NO_ENCRYPTED = 0
        /** 以加密方式导出 */
        const val EXPORT_ENCRYPTED = 1

        fun getIntent(context: Context): Intent {
            return Intent(context, ExportActivity::class.java)
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMainBinder != null) {
                initView()
            }
        }
    }

    private fun initView() {
        btn_export.isEnabled = true
        mExportType = PreferencesUtils.getInt(this, Config.LAST_EXPORT, EXPORT_NO_ENCRYPTED)
        if (mExportType == EXPORT_NO_ENCRYPTED) {
            rg_export.check(R.id.rb_no_encrypted)
            et_key.visibility = View.GONE
            tv_export_warning.visibility = View.GONE
        } else if (mExportType == EXPORT_ENCRYPTED) {
            rg_export.check(R.id.rb_encrypted)
            et_key.visibility = View.VISIBLE
            tv_export_warning.visibility = View.VISIBLE
        }

        rg_export.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_no_encrypted) {
                mExportType = EXPORT_NO_ENCRYPTED
                et_key.visibility = View.GONE
                tv_export_warning.visibility = View.GONE
            } else if (checkedId == R.id.rb_encrypted) {
                mExportType = EXPORT_ENCRYPTED
                et_key.visibility = View.VISIBLE
                tv_export_warning.visibility = View.VISIBLE
            }
        }

        btn_export.setOnClickListener {
            // 保存导出类型
            PreferencesUtils.putInt(this, Config.LAST_EXPORT, mExportType)
            val export = Export()
            if (mExportType == EXPORT_NO_ENCRYPTED) {
                export.isEncrypted = false
                exportPassword(export)
            } else if (mExportType == EXPORT_ENCRYPTED) {
                val key = et_key.text.toString().trim()
                if (key == "") {
                    AppToast.showToast(R.string.key_can_not_be_empty)
                    return@setOnClickListener
                }
                export.isEncrypted = true
                export.key = key.sha1Hex()
                exportPassword(export, key)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.title_export_password)

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    /**
     * 导出密码
     * @param export 用于导出的数据实体
     * @param oriKey 加密种子
     */
    private fun exportPassword(export: Export, oriKey: String? = null) {
        val progressDialog = ProgressDialog(this@ExportActivity)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getString(R.string.exporting))
        progressDialog.show()

        mMainBinder?.getPasswords(object : OnGetPasswordsListener {
            override fun onGetPasswords(groupName: String?, passwords: List<Password>) {
                if (export.isEncrypted) {
                    export.passwords = encrypt(passwords, oriKey!!)
                } else {
                    export.passwords = passwords
                }

                if (isExistSDCard()) {
                    // 存在存储卡
                    val path = Environment.getExternalStorageDirectory().absolutePath + File.separator +
                        Config.WORK_DIR + File.separator + Config.EXPORT_DIR
                    val dir = File(path)
                    if (dir.exists() && dir.isDirectory) {
                        writeFile(path, export, progressDialog)
                    } else {
                        dir.mkdirs()
                    }
                } else {
                    // 不存在存储卡
                    writeFile(null, export, progressDialog)
                }
            }

        })
    }

    /**
     * 加密 Password List
     * @param passwords
     * @param key
     */
    private fun encrypt(passwords: List<Password>, key: String): List<Password> {
        val size = passwords.size
        for (i in 0 until size) {
            passwords[i].password = passwords[i].password.aesEncrypt(key)
        }
        return passwords
    }

    /**
     * 将密码信息写入文件
     * @param dir 写入外部存储卡的目录
     * @param export
     * @param progressDialog
     */
    private fun writeFile(dir: String?, export: Export, progressDialog: ProgressDialog) {
        val jsonString = JSON.toJSONString(export)
        val fileName = SimpleDateFormat(Config.EXPORT_FILENAME_FORMAT, Locale.getDefault()).format(Date()) +
            Config.EXPORT_FILE_EXTENSION

        val file = if (dir == null) {
            File(fileName)
        } else {
            File(dir, fileName)
        }
        file.writeText(jsonString)
        // 关闭进度对话框
        progressDialog.dismiss()
        // 显示导出成功提醒对话框
        val builder = AlertDialog.Builder(this@ExportActivity)
        builder.setMessage(getString(R.string.export_success, fileName))
        builder.setPositiveButton(R.string.i_known, DialogInterface.OnClickListener { dialog, which ->
            finish()
        })
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    /**
     * 点击ActionBar返回图标回到上一个Activity
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        unbindService(mServiceConnection)
        super.onDestroy()
    }
}
