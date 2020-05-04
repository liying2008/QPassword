package cc.duduhuo.qpassword.bean

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 16:27
 * Description: 密码
 * Remarks:
 * =======================================================
 */
data class Password @JvmOverloads constructor(
    /** ID */
    var id: Long = 0,
    /** 创建时间 */
    var createDate: Long = 0,
    /** 标题 */
    var title: String = "",
    /** 用户名 */
    var username: String = "",
    /** 密码 */
    var password: String = "",
    /** 邮箱 */
    var email: String = "",
    /** 是否置顶 */
    var isTop: Boolean = false,
    /** 备注 */
    var note: String = "",
    /** 所属分组 */
    var groupName: String = ""
) {

    companion object {
        const val ID = "id"
        const val CREATE_DATE = "create_date"
        const val TITLE = "title"
        const val USERNAME = "user_name"
        const val PASSWORD = "password"
        const val EMAIL = "email"
        const val IS_TOP = "is_top"
        const val NOTE = "note"
        const val GROUP_NAME = "group_name"
    }
}