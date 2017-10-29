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
interface OnPasswordChangeListener {
    /**  增加了新密码 */
    fun onNewPassword(password: Password)

    /** 删除了密码 */
    fun onDeletePassword(id: Long)

    /** 更新了密码 */
    fun onUpdatePassword(password: Password)

}