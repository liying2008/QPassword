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

    /** 导出的文件的扩展名 */
    const val EXPORT_FILE_EXTENSION = ".qpwd"

    /** 导出的文件的文件名格式 */
    const val EXPORT_FILENAME_FORMAT = "yyyMMdd-HHmmss"

    /** 没有设置主密码时，key的默认值 */
    const val NO_PASSWORD = "0"

    /** 主密码对象 */
    var mKey: Key? = null

    /** 未经 SHA-1加密的主密码 */
    var mOriKey: String = NO_PASSWORD

    /** 用来标识应用是否正在重启，防止多次重启应用 */
    var mIsAllFinishing = false
}
