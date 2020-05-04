package cc.duduhuo.qpassword.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
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
import cc.duduhuo.qpassword.ui.dialog.DistinctDialog
import cc.duduhuo.qpassword.util.PreferencesUtils
import cc.duduhuo.qpassword.util.copyText
import cc.duduhuo.qpassword.util.keyLost
import cc.duduhuo.qpassword.util.showSnackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import scut.carson_ho.searchview.SearchFragment
import kotlin.properties.Delegates


class MainActivity : BaseActivity(), OnGetPasswordsListener, OnPasswordChangeListener, OnGroupChangeListener, OnGetAllGroupsListener, OnPasswordFailListener {
    /** 当前是否是搜索模式 */
    private var mSearchMode = false
    /** 搜索关键词 */
    private var mSearchKeyword = ""
    private var mMainBinder: MainBinder? = null
    /** 左侧抽屉列表适配器 */
    private lateinit var mMenuAdapter: DrawerItemAdapter
    /** 密码列表适配器 */
    private lateinit var mPasswordAdapter: PasswordListAdapter
    /** 密码分组列表 */
    private var mGroupList = mutableListOf<Group>()
    /** 搜索控件 */
    private var mSearchFragment: SearchFragment? = null

    companion object {
        private const val REQUEST_CODE_IMPORT = 0x0000

        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    /** 执行搜索前，所在的分组 */
    private var mBeforeSearchGroupName = ""
    /** 当前的分组名称 */
    private var mGroupName: String by Delegates.observable("") { _, old, new ->
        if (new != "") {
            if (new != old) {
                if (new != getString(R.string.search_result)) {
                    PreferencesUtils.putString(this@MainActivity, Config.LAST_GROUP, new)
                }
                if (old != getString(R.string.search_result)) {
                    mBeforeSearchGroupName = old
                }
                title = new
            }
        } else {
            title = getString(R.string.app_name)
        }
    }
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            if (mMainBinder != null) {
                mMainBinder!!.unregisterOnGroupChangeListener(this@MainActivity)
                mMainBinder!!.unregisterOnPasswordChangeListener(this@MainActivity)
                mMainBinder!!.unregisterOnPasswordFailListener(this@MainActivity)
            }
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMainBinder != null) {
                // 注册分组变化监听器
                mMainBinder!!.registerOnGroupChangeListener(this@MainActivity)
                // 注册密码变化监听器
                mMainBinder!!.registerOnPasswordChangeListener(this@MainActivity)
                // 注册读取 / 更新 / 写入密码失败监听器
                mMainBinder!!.registerOnPasswordFailListener(this@MainActivity)

                initData()
            }
        }
    }

    private val mActionListener = object : PasswordListAdapter.OnPasswordActionListener {
        override fun onCopy(password: Password) {
            val items = mutableListOf<String>()
            if (password.username.isNotEmpty()) {
                items.add(getString(R.string.copy_username))
            }
            if (password.password.isNotEmpty()) {
                items.add(getString(R.string.copy_password))
            }
            if (password.email.isNotEmpty()) {
                items.add(getString(R.string.copy_email))
            }
            if (password.note.isNotEmpty()) {
                items.add(getString(R.string.copy_note))
            }
            if (items.isEmpty()) {
                showSnackbar(fab, R.string.nothing_to_copy)
                return
            }

            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setItems(items.toTypedArray()) { _, which ->
                when (items[which]) {
                    getString(R.string.copy_username) -> {
                        // 复制用户名
                        copyText(this@MainActivity, password.username)
                        showSnackbar(fab, R.string.copy_username_done)
                    }
                    getString(R.string.copy_password) -> {
                        // 复制密码
                        copyText(this@MainActivity, password.password)
                        showSnackbar(fab, R.string.copy_password_done)
                    }
                    getString(R.string.copy_email) -> {
                        // 复制邮箱
                        copyText(this@MainActivity, password.email)
                        showSnackbar(fab, R.string.copy_email_done)
                    }
                    getString(R.string.copy_note) -> {
                        // 复制备注
                        copyText(this@MainActivity, password.note)
                        showSnackbar(fab, R.string.copy_note_done)
                    }
                }
            }
            builder.create().show()
        }

        override fun onDelete(password: Password) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(password.title)
            builder.setMessage(R.string.alert_delete_message)
            builder.setNegativeButton(R.string.delete) { _, _ ->
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

        fab.setOnClickListener {
            // 添加密码
            val intent = EditActivity.getIntent(this)
            intent.putExtra(EditActivity.PASSWORD_GROUP, mGroupName)
            startActivity(intent)
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)

        mPasswordAdapter = PasswordListAdapter(this)
        rv_password.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rv_password.adapter = mPasswordAdapter
        mPasswordAdapter.setOnPasswordActionListener(mActionListener)
        // 得到上次选中的分组名称
        mGroupName = PreferencesUtils.getString(this, Config.LAST_GROUP, getString(R.string.group_default))

        mMenuAdapter = DrawerItemAdapter(this)
        rv_left_menu.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        mMenuAdapter.setOnItemClickListener(DrawerItemClickListener())
        rv_left_menu.adapter = mMenuAdapter
    }

    /**
     * 搜索密码
     * @param keyword 搜索关键词
     */
    private fun searchPasswords(keyword: String) {
        // 处于搜索模式
        mSearchMode = true
        mSearchKeyword = keyword
        showGroup(null)
    }

    override fun onResume() {
        super.onResume()
        if (keyLost()) {
            restartApp()
        }
    }

    private fun initData() {
        showProgressBar()

        mMainBinder?.getAllGroups(this)
        if (mGroupName == getString(R.string.group_all)) {
            mMainBinder?.getPasswords(this, null)
        } else {
            mMainBinder?.getPasswords(this, mGroupName)
        }
    }

    /** 上次点击Back键的时间  */
    private var mLastBackKeyTime = 0L

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (mSearchMode) {
                mSearchMode = false
                showGroup(mBeforeSearchGroupName)
            } else {
                val delay = Math.abs(System.currentTimeMillis() - mLastBackKeyTime)
                if (delay > 2000) {
                    showSnackbar(fab, R.string.press_again_to_exit)
                    mLastBackKeyTime = System.currentTimeMillis()
                } else {
                    exit()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                // 搜索密码
                if (mSearchFragment == null) {
                    mSearchFragment = SearchFragment.newInstance()
                    mSearchFragment!!.setOnSearchListener { keyword ->
                        if (keyword.isEmpty()) {
                            AppToast.showToast(R.string.search_keyword_can_not_be_empty)
                        } else {
                            searchPasswords(keyword)
                        }
                    }
                }
                mSearchFragment!!.show(supportFragmentManager, SearchFragment.TAG)
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
                startActivityForResult(ImportActivity.getIntent(this), REQUEST_CODE_IMPORT)
            }
            R.id.action_distinct -> {
                // 密码去重
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.action_distinct)
                builder.setMessage(R.string.distinct_tip)
                builder.setNegativeButton(R.string.cancel, null)
                builder.setPositiveButton(R.string.start_distinct) { _, _ ->
                    val distinctDialog = DistinctDialog(this, mMainBinder!!)
                    distinctDialog.show()
                }
                builder.create().show()
            }
            R.id.action_delete_all -> {
                // 删除所有密码
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.delete_all_password)
                builder.setMessage(R.string.delete_all_password_message)
                builder.setNegativeButton(R.string.delete_passwords) { _, _ ->
                    mGroupList.forEach {
                        mMainBinder?.deleteGroup(it.name)
                    }
                    if (mGroupName == getString(R.string.group_all)) {
                        refreshAll()
                    }
                }
                builder.setPositiveButton(R.string.export_passwords) { _, _ ->
                    startActivity(ExportActivity.getIntent(this))
                }
                builder.create().show()
            }
            R.id.action_about -> {
                // 关于
                startActivity(AboutActivity.getIntent(this))
            }
            R.id.action_exit -> {
                // 退出
                exit()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class DrawerItemClickListener : DrawerItemAdapter.OnItemClickListener {
        override fun onGroupItemClick(groupDrawerItem: GroupDrawerItem) {
            // 切换分组
            mSearchMode = false
            mGroupName = groupDrawerItem.title
            if (mGroupName == getString(R.string.group_all)) {
                mMainBinder?.getPasswords(this@MainActivity, null)
            } else {
                mMainBinder?.getPasswords(this@MainActivity, mGroupName)
            }

            showProgressBar()
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
                builder.setItems(items) { _, which ->
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

        @SuppressLint("InflateParams")
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
                // 弹出输入法面板
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                etGroup.setOnEditorActionListener { _, _, _ ->
                    val groupName = etGroup.text.toString().trim()
                    if (groupName.isEmpty()) {
                        AppToast.showToast(R.string.group_name_can_not_be_empty)
                    } else if (groupName == getString(R.string.group_all) || mGroupList.contains(Group(groupName))) {
                        AppToast.showToast(R.string.group_exists)
                    } else {
                        mMainBinder?.insertGroup(Group(groupName))
                        dialog.dismiss()
                    }
                    return@setOnEditorActionListener true
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val groupName = etGroup.text.toString().trim()
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
        mergeBuilder.setItems(items.toTypedArray()) { _, which ->
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
        delBuilder.setNegativeButton(R.string.delete) { _, _ ->
            mMainBinder?.deleteGroup(groupName)
        }
        delBuilder.create().show()
    }

    /**
     * 更新分组名
     * @param oldName 原分组名称
     */
    @SuppressLint("InflateParams")
    private fun updateGroupName(oldName: String) {
        val updateBuilder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_update_group, null, false)
        updateBuilder.setView(view)
        val etGroup = view.findViewById<EditText>(R.id.et_update_group)
        etGroup.setText(oldName)
        etGroup.setSelection(oldName.length)
        updateBuilder.setTitle(R.string.update_group_name)
        updateBuilder.setPositiveButton(R.string.ok, null)
        updateBuilder.setNegativeButton(R.string.cancel, null)
        val updateDialog = updateBuilder.create()
        updateDialog.show()
        // 弹出输入法面板
        updateDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        etGroup.setOnEditorActionListener { _, _, _ ->
            val groupName = etGroup.text.toString().trim()
            if (groupName == getString(R.string.group_all) || mGroupList.contains(Group(groupName))) {
                AppToast.showToast(R.string.group_exists)
            } else {
                mMainBinder?.updateGroupName(oldName, groupName, false)
                updateDialog.dismiss()
            }
            return@setOnEditorActionListener true
        }
        updateDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val groupName = etGroup.text.toString().trim()
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
        dismissProgressBar()

        if (mSearchMode) {
            val resultPassword = passwords.filter { it.title.toLowerCase().contains(mSearchKeyword.toLowerCase()) || it.note.toLowerCase().contains(mSearchKeyword.toLowerCase()) }
            mPasswordAdapter.setData(resultPassword)
            mGroupName = getString(R.string.search_result)
            val size = resultPassword.size
            mMenuAdapter.updateHeader(getString(R.string.search_result), size)
            if (size == 0) {
                showSnackbar(fab, R.string.search_result_is_empty)
            }
        } else {
            mGroupName = groupName ?: getString(R.string.group_all)
            val size = passwords.size
            mPasswordAdapter.setData(passwords)
            mMenuAdapter.updateHeader(groupName, size)
            if (size == 0) {
                showSnackbar(fab, R.string.no_passwords)
            }
        }
    }

    override fun onNewPassword(password: Password) {
        if (password.groupName == mGroupName || mGroupName == getString(R.string.group_all)) {
            mPasswordAdapter.addData(password)
        }
        showSnackbar(fab, getString(R.string.new_password_added_to_group, password.title, password.groupName))
    }

    override fun onDeletePassword(password: Password) {
        mPasswordAdapter.delData(password)
    }

    override fun onUpdatePassword(newPassword: Password) {
        val adapterNotEmpty = mPasswordAdapter.updateData(newPassword, mGroupName)
        // 修改密码时更改了密码分组，并且该分组是新创建的时候会执行下面的代码
        if (!adapterNotEmpty && newPassword.groupName == mGroupName) {
            showGroup(mGroupName)
            showSnackbar(fab, getString(R.string.password_added_to_new_group, newPassword.title, mGroupName))
        } else {
            showSnackbar(fab, getString(R.string.password_updated, newPassword.title))
        }
    }

    override fun onNewGroup(group: Group) {
        mGroupList.add(group)
        mMenuAdapter.addData(group)
        AppToast.showToast(getString(R.string.group_added, group.name))
        if (!mSearchMode) {
            showGroup(group.name)
        }
    }

    override fun onDeleteGroup(groupName: String) {
        AppToast.showToast(getString(R.string.group_deleted, groupName))
        val result = mMenuAdapter.delData(groupName)
        if (result) {
            mGroupList.remove(Group(groupName))
            if (mGroupName == groupName) {
                showGroup(null)
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
    private fun showGroup(groupName: String? = null) {
        if (groupName != null && groupName == getString(R.string.group_all)) {
            mMainBinder?.getPasswords(this, null)
        } else {
            mMainBinder?.getPasswords(this, groupName)
        }

        showProgressBar()
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

    /**
     * 刷新整个密码列表
     */
    fun refreshAll() {
        if (mSearchMode) {
            showGroup(null)
        } else {
            showGroup(mGroupName)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMPORT) {
            if (resultCode == ImportActivity.RESULT_CODE_IMPORT) {
                // 重新读取数据库中该分组下的密码数据
                refreshAll()
            }
        }
    }

    /**
     * 显示 ProgressBar
     */
    private fun showProgressBar() {
        if (isFinishing) {
            return
        }
        rv_password.visibility = View.GONE
        layout_no_content.visibility = View.VISIBLE
    }

    /**
     * 取消 ProgressBar
     */
    private fun dismissProgressBar() {
        if (isFinishing) {
            return
        }
        layout_no_content.visibility = View.GONE
        rv_password.visibility = View.VISIBLE
    }

    /**
     * 退出应用
     */
    private fun exit() {
        Config.mKey = null
        Config.mOriKey = Config.NO_PASSWORD
        destroyAllActivities()
    }

    override fun onDestroy() {
        unbindService(mServiceConnection)
        super.onDestroy()
    }
}
