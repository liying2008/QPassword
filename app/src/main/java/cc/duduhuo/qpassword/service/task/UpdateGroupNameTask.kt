package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.service.listener.OnGroupChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 20:35
 * Description: 更新分组名称的异步任务
 * Remarks:
 * =======================================================
 */
class UpdateGroupNameTask(val mOldName: String,
                          val mNewName: String,
                          val mGroupService: GroupService) : AsyncTask<Void, Void, Void>() {
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Void? {
        mGroupService.updatePasswdGroupName(mOldName, mNewName)
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        mGroupListeners.map { it.onUpdateGroupName(mOldName, mNewName) }
    }
}