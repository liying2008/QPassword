package cc.duduhuo.qpassword.service.task

import android.os.AsyncTask
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.service.listener.OnGetAllGroupsListener

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 20:30
 * Description: 获取所有密码分组的异步任务
 * Remarks:
 * =======================================================
 */
class GetAllGroupsTask(private val mGroupService: GroupService,
                       private val mListener: OnGetAllGroupsListener) : AsyncTask<Void, Void, List<Group>>() {

    override fun doInBackground(vararg params: Void?): List<Group> {
        return mGroupService.getAllGroups()
    }

    override fun onPostExecute(result: List<Group>) {
        super.onPostExecute(result)
        if (mListener.isAlive()) {
            mListener.onGetAllGroups(result)
        }
    }
}