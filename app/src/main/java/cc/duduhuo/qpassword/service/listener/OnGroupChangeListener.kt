package cc.duduhuo.qpassword.service.listener

import cc.duduhuo.qpassword.bean.Group

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 20:51
 * Description: 分组变化监听
 * Remarks:
 * =======================================================
 */
interface OnGroupChangeListener : BaseListener {
    /**
     * 增加了新分组
     * @param group
     */
    fun onNewGroup(group: Group)

    /**
     * 删除分组
     * @param groupName 分组名称
     */
    fun onDeleteGroup(groupName: String)

    /**
     * 更新分组名称
     * @param oldGroupName 旧分组名称
     * @param newGroupName 新分组名称
     * @param merge 是否是合并分组
     */
    fun onUpdateGroupName(oldGroupName: String, newGroupName: String, merge: Boolean)

}