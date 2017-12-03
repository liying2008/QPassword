package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Password

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 20:49
 * Description: 密码变化监听
 * Remarks:
 * =======================================================
 */
interface OnPasswordChangeListener : BaseListener {
    /**
     * 增加了新密码
     * @param password 增加的密码
     */
    fun onNewPassword(password: Password)

    /**
     * 删除了密码
     * @param password 删除的密码
     */
    fun onDeletePassword(password: Password)

    /**
     * 更新了密码
     * @param newPassword 新密码数据
     */
    fun onUpdatePassword(newPassword: Password)

}