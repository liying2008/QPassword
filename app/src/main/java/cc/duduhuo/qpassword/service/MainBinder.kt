package cc.duduhuo.qpassword.service

import android.content.Context
import android.os.Binder
import cc.duduhuo.qpassword.app.App
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.db.KeyService
import cc.duduhuo.qpassword.db.PasswordService
import cc.duduhuo.qpassword.service.listener.*
import cc.duduhuo.qpassword.service.task.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/29 18:11
 * Description:
 * Remarks:
 * =======================================================
 */
class MainBinder(context: Context, val mApp: App) : Binder() {
    private val mKeyService: KeyService = KeyService(context)
    private val mPasswordService: PasswordService = PasswordService(context)
    private val mGroupService: GroupService = GroupService(context)
    /** 密码变化监听器 */
    private val mOnPasswordChangeListeners = mutableListOf<OnPasswordChangeListener>()

    /** 主密码变化监听器 */
    private val mOnKeyChangeListeners = mutableListOf<OnKeyChangeListener>()

    /** 分组变化监听器 */
    private val mOnGroupChangeListeners = mutableListOf<OnGroupChangeListener>()

    /**
     * 注册密码变化监听器
     * @param listener 密码变化监听器
     */
    fun registerOnPasswordChangeListener(listener: OnPasswordChangeListener) {
        mOnPasswordChangeListeners.add(listener)
    }

    /**
     * 注册主密码变化监听器
     * @param listener 主密码变化监听器
     */
    fun registerOnKeyChangeListener(listener: OnKeyChangeListener) {
        mOnKeyChangeListeners.add(listener)
    }

    /**
     * 注册分组变化监听器
     * @param listener 分组变化监听器
     */
    fun registerOnGroupChangeListener(listener: OnGroupChangeListener) {
        mOnGroupChangeListeners.add(listener)
    }

    /**
     * 添加主密码
     * @param key 主密码
     */
    fun insertKey(key: Key, listener: OnNewKeyListener) {
        val task = InsertKeyTask(key, mKeyService)
        task.setOnNewKeyListener(listener)
        task.execute()
    }

    /**
     * 获取主密码
     * @param listener
     */
    fun getKey(listener: OnGetKeyListener) {
        val task = GetKeyTask(listener, mKeyService)
        task.execute()
    }

    /**
     * 更新主密码
     * @param oldKey 旧主密码
     * @param newKey 新主密码
     */
    fun updateKey(oldKey: Key, newKey: Key) {
        val task = UpdateKeyTask(oldKey, newKey, mKeyService)
        task.setOnKeyChangeListener(mOnKeyChangeListeners)
        task.execute()
    }

    /**
     * 获取密码
     * @param listener
     * @param groupName 分组，如果为 null, 则获取全部密码
     */
    fun getPasswords(listener: OnGetPasswordsListener, groupName: String? = null) {
        val task = GetPasswordsTask(listener, groupName, mPasswordService)
        task.execute()
    }

    /**
     * 删除密码
     * @param password 要删除的密码
     */
    fun deletePassword(password: Password) {
        val task = DeletePasswordTask(password, mPasswordService)
        task.setOnPasswordChangeListeners(mOnPasswordChangeListeners)
        task.execute()
    }

    /**
     * 根据ID获取密码
     * @param listener
     * @param id ID
     */
    fun getPassword(listener: OnGetPasswordListener, id: Long) {
        val task = GetPasswordTask(id, listener, mPasswordService)
        task.execute()
    }

    /**
     * 更新密码
     * @param password 新密码对象
     */
    fun updatePassword(password: Password) {
        val task = UpdatePasswordTask(password, mPasswordService)
        task.setOnPasswordChangeListeners(mOnPasswordChangeListeners)
        task.execute()
    }

    /**
     * 添加新密码
     * @param password 新密码
     */
    fun insertPassword(password: Password) {
        val task = InsertPasswordTask(password, mPasswordService, mGroupService)
        task.setOnGroupChangeListeners(mOnGroupChangeListeners)
        task.setOnPasswordChangeListeners(mOnPasswordChangeListeners)
        task.execute()
    }

    /**
     * 添加新分组
     * @param group 新分组
     */
    fun insertGroup(group: Group) {
        val task = InsertGroupTask(group, mGroupService)
        task.setOnGroupChangeListeners(mOnGroupChangeListeners)
        task.execute()
    }

    /**
     * 删除分组
     * @param group 要删除的分组（该分组下的所有密码会一并删除）
     */
    fun deleteGroup(groupName: String) {
        val task = DeleteGroupTask(groupName, mGroupService)
        task.setOnGroupChangeListeners(mOnGroupChangeListeners)
        task.execute()
    }

    /**
     * 获取所有分组
     * @param listener
     */
    fun getAllGroups(listener: OnGetAllGroupsListener) {
        val task = GetAllGroupsTask(mGroupService, listener)
        task.execute()
    }

    /**
     * 更新分组名称
     * @param oldName 旧名字
     * @param newName 新名字
     * @param merge 是否是合并分组
     */
    fun updateGroupName(oldName: String, newName: String, merge: Boolean) {
        val task = UpdateGroupNameTask(oldName, newName, merge, mGroupService)
        task.setOnGroupChangeListeners(mOnGroupChangeListeners)
        task.execute()
    }

    fun onDestroy() {
        mOnGroupChangeListeners.clear()
        mOnPasswordChangeListeners.clear()
    }
}