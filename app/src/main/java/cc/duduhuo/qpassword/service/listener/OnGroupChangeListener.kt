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
interface OnGroupChangeListener {
    /** 增加了新分组 */
    fun onNewGroup(group: Group)

    /** 删除分组 */
    fun onDeleteGroup(groupName: String)

    /** 更新分组名称 */
    fun onUpdateGroupName(oldGroupName: String, newGroupName: String)

}