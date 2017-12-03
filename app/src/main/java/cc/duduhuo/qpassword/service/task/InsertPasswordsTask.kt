package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.OnGroupChangeListener
import cc.duduhuo.qpassword.service.listener.OnPasswordFailListener
import cc.duduhuo.qpassword.service.listener.OnPasswordsChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/25 11:16
 * Description: 添加新密码的异步任务
 * Remarks:
 * =======================================================
 */
class InsertPasswordsTask(private val mPasswords: List<Password>,
                          private val mPasswordService: PasswordService,
                          private val mGroupService: GroupService) : AsyncTask<Void, Void, Void?>() {
    private var mId: Int = 0
    private val mNewGroups: MutableList<Group> = mutableListOf()
    private lateinit var mPasswordsListeners: List<OnPasswordsChangeListener>
    private lateinit var mPasswordFailListeners: List<OnPasswordFailListener>
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnPasswordsChangeListeners(listeners: List<OnPasswordsChangeListener>) {
        mPasswordsListeners = listeners
    }

    fun setOnPasswordFailListeners(listeners: List<OnPasswordFailListener>) {
        mPasswordFailListeners = listeners
    }

    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val groups = mGroupService.getAllGroups().toMutableList()
        mPasswords.map {
            val newGroupName = it.groupName
            if (groups.none { g -> g.name == newGroupName }) {
                val group = Group()
                group.name = newGroupName
                mGroupService.addGroup(group)
                groups.add(group)
                mNewGroups.add(group)
            }
        }

        mId = mPasswordService.insertPasswords(mPasswords)
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        if (mId == -2) {
            mPasswordFailListeners.filter { it.isAlive() }.forEach { it.onKeyLose() }
        } else {
            mNewGroups.forEach { group ->
                mGroupListeners.filter { it.isAlive() }.forEach { it.onNewGroup(group) }
            }
            mPasswordsListeners.filter { it.isAlive() }.forEach { it.onNewPasswords(mPasswords) }
        }
    }
}