package cc.duduhuo.qpassword.ui.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
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
import cc.duduhuo.qpassword.util.*
import com.alibaba.fastjson.JSON
import kotlinx.android.synthetic.main.activity_export.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExportActivity : BaseActivity() {
    private var mMainBinder: MainBinder? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mExportType = EXPORT_NO_ENCRYPTED

    companion object {
        /** 以非加密方式导出 */
        private const val EXPORT_NO_ENCRYPTED = 0
        /** 以加密方式导出 */
        private const val EXPORT_ENCRYPTED = 1
        private const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private const val REQUEST_PERMISSION = 0x0000

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

        rg_export.setOnCheckedChangeListener { _, checkedId ->
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

        btn_export.setOnClickListener { view ->
            // 检查权限
            if (Build.VERSION.SDK_INT >= 23) {
                if (isPermissionGranted(PERMISSION)) {
                    startExport()
                } else {
                    // 申请权限
                    if (shouldShowPermissionRationale(PERMISSION)) {
                        Snackbar.make(view, R.string.permission_write_rationale,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.ok) {
                                requestPermission(PERMISSION, REQUEST_PERMISSION)
                            }.show()
                    } else {
                        requestPermission(PERMISSION, REQUEST_PERMISSION)
                    }
                }
            } else {
                startExport()
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
     * 开始导出
     */
    private fun startExport() {
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
                return
            }
            export.isEncrypted = true
            export.key = key.sha1Hex()
            exportPassword(export, key)
        }
    }

    /**
     * 导出密码
     * @param export 用于导出的数据实体
     * @param oriKey 加密种子
     */
    private fun exportPassword(export: Export, oriKey: String? = null) {
        AppToast.showToast(R.string.fetching_all_password)
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
                        writeFile(path, export)
                    } else {
                        dir.mkdirs()
                    }
                } else {
                    // 不存在存储卡
                    writeFile(null, export)
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
     */
    private fun writeFile(dir: String?, export: Export) {
        showProgressDialog()

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
        dismissProgressDialog()
        // 显示导出成功提醒对话框
        val builder = AlertDialog.Builder(this@ExportActivity)
        builder.setMessage(getString(R.string.export_success, fileName))
        builder.setPositiveButton(R.string.i_known) { _, _ ->
            finish()
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                startExport()
            } else {
                Snackbar.make(main_layout, R.string.write_permission_not_granted, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * 显示 ProgressDialog
     */
    private fun showProgressDialog() {
        if (isFinishing) {
            return
        }
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setCancelable(false)
        }
        mProgressDialog!!.setMessage(getString(R.string.exporting))
        mProgressDialog!!.show()
    }

    /**
     * 取消 ProgressDialog
     */
    private fun dismissProgressDialog() {
        if (isFinishing) {
            return
        }
        if (mProgressDialog != null) {
            if (mProgressDialog!!.isShowing) {
                mProgressDialog!!.dismiss()
            }
            mProgressDialog = null
        }
    }

    override fun onDestroy() {
        unbindService(mServiceConnection)
        super.onDestroy()
    }
}
