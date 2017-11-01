package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener

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
                       private val mPasswordService: PasswordService):AsyncTask<Void, Void, List<Password>>() {
    override fun doInBackground(vararg params: Void?): List<Password> {
        if (mGroupName == null) {
            return mPasswordService.getAllPassword()
        } else {
            return mPasswordService.getAllPasswordByGroupName(mGroupName)
        }
    }

    override fun onPostExecute(result: List<Password>) {
        super.onPostExecute(result)
        mListener.onGetPasswords(mGroupName, result)
    }
}