package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService

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
                //todo
            }
        }
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
