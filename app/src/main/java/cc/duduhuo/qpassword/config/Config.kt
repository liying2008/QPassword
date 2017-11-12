package cc.duduhuo.qpassword.config

import cc.duduhuo.qpassword.bean.Key

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 18:17
 * Description: 应用的一些配置信息
 * Remarks:
 * =======================================================
 */
object Config {
    /** 上次显示的分组名成 */
    const val LAST_GROUP = "last_group"
    /** 上次导出密码的方式 */
    const val LAST_EXPORT = "last_export"
    /** 存储卡上的工作目录 */
    const val WORK_DIR = "QPassword"
    /** 密码导入导出目录 */
    const val EXPORT_DIR = "password"
    /** 没有设置主密码时，key的默认值 */
    const val NO_PASSWORD = "0"
    /** 主密码 */
    var mKey: Key? = null
}
