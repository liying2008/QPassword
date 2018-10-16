package cc.duduhuo.qpassword.bean

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/5 16:32
 * Description: 导出的数据类
 * Remarks:
 * =======================================================
 */
data class Export @JvmOverloads constructor(
    /** 是否加密 */
    var isEncrypted: Boolean = false,
    /** 是否采用图案解锁 */
    var isPattern: Boolean = false,
    /** 主密码的 SHA1 值 */
    var key: String = "",
    /** 密码列表 */
    var passwords: List<Password> = mutableListOf()
)