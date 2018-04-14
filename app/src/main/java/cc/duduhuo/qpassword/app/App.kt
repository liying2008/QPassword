package cc.duduhuo.qpassword.app

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
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

    companion object {
        /**
         * 判断是否是 DEBUG 版本
         * @param context
         */
        fun isDebugVersion(context: Context): Boolean {
            try {
                val info = context.applicationInfo
                return info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}
