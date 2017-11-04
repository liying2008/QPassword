package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Key

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:26
 * Description: 获取密钥（SHA1 加密）监听
 * Remarks:
 * =======================================================
 */
interface OnGetKeyListener {
    /**
     * 获取加密密钥的 SHA1 值
     * @param key 加密密钥的 SHA1 值
     */
    fun onGetKey(key: Key?)
}