package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnPasswordsChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/26 21:32
 * Description: 删除密码的异步任务
 * Remarks:
 * =======================================================
 */
class DeletePasswordsTask(private val mPasswords: List<Password>,
                          private val passwordService: PasswordService) : AsyncTask<Void, Void, Void?>() {
    private lateinit var mListeners: List<OnPasswordsChangeListener>
    fun setOnPasswordsChangeListeners(listeners: List<OnPasswordsChangeListener>) {
        mListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Void? {
        passwordService.deletePasswords(mPasswords)
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        mListeners.filter { it.isAlive() }.forEach { it.onDeletePasswords(mPasswords) }
    }
}