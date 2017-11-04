package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Key

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:40
 * Description: 密钥变化监听
 * Remarks:
 * =======================================================
 */
interface OnKeyChangeListener {
    /**
     * 更新了密钥
     * @param oldKey 旧密钥
     * @param newKey 新密钥
     */
    fun onUpdateKey(oldKey: Key, newKey: Key)
}