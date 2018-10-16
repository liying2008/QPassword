package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnPasswordChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 21:33
 * Description: 删除密码的异步任务
 * Remarks:
 * =======================================================
 */
class DeletePasswordTask(private val mPassword: Password,
                         private val passwordService: PasswordService) : AsyncTask<Void, Void, Int>() {
    private lateinit var mListeners: List<OnPasswordChangeListener>
    fun setOnPasswordChangeListeners(listeners: List<OnPasswordChangeListener>) {
        mListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Int {
        return passwordService.deletePassword(mPassword.id)
    }

    override fun onPostExecute(result: Int) {
        super.onPostExecute(result)
        if (result > 0) {
            mListeners.filter { it.isAlive() }.forEach { it.onDeletePassword(mPassword) }
        }
    }
}