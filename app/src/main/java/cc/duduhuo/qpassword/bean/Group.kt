package cc.duduhuo.qpassword.bean

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 16:28
 * Description: 密码分组
 * Remarks:
 * =======================================================
 */
data class Group(
    /** 分组名称 */
    var name: String = ""
) {
    companion object {
        const val NAME = "name"
    }
}