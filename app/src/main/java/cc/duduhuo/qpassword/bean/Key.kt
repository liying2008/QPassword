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
    /** 主密码的 SHA1 值 */
    var key: String = "",
    /** 主密码模式 */
    var mode: Int = MODE_COMPLEX
) {
    companion object {
        const val KEY = "key"
        const val MODE = "mode"

        /** 图案密码 */
        const val MODE_PATTERN = 0

        /** 数字密码 */
        const val MODE_NUMBER = 1

        /** 复杂密码 */
        const val MODE_COMPLEX = 2

        /** 无主密码 */
        const val MODE_NO_KEY = 3
    }
}