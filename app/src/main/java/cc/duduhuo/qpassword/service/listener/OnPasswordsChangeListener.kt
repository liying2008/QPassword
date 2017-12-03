package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Password

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/25 11:47
 * Description: 密码变化监听
 * Remarks:
 * =======================================================
 */
interface OnPasswordsChangeListener : BaseListener {
    /**
     * 增加了新密码
     * @param passwords 增加的密码
     */
    fun onNewPasswords(passwords: List<Password>)

    /**
     * 删除了密码
     * @param passwords 删除的密码
     */
    fun onDeletePasswords(passwords: List<Password>)
}