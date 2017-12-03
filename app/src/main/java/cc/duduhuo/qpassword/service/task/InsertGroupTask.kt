package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.service.listener.OnGroupChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 23:09
 * Description: 添加分组的异步任务
 * Remarks:
 * =======================================================
 */
class InsertGroupTask(private val mGroup: Group,
                      private val mGroupService: GroupService) : AsyncTask<Void, Void, Group>() {
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Group {
        val group = Group()
        group.name = mGroup.name
        mGroupService.addGroup(group)
        return mGroup
    }

    override fun onPostExecute(result: Group) {
        super.onPostExecute(result)
        mGroupListeners.filter { it.isAlive() }.forEach { it.onNewGroup(result) }
    }
}