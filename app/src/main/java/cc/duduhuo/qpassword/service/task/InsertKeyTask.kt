package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.db.KeyService
import cc.duduhuo.qpassword.service.listener.OnNewKeyListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:49
 * Description: 添加新主密码的异步任务
 * Remarks:
 * =======================================================
 */
class InsertKeyTask(private val mKey: Key,
                    private val mKeyService: KeyService) : AsyncTask<Void, Void, Void?>() {
    private var mListener: OnNewKeyListener? = null

    fun setOnNewKeyListener(listener: OnNewKeyListener) {
        this.mListener = listener
    }

    override fun doInBackground(vararg params: Void?): Void? {
        mKeyService.addKey(mKey)
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        mListener?.onNewKey(mKey)
    }
}