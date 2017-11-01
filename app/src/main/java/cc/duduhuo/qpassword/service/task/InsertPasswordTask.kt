package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnGroupChangeListener
import cc.duduhuo.qpassword.service.listener.OnPasswordChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 22:25
 * Description: 添加新密码的异步任务
 * Remarks:
 * =======================================================
 */
class InsertPasswordTask(private val mPassword: Password,
                         private val mPasswordService: PasswordService,
                         private val mGroupService: GroupService) : AsyncTask<Void, Void, Password>() {
    private var mIsNew = true
    private lateinit var mPasswordListeners: List<OnPasswordChangeListener>
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnPasswordChangeListeners(listeners: List<OnPasswordChangeListener>) {
        mPasswordListeners = listeners
    }

    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Password {
        val newGroupName = mPassword.groupName
        val groups = mGroupService.getAllGroups()
        mIsNew = groups.none { it.name == newGroupName }    // 是否是新的分组

        if (mIsNew) {
            // 添加新分组
            val group = Group()
            group.name = newGroupName
            mGroupService.addGroup(group)
        }
        val id = mPasswordService.insertPassword(mPassword)
        mPassword.id = id
        return mPassword
    }

    override fun onPostExecute(result: Password) {
        super.onPostExecute(result)
        if (mIsNew) {
            val group = Group()
            group.name = result.groupName
            mGroupListeners.map { it.onNewGroup(group) }
        }
        mPasswordListeners.map { it.onNewPassword(result) }
    }
}