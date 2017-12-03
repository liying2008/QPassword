package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Key

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 20:29
 * Description: 创建主密码监听
 * Remarks:
 * =======================================================
 */
interface OnNewKeyListener : BaseListener {
    /**
     * 创建主密码
     * @param key 主密码的 SHA-1 值
     */
    fun onNewKey(key: Key)
}