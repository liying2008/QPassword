package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.db.KeyService
import cc.duduhuo.qpassword.service.listener.OnKeyChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:33
 * Description: 更新主密码（SHA 1值）的异步任务类
 * Remarks:
 * =======================================================
 */
class UpdateKeyTask(private val mOldKey: Key,
                    private val mOldOriKey: String,
                    private val mNewKey: Key,
                    private val mNewOriKey: String,
                    private val mKeyService: KeyService) : AsyncTask<Void, Void, Boolean>() {
    private lateinit var mListener: OnKeyChangeListener

    fun setOnKeyChangeListener(listener: OnKeyChangeListener) {
        this.mListener = listener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        return mKeyService.updateKey(mOldKey, mOldOriKey, mNewKey, mNewOriKey)
    }

    override fun onPostExecute(update: Boolean) {
        super.onPostExecute(update)
        if (mListener.isAlive()) {
            mListener.onUpdateKey(mOldKey, mNewKey)
        }
    }
}