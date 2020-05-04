package cc.duduhuo.qpassword.service.listener

import android.app.Activity
import android.app.Dialog
import android.util.Log

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/12/2 17:56
 * Description:
 * Remarks:
 * =======================================================
 */
interface BaseListener {
    /**
     * 实现该 Listener 的 Activity 或 Dialog 是否存活
     */
    fun isAlive(): Boolean {
        Log.d("TTT", "--> $this")
        if (this is Activity && this.isFinishing) {
            Log.d("TTT", this.javaClass.simpleName + " Die")
            return false
        } else if (this is Dialog && !this.isShowing) {
            Log.d("TTT", this.javaClass.simpleName + " Die")
            return false
        }
        Log.d("TTT", this.javaClass.simpleName + " Alive")
        return true
    }
}