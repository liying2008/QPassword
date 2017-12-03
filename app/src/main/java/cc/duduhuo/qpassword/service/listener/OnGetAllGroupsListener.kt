package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Group

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 20:14
 * Description: 获取所有密码的 Listener
 * Remarks:
 * =======================================================
 */
interface OnGetAllGroupsListener : BaseListener {
    /**
     * 获取所有密码
     * @param groups 密码列表
     */
    fun onGetAllGroups(groups: List<Group>)
}