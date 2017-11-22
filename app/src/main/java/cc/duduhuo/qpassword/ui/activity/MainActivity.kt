package cc.duduhuo.qpassword.ui.activity

import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.DrawerItemAdapter
import cc.duduhuo.qpassword.adapter.PasswordListAdapter
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.model.GroupDrawerItem
import cc.duduhuo.qpassword.model.OperationDrawerItem
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.*
import cc.duduhuo.qpassword.util.PreferencesUtils
import cc.duduhuo.qpassword.util.copyText
import cc.duduhuo.qpassword.util.keyLost
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.properties.Delegates


class MainActivity : BaseActivity(), OnGetPasswordsListener, OnPasswordChangeListener, OnGroupChangeListener, OnGetAllGroupsListener, OnPasswordFailListener {
    private var mMainBinder: MainBinder? = null
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var mMenuAdapter: DrawerItemAdapter
    private lateinit var mPasswordAdapter: PasswordListAdapter
    private var mGroupList = mutableListOf<Group>()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    private var mGroupName: String by Delegates.observable("") { prop, old, new ->
        if (new != "") {
            if (new != old) {
                PreferencesUtils.putString(this@MainActivity, Config.LAST_GROUP, new)
                title = new
            }
        } else {
            title = getString(R.string.app_name)
        }
    }
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            // 注册分组变化监听器
            mMainBinder?.registerOnGroupChangeListener(this@MainActivity)
            // 注册密码变化监听器
            mMainBinder?.registerOnPasswordChangeListener(this@MainActivity)
            // 注册读取 / 更新 / 写入密码失败监听器
            mMainBinder?.registerOnPasswordFailListener(this@MainActivity)

