package cc.duduhuo.qpassword.service.listener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/18 12:03
 * Description: 读取 / 更新 / 写入密码失败监听
 * Remarks:
 * =======================================================
 */
interface OnPasswordFailListener : BaseListener {
    /** 插入密码失败 */
    fun onInsertFail()

    /** 读取密码失败 */
    fun onReadFail()

    /** 主密码丢失 */
    fun onKeyLose()
}