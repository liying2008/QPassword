package cc.duduhuo.qpassword.bean

import java.io.Serializable

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 16:28
 * Description: 密码分组
 * Remarks:
 * =======================================================
 */
class Group : Serializable {
    companion object {
        val NAME = "name"
    }

    /** 分组名称 */
    var name: String = ""
}