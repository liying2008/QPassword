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

// 有图片和文字的item
class DrawerItemNormal(var iconRes: Int, var title: String) : DrawerItem

// 分割线item
class DrawerItemDivider : DrawerItem

// 头部item
class DrawerItemHeader : DrawerItem

class DrawerItemTitle(var title: String) : DrawerItem