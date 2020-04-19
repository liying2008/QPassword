package cc.duduhuo.qpassword.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.EditText
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.FileListAdapter
import cc.duduhuo.qpassword.bean.Export
import cc.duduhuo.qpassword.bean.ImportFile
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnPasswordsChangeListener
import cc.duduhuo.qpassword.util.*
import cc.duduhuo.qpassword.widget.FileItemDecoration
import com.alibaba.fastjson.JSON
import kotlinx.android.synthetic.main.activity_import.*
import java.io.File


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/11 22:56
 * Description: 导入密码 Activity
 * Remarks:
 * =======================================================
 */
class ImportActivity : BaseActivity(), FileListAdapter.OnFileClickListener, OnPasswordsChangeListener {
    private var mMainBinder: MainBinder? = null
    private var mAdapter: FileListAdapter? = null
    private var mProgressDialog: ProgressDialog? = null

    companion object {
        const val RESULT_CODE_IMPORT = 0x0010
        @SuppressLint("InlinedApi")
        private const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private const val REQUEST_PERMISSION = 0x0000
        fun getIntent(context: Context): Intent {
            return Intent(context, ImportActivity::class.java)
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            if (mMainBinder != null) {
                mMainBinder!!.unregisterOnPasswordsChangeListener(this@ImportActivity)
                mMainBinder = null
            }
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMainBinder != null) {
                // 注册密码变化监听器
                mMainBinder!!.registerOnPasswordsChangeListener(this@ImportActivity)
                initViews()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.title_import_password)
        tv_import_msg.text = getString(R.string.import_password_tip, Config.EXPORT_FILE_EXTENSION, Config.WORK_DIR + "/" + Config.EXPORT_DIR)

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    private fun initViews() {
        // 检查权限
        if (Build.VERSION.SDK_INT >= 23) {
            if (isPermissionGranted(PERMISSION)) {
                initData()
            } else {
                // 申请权限
                if (shouldShowPermissionRationale(PERMISSION)) {
                    Snackbar.make(main_layout, R.string.permission_read_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok) {
                            requestPermission(PERMISSION, REQUEST_PERMISSION)
                        }
                        .show()
                } else {
                    requestPermission(PERMISSION, REQUEST_PERMISSION)
                }
            }
        } else {
            initData()
        }

    }

    private fun initData(refresh: Boolean = false) {
        val sdPath = Environment.getExternalStorageDirectory().absolutePath
        val exportPath = sdPath + File.separator + Config.WORK_DIR + File.separator + Config.EXPORT_DIR
        val exportPathFile = File(exportPath)
        // 检查 exportPath 是否存在，不存在则创建
        if (!exportPathFile.exists()) {
            exportPathFile.mkdirs()
        }
        val files = getFiles(exportPathFile)
        if (files.isEmpty()) {
            AppToast.showToast(getString(R.string.search_files_fail, Config.EXPORT_FILE_EXTENSION, Config.WORK_DIR + "/" + Config.EXPORT_DIR))
        }
        if (refresh) {
            mAdapter?.refresh(files)
        } else {
            mAdapter = FileListAdapter(this, files)
            // 设置文件名点击监听
            mAdapter!!.setOnFileClickListener(this)
            rv_file.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            rv_file.addItemDecoration(FileItemDecoration(this))
            rv_file.adapter = mAdapter
        }
    }


    /**
     * 遍历目标文件下的文件，找到扩展名相符的文件
     * @param dirs 需要遍历的所有文件夹
     */
    private fun getFiles(vararg dirs: File): MutableList<ImportFile> {
        val fileList = mutableListOf<ImportFile>()
        // 文件扩展名（不包括 .）
        val extension = Config.EXPORT_FILE_EXTENSION.substring(1)
        for (dir in dirs) {
            dir.walk().maxDepth(1).filter { it.isFile }.filter { it.extension == extension }
                .forEach { fileList.add(ImportFile(it.name, it.absolutePath, it.length())) }
        }
        return fileList
    }

    /**
     * 点击文件名
     */
    override fun onFileClick(absolutePath: String) {
        var export: Export? = null
        showProgressDialog(getString(R.string.parsing_password_file))

        val readFileTask = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg params: Void?): Int {
                try {
                    export = JSON.parseObject(File(absolutePath).readText(), Export::class.java)
                    return 0
                } catch (e: Exception) {
                    e.printStackTrace()
                    return -1
                }
            }

            override fun onPostExecute(result: Int?) {
                super.onPostExecute(result)
                dismissProgressDialog()

                if (result == -1) {
                    AppToast.showToast(R.string.import_file_fail)
                } else {
                    if (export != null) {
                        if (export!!.isEncrypted) {
                            val builder = AlertDialog.Builder(this@ImportActivity)
                            val view = layoutInflater.inflate(R.layout.dialog_enter_key, null, false)
                            builder.setView(view)
                            val etKey = view.findViewById<EditText>(R.id.et_enter_key)
                            builder.setTitle(R.string.import_enter_key)
                            builder.setPositiveButton(R.string.ok, null)
                            builder.setNegativeButton(R.string.cancel, null)
                            val dialog = builder.create()
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                                dialog.dismiss()
                            }
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                val oriKey = etKey.text.toString()
                                if (oriKey.isEmpty()) {
                                    AppToast.showToast(R.string.import_key_can_not_be_empty)
                                    return@setOnClickListener
                                }
                                if (oriKey.sha1Hex() == export!!.key) {
                                    importPassword(export!!.passwords, oriKey)
                                    dialog.dismiss()
                                    return@setOnClickListener
                                } else {
                                    AppToast.showToast(R.string.import_key_wrong)
                                    return@setOnClickListener
                                }
                            }
                        } else {
                            importPassword(export!!.passwords)
                        }
                    }
                }
            }
        }
        registerAsyncTask(ImportActivity::class.java, readFileTask)
        readFileTask.execute()
    }

    /**
     * 长按文件名
     */
    override fun onFileLongClick(position: Int, absolutePath: String): Boolean {
        val fileName = absolutePath.split(File.separator).last()
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.delete_file)
        builder.setMessage(getString(R.string.delete_file_message, fileName))
        builder.setNegativeButton(R.string.delete) { _, _ ->
            val ok = File(absolutePath).delete()
            if (ok) {
                // 刷新 RecyclerView
                mAdapter!!.removeItem(position)
            } else {
                // 删除文件失败
                Snackbar.make(rv_file, R.string.delete_fail, Snackbar.LENGTH_LONG)
                    .setAction(R.string.refresh_file_list) {
                        initData(refresh = true)
                    }.show()
            }
        }
        builder.setPositiveButton(R.string.cancel) { _, _ ->
            // no op
        }
        builder.create().show()
        return true
    }

    /**
     * 导入密码
     * @param passwords 密码 List
     * @param oriKey 加密密钥
     */
    private fun importPassword(passwords: List<Password>, oriKey: String? = null) {
        val passwordCount = passwords.size
        if (passwordCount == 0) {
            AppToast.showToast(R.string.import_zero)
            return
        }
        showProgressDialog(getString(R.string.importing_passwords, passwordCount))
        if (oriKey != null) {
            // 有密钥，先解密
            for (i in 0 until passwordCount) {
                passwords[i].password = passwords[i].password.aesDecrypt(oriKey)
            }
        }
        // 导入密码
        mMainBinder?.insertPasswords(passwords)
    }

    override fun onNewPasswords(passwords: List<Password>) {
        dismissProgressDialog()

        AppToast.showToast(getString(R.string.imported, passwords.size))
        setResult(RESULT_CODE_IMPORT)
        finish()
    }

    override fun onDeletePasswords(passwords: List<Password>) {
        // no op
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
                initData()
            } else {
                Snackbar.make(main_layout, R.string.read_permission_not_granted, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * 显示 ProgressDialog
     * @param message 显示的信息
     */
    private fun showProgressDialog(message: CharSequence) {
        if (isFinishing) {
            return
        }
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setCancelable(false)
        }
        mProgressDialog!!.setMessage(message)
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
        unregisterAsyncTask(ImportActivity::class.java)
        unbindService(mServiceConnection)
        dismissProgressDialog()
        super.onDestroy()
    }

}
