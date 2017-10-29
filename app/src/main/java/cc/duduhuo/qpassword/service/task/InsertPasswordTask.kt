package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import android.os.Bundle
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
class InsertPasswordTask(val mPassword: Password,
                         val mPasswordService: PasswordService,
                         val mGroupService: GroupService) : AsyncTask<Void, Void, Bundle>() {
    private lateinit var mPasswordListeners: List<OnPasswordChangeListener>
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnPasswordChangeListeners(listeners: List<OnPasswordChangeListener>) {
        mPasswordListeners = listeners
    }

    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Bundle {
        val bundle = Bundle()
        val newGroupName = mPassword.groupName
        val groups = mGroupService.getAllPasswordGroup()
        val isNew = groups.none { it.name == newGroupName }    // 是否是新的分组

        if (isNew) {
            // 添加新分组
            val group = Group()
            group.name = newGroupName
            mGroupService.addPasswordGroup(group)
        }
        bundle.putBoolean("isNew", isNew)
        val id = mPasswordService.insertPassword(mPassword)
        mPassword.id = id
        bundle.putSerializable("password", mPassword)
        return bundle
    }

    override fun onPostExecute(result: Bundle) {
        super.onPostExecute(result)
        val isNew = result.getBoolean("isNew")
        val password: Password = result.getSerializable("password") as Password

        if (isNew) {
            val group = Group()
            group.name = password.groupName
            mGroupListeners.map { it.onNewGroup(group) }
        }
        mPasswordListeners.map { it.onNewPassword(password) }
    }
}