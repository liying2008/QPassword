package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnPasswordChangeListener
import cc.duduhuo.qpassword.service.listener.OnPasswordFailListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 22:15
 * Description: 更新密码的异步任务类
 * Remarks:
 * =======================================================
 */
class UpdatePasswordTask(private val mPassword: Password,
                         private val mPasswordService: PasswordService) : AsyncTask<Void, Void, Password>() {
    private var mId = 0
    private lateinit var mListeners: List<OnPasswordChangeListener>
    private lateinit var mPasswordFailListeners: List<OnPasswordFailListener>
    fun setOnPasswordFailListeners(listeners: List<OnPasswordFailListener>) {
        mPasswordFailListeners = listeners
    }

    fun setOnPasswordChangeListeners(listeners: List<OnPasswordChangeListener>) {
        mListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Password {
        mId = mPasswordService.updatePassword(mPassword)
        return mPassword
    }

    override fun onPostExecute(password: Password) {
        super.onPostExecute(password)
        if (mId == -2) {
            mPasswordFailListeners.filter { it.isAlive() }.forEach { it.onKeyLose() }
        } else {
            mListeners.filter { it.isAlive() }.forEach { it.onUpdatePassword(password) }
        }
    }
}