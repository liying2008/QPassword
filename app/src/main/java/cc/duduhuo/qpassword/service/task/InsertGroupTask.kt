package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
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
                      private val mGroupService: GroupService):AsyncTask<Void, Void, Group>() {
    private var mIsNew = true
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Group {
        val newGroupName = mGroup.name
        val groups = mGroupService.getAllGroups()
        mIsNew = groups.none { it.name == newGroupName }    // 是否是新的分组
        if (mIsNew) {
            // 添加新分组
            val group = Group()
            group.name = newGroupName
            mGroupService.addGroup(group)
        }
        return mGroup
    }

    override fun onPostExecute(result: Group) {
        super.onPostExecute(result)
        if (mIsNew) {
            mGroupListeners.map { it.onNewGroup(result) }
        } else {
            // todo 考虑将值返回，不在此处处理
            AppToast.showToast(R.string.info_group_exist)
        }
    }
}