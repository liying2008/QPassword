package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.task.SplashLoadDataTask

class SplashActivity : BaseActivity(), SplashLoadDataTask.OnLoadKeyListener {
    private var mMainBinder: MainBinder? = null

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMainBinder != null) {
                val task = SplashLoadDataTask(mMainBinder!!)
                task.setOnLoadKeyListener(this@SplashActivity)
                task.execute()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onKeyLoaded(key: Key?) {
        if (key == null || key.key == "") {
            // 没有密钥，跳转到创建密钥界面
            start(CreateKeyOptionsActivity.getIntent(this, CreateKeyOptionsActivity.MODE_CREATE))
        } else if (key.key == Config.NO_PASSWORD && key.mode == Key.MODE_NO_KEY) {
            // 没有设置密钥，直接跳转到主界面
            start(MainActivity.getIntent(this))
        } else if (key.mode == Key.MODE_PATTERN) {
            // 已有密钥，跳转到图案密码解锁界面
            start(PatternLockActivity.getIntent(this, key.key))
        } else if (key.mode == Key.MODE_NUMBER) {
            // 已有密钥，跳转到数字密码解锁界面
            start(NumberLockActivity.getIntent(this, key.key))
        } else if (key.mode == Key.MODE_COMPLEX) {
            // 已有密钥，跳转到复杂密码解锁界面
            start(ComplexLockActivity.getIntent(this, key.key))
        }
    }

    private fun start(intent: Intent) {
        Config.mIsAllFinishing = false

        Handler().postDelayed({
            startActivity(intent)
            this.finish()
        }, 500)
    }

    override fun onBackPressed() {
        // Splash界面不允许使用back键
    }

    override fun onDestroy() {
        unbindService(mServiceConnection)
        super.onDestroy()
    }
}
