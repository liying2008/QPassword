package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.FileListAdapter
import cc.duduhuo.qpassword.bean.ImportFile
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.widget.FileItemDecoration
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
class ImportActivity : BaseActivity() {
    private var mMainBinder: MainBinder? = null
    private var mAdapter: FileListAdapter? = null

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ImportActivity::class.java)
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMainBinder != null) {
                initViews()
            }
        }
    }

    private fun initViews() {
        tv_import_msg.text = getString(R.string.import_password_tip, Config.EXPORT_FILE_EXTENSION, Config.WORK_DIR + "/" + Config.EXPORT_DIR)
        val sdPath = Environment.getExternalStorageDirectory().absolutePath
        val exportPath = sdPath + File.separator + Config.WORK_DIR + File.separator + Config.EXPORT_DIR
        val files = getFiles(File(exportPath), File(sdPath))
        mAdapter = FileListAdapter(this, files)
        rv_file.layoutManager = LinearLayoutManager(this)
        rv_file.addItemDecoration(FileItemDecoration(this))
        rv_file.adapter = mAdapter
    }

    /**
     * 遍历目标文件下的文件，找到扩展名相符的文件
     * @param dirs 需要遍历的所有文件夹
     */
    private fun getFiles(vararg dirs: File): MutableList<ImportFile> {
        val fileList = mutableListOf<ImportFile>()
        for (dir in dirs) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.name.endsWith(Config.EXPORT_FILE_EXTENSION)) {
                        fileList.add(ImportFile(file.name, file.absolutePath))
                    }
                }
            }
        }
        return fileList
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.title_import_password)

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }

}
