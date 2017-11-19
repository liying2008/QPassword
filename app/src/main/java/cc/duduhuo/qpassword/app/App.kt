package cc.duduhuo.qpassword.app

import android.app.Application
import cc.duduhuo.applicationtoast.AppToast

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 10:34
 * Description:
 * Remarks:
 * =======================================================
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppToast.init(this)
    }
}
