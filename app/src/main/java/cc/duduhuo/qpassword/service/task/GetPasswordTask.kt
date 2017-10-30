package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnGetPasswordListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 22:07
 * Description: 根据ID获取密码的异步任务
 * Remarks:
 * =======================================================
 */
class GetPasswordTask(val mId: Long,
                      val mListener: OnGetPasswordListener,
                      val mPasswordService: PasswordService) : AsyncTask<Void, Void, Password?>() {
    override fun doInBackground(vararg params: Void?): Password? {
        return mPasswordService.getPassword(mId)
    }

    override fun onPostExecute(result: Password?) {
        super.onPostExecute(result)
        mListener.onGetPassword(result)
    }

}