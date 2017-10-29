package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import android.os.Bundle
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
class InsertGroupTask(val mGroup: Group, val mGroupService: GroupService):AsyncTask<Void, Void, Bundle>() {
    private lateinit var mGroupListeners: List<OnGroupChangeListener>
    fun setOnGroupChangeListeners(listeners: List<OnGroupChangeListener>) {
        mGroupListeners = listeners
    }

    override fun doInBackground(vararg params: Void?): Bundle {
        val bundle = Bundle()
        val newGroupName = mGroup.name
        val groups = mGroupService.getAllPasswordGroup()
        val isNew = groups.none { it.name == newGroupName }    // 是否是新的分组
        if (isNew) {
            // 添加新分组
            val group = Group()
            group.name = newGroupName
            mGroupService.addPasswordGroup(group)
        }
        bundle.putBoolean("isNew", isNew)
        bundle.putSerializable("group", mGroup)
        return bundle
    }

    override fun onPostExecute(result: Bundle) {
        super.onPostExecute(result)
        val isNew = result.getBoolean("isNew")
        val group = result.getSerializable("group") as Group
        if (isNew) {
            mGroupListeners.map { it.onNewGroup(group) }
        } else {
            AppToast.showToast(R.string.info_group_exist)
        }
    }
}