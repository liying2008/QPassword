package cc.duduhuo.qpassword.bean

import java.io.Serializable

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 16:27
 * Description: 密码
 * Remarks:
 * =======================================================
 */
class Password : Serializable {
    companion object {
        val ID = "id"
        val CREATE_DATE = "create_date"
        val TITLE = "title"
        val USERNAME = "user_name"
        val PASSWORD = "password"
        val EMAIL = "email"
        val IS_TOP = "is_top"
        val NOTE = "note"
        val GROUP_NAME = "group_name"
    }

    /** ID */
    var id: Long = 0
    /** 创建时间 */
    var createDate: Int = 0
    /** 标题 */
    var title: String = ""
    /** 用户名 */
    var username: String = ""
    /** 密码 */
    var password: String = ""
    /** 邮箱 */
    var email: String = ""
    /** 是否置顶 */
    var isTop: Boolean = false
    /** 备注 */
    var note: String = ""
    /** 所属分组 */
    var groupName: String = ""
}