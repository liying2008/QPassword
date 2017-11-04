package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.db.KeyService

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:49
 * Description: 添加新密钥的异步任务
 * Remarks:
 * =======================================================
 */
class InsertKeyTask(private val mKey: Key,
                    private val mKeyService: KeyService) : AsyncTask<Void, Void, Void?>() {
    override fun doInBackground(vararg params: Void?): Void? {
        mKeyService.addKey(mKey)
        return null
    }
}