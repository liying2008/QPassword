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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.properties.Delegates


class MainActivity : BaseActivity(), OnGetPasswordsListener, OnPasswordChangeListener, OnGroupChangeListener, OnGetAllGroupsListener, OnPasswordFailListener {
    /** 当前是否是搜索模式 */
    private var mSearchMode = false
    /** 搜索关键词 */
    private var mSearchKeyword = ""
    private var mMainBinder: MainBinder? = null
    private lateinit var mProgressDialog: ProgressDialog
    /** 左侧抽屉列表适配器 */
    private lateinit var mMenuAdapter: DrawerItemAdapter
    /** 密码列表适配器 */
    private lateinit var mPasswordAdapter: PasswordListAdapter
    /** 密码分组列表 */
    private var mGroupList = mutableListOf<Group>()

    companion object {
        private const val REQUEST_CODE_IMPORT = 0x0000

        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    /** 执行搜索前，所在的分组 */
    private var mBeforeSearchGroupName = ""
    /** 当前的分组名称 */
    private var mGroupName: String by Delegates.observable("") { prop, old, new ->
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
            // 搜索密码
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.search_passwords)
            val searchView = layoutInflater.inflate(R.layout.dialog_search_password, null, false)
            builder.setView(searchView)
            val etSearchKeyword = searchView.findViewById<EditText>(R.id.et_search_keyword)
            builder.setPositiveButton(R.string.search, null)
            val dialog = builder.create()
            dialog.show()
            // 弹出输入法面板
            dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

            etSearchKeyword.setOnEditorActionListener { v, actionId, event ->
                val keyword = etSearchKeyword.text.toString().trim()
                if (keyword.isEmpty()) {
                    AppToast.showToast(R.string.search_keyword_can_not_be_empty)
                } else {
                    searchPasswords(keyword)
                    dialog.dismiss()
                }
                return@setOnEditorActionListener true
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val keyword = etSearchKeyword.text.toString().trim()
                if (keyword.isEmpty()) {
                    AppToast.showToast(R.string.search_keyword_can_not_be_empty)
                } else {
                    searchPasswords(keyword)
                    dialog.dismiss()
                }
            }
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
                    AppToast.showToast(R.string.press_again_to_exit)
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
                startActivityForResult(ImportActivity.getIntent(this), REQUEST_CODE_IMPORT)
            }
            R.id.action_distinct -> {
                // 密码去重
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.action_distinct)
                builder.setMessage(R.string.distinct_tip)
                builder.setNegativeButton(R.string.cancel, null)
                builder.setPositiveButton(R.string.start_distinct) { dialog, which ->
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
                builder.setNegativeButton(R.string.delete_passwords) { dialog, which ->
                    mGroupList.forEach {
                        mMainBinder?.deleteGroup(it.name)
                    }

                }
                builder.setPositiveButton(R.string.export_passwords) { dialog, which ->
                    startActivity(ExportActivity.getIntent(this))
                }
                builder.create().show()
            }
            R.id.action_about -> {
                // 关于
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
                // 弹出输入法面板
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                etGroup.setOnEditorActionListener { v, actionId, event ->
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
        etGroup.setText(oldName)
        etGroup.setSelection(oldName.length)
        updateBuilder.setTitle(R.string.update_group_name)
        updateBuilder.setPositiveButton(R.string.ok, null)
        updateBuilder.setNegativeButton(R.string.cancel, null)
        val updateDialog = updateBuilder.create()
        updateDialog.show()
        // 弹出输入法面板
        updateDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        etGroup.setOnEditorActionListener { v, actionId, event ->
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
        mProgressDialog.dismiss()
        if (mSearchMode) {
            val resultPassword = passwords.filter { it.title.toLowerCase().contains(mSearchKeyword.toLowerCase()) || it.note.toLowerCase().contains(mSearchKeyword.toLowerCase()) }
            mPasswordAdapter.setData(resultPassword.toMutableList())
            mGroupName = getString(R.string.search_result)
            val size = resultPassword.size
            mMenuAdapter.updateHeader(getString(R.string.search_result), size)
            if (size == 0) {
                AppToast.showToast(R.string.search_result_is_empty)
            }
        } else {
            mGroupName = groupName ?: getString(R.string.group_all)
            mPasswordAdapter.setData(passwords.toMutableList())
            mMenuAdapter.updateHeader(groupName, passwords.size)
        }
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
     * 退出应用
     */
    private fun exit() {
        Config.mKey = null
        Config.mOriKey = Config.NO_PASSWORD
        destroyAllActivities()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
