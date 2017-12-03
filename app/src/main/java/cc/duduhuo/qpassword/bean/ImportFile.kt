package cc.duduhuo.qpassword.bean

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/20 22:57
 * Description: 导入的文件的实体
 * Remarks:
 * =======================================================
 */
class ImportFile(
    /** 文件名 */
    var fileName: String = "",
    /** 文件的绝对路径 */
    var absolutePath: String = "",
    /** 文件大小 */
    var fileSize: Long = 0L
)