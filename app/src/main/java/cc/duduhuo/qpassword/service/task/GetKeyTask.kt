package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.db.KeyService
import cc.duduhuo.qpassword.service.listener.OnGetKeyListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:29
 * Description: 获取主密码（SHA1 值）的异步任务
 * Remarks:
 * =======================================================
 */
class GetKeyTask(private val mListener: OnGetKeyListener,
                 private val mKeyService: KeyService) : AsyncTask<Void, Void, Key?>() {
    override fun doInBackground(vararg params: Void?): Key? {
        return mKeyService.getKey()
    }

    override fun onPostExecute(result: Key?) {
        super.onPostExecute(result)
        if (mListener.isAlive()) {
            mListener.onGetKey(result)
        }
    }
}