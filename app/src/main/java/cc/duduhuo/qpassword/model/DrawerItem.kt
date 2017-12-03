package cc.duduhuo.qpassword.model

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 15:27
 * Description:
 * Remarks:
 * =======================================================
 */
interface DrawerItem

// 分组 Item
class GroupDrawerItem(var iconRes: Int, var title: String) : DrawerItem

// 操作 Item
class OperationDrawerItem(var iconRes: Int, var title: String) : DrawerItem

// 分割线 Item
class DividerDrawerItem : DrawerItem

// 头部 Item
class HeaderDrawerItem : DrawerItem

// 标题 Item
class TitleDrawerItem(var title: String) : DrawerItem