            initData()
        }
    }

    private val mActionListener = object : PasswordListAdapter.OnPasswordActionListener {
        override fun onCopy(password: Password) {
            val items = arrayOf<String>(getString(R.string.copy_username),
                getString(R.string.copy_password),
                getString(R.string.copy_email),
                getString(R.string.copy_note))
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setItems(items) { dialog, which ->
                when (which) {
                    0 -> {
                        // 复制用户名
                        copyText(this@MainActivity, password.username)
                        AppToast.showToast(R.string.copy_username_done)
                    }
                    1 -> {
                        // 复制密码
                        copyText(this@MainActivity, password.password)
                        AppToast.showToast(R.string.copy_password_done)
                    }
                    2 -> {
                        // 复制邮箱
                        copyText(this@MainActivity, password.email)
                        AppToast.showToast(R.string.copy_email_done)
                    }
                    3 -> {
                        // 复制备注
                        copyText(this@MainActivity, password.note)
                        AppToast.showToast(R.string.copy_note_done)
                    }
                }
            }
            builder.create().show()
        }

        override fun onDelete(password: Password) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(password.title)
            builder.setMessage(R.string.alert_delete_message)
            builder.setNegativeButton(R.string.delete) { dialog, which ->
                mMainBinder?.deletePassword(password)
            }
            builder.setPositiveButton(R.string.cancel, null)
            builder.create().show()
        }

        override fun onEdit(password: Password) {
            val intent = EditActivity.getIntent(this@MainActivity)
            intent.putExtra(EditActivity.ID, password.id)
            intent.putExtra(EditActivity.PASSWORD_GROUP, password.groupName)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->

        }
        mProgressDialog = ProgressDialog(this@MainActivity)
        mProgressDialog.setMessage(getString(R.string.reading_passwords))
        mProgressDialog.setCanceledOnTouchOutside(false)

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onResume() {
        super.onResume()
        if (keyLost()) {
            restartApp()
        }
    }

    private fun initData() {
        // 显示 ProgressDialog
        mProgressDialog.show()
        setupDrawer()

        mPasswordAdapter = PasswordListAdapter(this)
        rv_password.adapter = mPasswordAdapter
        mPasswordAdapter.setOnPasswordActionListener(mActionListener)
        // 得到上次选中的分组名称
        mGroupName = PreferencesUtils.getString(this, Config.LAST_GROUP, getString(R.string.group_default))
        if (mGroupName == getString(R.string.group_all)) {
            mMainBinder?.getPasswords(this, null)
        } else {
            mMainBinder?.getPasswords(this, mGroupName)
        }
    }

    private fun setupDrawer() {
        mMenuAdapter = DrawerItemAdapter(this)
        rv_left_menu.layoutManager = LinearLayoutManager(this)
        mMenuAdapter.setOnItemClickListener(DrawerItemClickListener())
        rv_left_menu.adapter = mMenuAdapter
        mMainBinder?.getAllGroups(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                // 添加密码
                val intent = EditActivity.getIntent(this)
                intent.putExtra(EditActivity.PASSWORD_GROUP, mGroupName)
                startActivity(intent)
                return true
            }
            R.id.action_modify_main -> {
                // 修改主密码
                startActivity(CreateKeyOptionsActivity.getIntent(this, CreateKeyOptionsActivity.MODE_UPDATE))
            }
            R.id.action_export -> {
                // 导出密码
                startActivity(ExportActivity.getIntent(this))
            }
            R.id.action_import -> {
                // 导入密码
                startActivity(ImportActivity.getIntent(this))
            }
            R.id.action_distinct -> {
                // 密码去重
            }
            R.id.action_about -> {
                // 关于
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class DrawerItemClickListener : DrawerItemAdapter.OnItemClickListener {
        override fun onGroupItemClick(groupDrawerItem: GroupDrawerItem) {
            // 切换分组
            mGroupName = groupDrawerItem.title
            if (mGroupName == getString(R.string.group_all)) {
                mMainBinder?.getPasswords(this@MainActivity, null)
            } else {
                mMainBinder?.getPasswords(this@MainActivity, mGroupName)
            }
            mProgressDialog.show()
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        override fun onGroupItemLongClick(groupDrawerItem: GroupDrawerItem) {
            val title = groupDrawerItem.title
            if (title == getString(R.string.group_all)) {
                // 所有密码
                AppToast.showToast(R.string.can_not_change_this_group)
            } else {
                val builder = AlertDialog.Builder(this@MainActivity)
                val items = arrayOf(getString(R.string.update_group_name), getString(R.string.merge_group), getString(R.string.delete_group))
                builder.setItems(items) { dialog, which ->
                    when (which) {
                        0 -> {
                            // 修改分组名
                            updateGroupName(title)
                        }
                        1 -> {
                            // 合并分组到
                            mergeGroup(title)
                        }
                        2 -> {
                            // 删除分组
                            deleteGroup(title)
                        }
                    }
                }
                builder.create().show()
            }
        }

        override fun onOperationItemClick(groupDrawerItem: OperationDrawerItem) {
            val title = groupDrawerItem.title
            if (title == getString(R.string.group_add)) {
                // 添加分组
                val builder = AlertDialog.Builder(this@MainActivity)
                val view = layoutInflater.inflate(R.layout.dialog_add_group, null, false)
                builder.setView(view)
                val etGroup = view.findViewById<EditText>(R.id.et_add_group)
                builder.setTitle(R.string.group_add)
                builder.setPositiveButton(R.string.ok, null)
                builder.setNegativeButton(R.string.cancel, null)
                val dialog = builder.create()
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val groupName = etGroup.text.toString()
                    if (groupName.isEmpty()) {
                        AppToast.showToast(R.string.group_name_can_not_be_empty)
                    } else if (groupName == getString(R.string.group_all) || mGroupList.contains(Group(groupName))) {
                        AppToast.showToast(R.string.group_exists)
                    } else {
                        mMainBinder?.insertGroup(Group(groupName))
                        dialog.dismiss()
                    }
                }
            }
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * 合并分组到
     * @param groupName 分组名
     */
    private fun mergeGroup(groupName: String) {
        if (mGroupList.size <= 1) {
            AppToast.showToast(R.string.no_group_can_be_merge)
            return
        }
        // 可以合并到的分组
        val items = mGroupList
            .filter { it.name != groupName }
            .map { it.name }

        val mergeBuilder = AlertDialog.Builder(this)
        mergeBuilder.setItems(items.toTypedArray()) { dialog, which ->
            val newGroupName = items[which]
            mMainBinder?.updateGroupName(groupName, newGroupName, true)
        }
        mergeBuilder.create().show()
    }

    /**
     * 删除分组
     *
     * @param groupName 分组名称
     */
    private fun deleteGroup(groupName: String) {
        val delBuilder = AlertDialog.Builder(this)
        delBuilder.setTitle(R.string.delete_group)
        delBuilder.setMessage(getString(R.string.delete_group_message, title))
        delBuilder.setPositiveButton(R.string.cancel, null)
        delBuilder.setNegativeButton(R.string.delete) { dialog, which ->
            mMainBinder?.deleteGroup(groupName)
        }
        delBuilder.create().show()
    }

    /**
     * 更新分组名
     * @param oldName 原分组名称
     */
    private fun updateGroupName(oldName: String) {
        val updateBuilder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_update_group, null, false)
        updateBuilder.setView(view)
        val etGroup = view.findViewById<EditText>(R.id.et_update_group)
        etGroup.setText(title)
        etGroup.setSelection(title.length)
        updateBuilder.setTitle(R.string.update_group_name)
        updateBuilder.setPositiveButton(R.string.ok, null)
        updateBuilder.setNegativeButton(R.string.cancel, null)
        val updateDialog = updateBuilder.create()
        updateDialog.show()
        updateDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val groupName = etGroup.text.toString()
            if (groupName == getString(R.string.group_all) || mGroupList.contains(Group(groupName))) {
                AppToast.showToast(R.string.group_exists)
            } else {
                mMainBinder?.updateGroupName(oldName, groupName, false)
                updateDialog.dismiss()
            }
        }
    }

    override fun onGetAllGroups(groups: List<Group>) {
        mMenuAdapter.initData(groups)
        mGroupList = groups.toMutableList()
    }

    override fun onGetPasswords(groupName: String?, passwords: List<Password>) {
        mProgressDialog.dismiss()
        mPasswordAdapter.setData(passwords.toMutableList())
    }

    override fun onNewPassword(password: Password) {
        AppToast.showToast(getString(R.string.password_added, password.title))
        if (password.groupName == mGroupName || mGroupName == getString(R.string.group_all)) {
            mPasswordAdapter.addData(password)
        }
    }

    override fun onDeletePassword(password: Password) {
        mPasswordAdapter.delData(password)
    }

    override fun onUpdatePassword(newPassword: Password) {
        AppToast.showToast(getString(R.string.password_updated, newPassword.title))
        mPasswordAdapter.updateData(newPassword, mGroupName)
    }

    override fun onNewGroup(group: Group) {
        mGroupList.add(group)
        mMenuAdapter.addData(group)
        AppToast.showToast(getString(R.string.group_added, group.name))
        showGroup(group.name)
    }

    override fun onDeleteGroup(groupName: String) {
        val result = mMenuAdapter.delData(groupName)
        if (result) {
            mGroupList.remove(Group(groupName))
            if (mGroupName == groupName) {
                showGroup(getString(R.string.group_all))
            }
        }
    }

    override fun onUpdateGroupName(oldGroupName: String, newGroupName: String, merge: Boolean) {
        if (merge) {
            // 合并分组（删除旧分组）
            mMenuAdapter.delData(oldGroupName)
            mGroupList.remove(Group(oldGroupName))
            AppToast.showToast(getString(R.string.group_merged, oldGroupName, newGroupName))
        } else {
            // 更新分组名称
            mMenuAdapter.updateData(oldGroupName, newGroupName)
            mGroupList[mGroupList.indexOf(Group(oldGroupName))].name = newGroupName
            AppToast.showToast(getString(R.string.group_name_has_been_update, newGroupName))
        }

        // 如果是当前选中的分组名称变了，重新加载密码列表
        if (mGroupName == oldGroupName || mGroupName == newGroupName) {
            showGroup(newGroupName)
        }
    }

    /**
     * 显示某个分组密码
     * @param groupName 分组名称
     */
    private fun showGroup(groupName: String) {
        mGroupName = groupName
        if (mGroupName == getString(R.string.group_all)) {
            mMainBinder?.getPasswords(this, null)
        } else {
            mMainBinder?.getPasswords(this, mGroupName)
        }
        mProgressDialog.isShowing
        mProgressDialog.show()
    }

    override fun onInsertFail() {
        AppToast.showToast(R.string.insert_password_fail)
    }

    override fun onReadFail() {
        AppToast.showToast(R.string.read_password_fail)
    }

    override fun onKeyLose() {
        restartApp()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
