package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Password

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 21:14
 * Description: 获取密码的 Listener
 * Remarks:
 * =======================================================
 */
interface OnGetPasswordsListener : BaseListener {
    /**
     * 根据分组名称获取密码（如果分组名称为 null， 则获取所有密码）
     * @param groupName 分组名称
     * @param passwords 获取的密码列表
     */
    fun onGetPasswords(groupName: String?, passwords: List<Password>)
}