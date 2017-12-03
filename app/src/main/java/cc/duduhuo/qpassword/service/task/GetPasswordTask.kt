package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnGetPasswordListener
import cc.duduhuo.qpassword.service.listener.OnPasswordFailListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 22:07
 * Description: 根据ID获取密码的异步任务
 * Remarks:
 * =======================================================
 */
class GetPasswordTask(private val mId: Long,
                      private val mListener: OnGetPasswordListener,
                      private val mPasswordService: PasswordService) : AsyncTask<Void, Void, Password?>() {

    private lateinit var mPasswordFailListeners: List<OnPasswordFailListener>

    fun setOnPasswordFailListeners(listeners: List<OnPasswordFailListener>) {
        mPasswordFailListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Password? {
        return mPasswordService.getPassword(mId)
    }

    override fun onPostExecute(password: Password?) {
        super.onPostExecute(password)
        if (password == null) {
            mPasswordFailListeners.filter { it.isAlive() }.forEach { it.onKeyLose() }
        } else if (password.id == -1L) {
            mPasswordFailListeners.filter { it.isAlive() }.forEach { it.onReadFail() }
        } else {
            if (mListener.isAlive()) {
                mListener.onGetPassword(password)
            }
        }
    }

}