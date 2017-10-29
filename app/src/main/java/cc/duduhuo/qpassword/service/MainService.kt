package cc.duduhuo.qpassword.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import cc.duduhuo.qpassword.app.App

class MainService : Service() {
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, MainService::class.java)
        }
    }

    private lateinit var mMainBinder: MainBinder
    override fun onCreate() {
        super.onCreate()
        mMainBinder = MainBinder(this, applicationContext as App)
    }

    override fun onBind(intent: Intent): IBinder {
        return mMainBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainBinder.onDestroy()
    }
}
