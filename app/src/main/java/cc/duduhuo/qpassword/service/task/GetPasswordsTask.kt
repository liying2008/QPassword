package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener
import cc.duduhuo.qpassword.service.listener.OnPasswordFailListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 21:23
 * Description: 获取密码的异步任务
 * Remarks:
 * =======================================================
 */
class GetPasswordsTask(private val mListener: OnGetPasswordsListener,
                       private val mGroupName: String?,
                       private val mPasswordService: PasswordService):AsyncTask<Void, Void, List<Password>?>() {
    private lateinit var mPasswordFailListeners: List<OnPasswordFailListener>

    fun setOnPasswordFailListeners(listeners: List<OnPasswordFailListener>) {
        mPasswordFailListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): List<Password>? {
        if (mGroupName == null) {
            return mPasswordService.getAllPassword()
        } else {
            return mPasswordService.getAllPasswordByGroupName(mGroupName)
        }
    }

    override fun onPostExecute(passwords: List<Password>?) {
        super.onPostExecute(passwords)
        if (passwords == null) {
            mPasswordFailListeners.filter { it.isAlive() }.forEach { it.onKeyLose() }
        } else {
            if (mListener.isAlive()) {
                mListener.onGetPasswords(mGroupName, passwords)
            }
        }
    }
}