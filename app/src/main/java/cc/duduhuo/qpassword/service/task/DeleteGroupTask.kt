package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.service.listener.OnGroupChangeListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 23:22
 * Description: 删除分组的异步任务
 * Remarks:
 * =======================================================
 */
class DeleteGroupTask(private val mGroupName: String,
                      private val mGroupService: GroupService) : AsyncTask<Void, Void, Int>() {
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Int {
        return mGroupService.deleteGroup(mGroupName)
    }

    override fun onPostExecute(result: Int) {
        super.onPostExecute(result)
        if (result > 0) {
            mGroupListeners.filter { it.isAlive() }.forEach { it.onDeleteGroup(mGroupName) }
        }
    }
}