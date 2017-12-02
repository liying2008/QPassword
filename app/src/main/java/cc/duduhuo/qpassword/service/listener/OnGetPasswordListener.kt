package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Password

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 22:04
 * Description: 获取密码 Listener
 * Remarks:
 * =======================================================
 */
interface OnGetPasswordListener : BaseListener {
    /**
     * 根据ID获取密码
     * @param password 密码
     */
    fun onGetPassword(password: Password?)
}