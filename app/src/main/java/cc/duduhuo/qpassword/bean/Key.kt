package cc.duduhuo.qpassword.bean

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/4 14:55
 * Description: 密钥的 SHA1 值
 * Remarks:
 * =======================================================
 */
data class Key(
    /** 密钥的 SHA1 值 */
    var key: String = "") {
    companion object {
        const val KEY = "key"
    }
}