package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.listener.OnGetKeyListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 17:16
 * Description: 启动页加载数据任务类
 * Remarks:
 * =======================================================
 */
class SplashLoadDataTask(private val mMainBinder: MainBinder) : AsyncTask<Void, Void, Void?>() {
    private var mListener: OnLoadKeyListener? = null
    fun setOnLoadKeyListener(listener: OnLoadKeyListener) {
        this.mListener = listener
    }

    override fun doInBackground(vararg params: Void?): Void? {
        loadKey()
        return null
    }

    /**
     * 加载密钥的 SHA1 值
     */
    private fun loadKey() {
        mMainBinder.getKey(object : OnGetKeyListener {
            override fun onGetKey(key: Key?) {
                Config.mKey = key
                mListener?.onKeyLoaded(key)
            }

        })
    }

    /**
     * Splash 界面加载密钥 SHA-1 值
     */
    interface OnLoadKeyListener {
        /**
         * 密钥已加载
         * @param key SHA-1 加密后的密钥
         */
        fun onKeyLoaded(key: Key?)
    }
}