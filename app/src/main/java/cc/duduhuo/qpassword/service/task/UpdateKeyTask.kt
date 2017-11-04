package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.db.KeyService
import cc.duduhuo.qpassword.service.listener.OnKeyChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:33
 * Description: 更新密钥（SHA 1值）的异步任务类
 * Remarks:
 * =======================================================
 */
class UpdateKeyTask(private val mOldKey: Key,
                    private val mNewKey: Key,
                    private val mKeyService: KeyService) : AsyncTask<Void, Void, Void?>() {
    private lateinit var mListeners: List<OnKeyChangeListener>

    fun setOnKeyChangeListener(listeners: List<OnKeyChangeListener>) {
        this.mListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Void? {
        mKeyService.updateKey(mOldKey, mNewKey)
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        mListeners.map { it.onUpdateKey(mOldKey, mNewKey) }
    }
}