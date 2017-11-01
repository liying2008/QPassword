package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnPasswordChangeListener

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
    private lateinit var mListeners: List<OnPasswordChangeListener>
    fun setOnPasswordChangeListeners(listeners: List<OnPasswordChangeListener>) {
        mListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Password {
        mPasswordService.updatePassword(mPassword)
        return mPassword
    }

    override fun onPostExecute(result: Password) {
        super.onPostExecute(result)
        mListeners.map { it.onUpdatePassword(result) }
    }
}