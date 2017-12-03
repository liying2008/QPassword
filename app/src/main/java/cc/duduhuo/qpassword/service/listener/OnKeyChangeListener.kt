package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Key

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 15:40
 * Description: 主密码变化监听
 * Remarks:
 * =======================================================
 */
interface OnKeyChangeListener : BaseListener {
    /**
     * 更新了主密码
     * @param oldKey 旧主密码
     * @param newKey 新主密码
     */
    fun onUpdateKey(oldKey: Key, newKey: Key)
}