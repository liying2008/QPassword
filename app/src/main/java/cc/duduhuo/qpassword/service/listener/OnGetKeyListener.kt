package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Key

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:26
 * Description: 获取主密码监听
 * Remarks:
 * =======================================================
 */
interface OnGetKeyListener : BaseListener {
    /**
     * 获取主密码的 SHA1 值
     * @param key 主密码的 SHA1 值
     */
    fun onGetKey(key: Key?)
}