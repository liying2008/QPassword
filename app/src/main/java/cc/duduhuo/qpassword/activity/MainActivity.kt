package cc.duduhuo.qpassword.activity

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import cc.duduhuo.qpassword.service.listener.OnGetAllGroupsListener
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener
import cc.duduhuo.qpassword.service.listener.OnGroupChangeListener
import cc.duduhuo.qpassword.service.listener.OnPasswordChangeListener
import cc.duduhuo.qpassword.util.PreferencesUtils
import cc.duduhuo.qpassword.util.copyText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.properties.Delegates


class MainActivity : BaseActivity(), OnGetPasswordsListener, OnPasswordChangeListener, OnGroupChangeListener, OnGetAllGroupsListener {
    private lateinit var mMenuAdapter: DrawerItemAdapter
    private lateinit var mPasswordAdapter: PasswordListAdapter
    private lateinit var mGroupList: List<Group>
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

            initData()
        }
    }

    private val mActionListener = object : PasswordListAdapter.OnPasswordActionListener {
        override fun onCopy(password: Password) {
            val items = arrayOf<String>(getString(R.string.copy_username),
                getString(R.string.copy_password),
                getString(R.string.copy_email))
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
        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun initData() {
        // 显示 ProgressBar
        pb.visibility = View.VISIBLE
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
                val intent = EditActivity.getIntent(this)
                // TODO
                intent.putExtra(EditActivity.PASSWORD_GROUP, mGroupName)
                startActivity(intent)
                return true
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
            pb.visibility = View.VISIBLE
            drawer_layout.closeDrawer(GravityCompat.START)
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
                    if (groupName == getString(R.string.group_all) || mGroupList.contains(Group(groupName))) {
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

    override fun onGetAllGroups(groups: List<Group>) {
        mMenuAdapter.initData(groups)
        mGroupList = groups
    }

    override fun onGetPasswords(groupName: String?, passwords: List<Password>) {
        pb.visibility = View.GONE
        mPasswordAdapter.setData(passwords.toMutableList())
    }

    override fun onNewPassword(password: Password) {
        if (password.groupName == mGroupName) {
            mPasswordAdapter.addData(password)
        } else if (mGroupName == getString(R.string.group_all)) {
            // 所有密码（组）
            // todo
            mPasswordAdapter.addData(password)
        }
    }

    override fun onDeletePassword(password: Password) {
        mPasswordAdapter.delData(password)
    }

    override fun onUpdatePassword(newPassword: Password) {
        mPasswordAdapter.updateData(newPassword)
        // todo
    }

    override fun onNewGroup(group: Group) {
        mMenuAdapter.addData(group)
    }

    override fun onDeleteGroup(groupName: String) {
        mMenuAdapter.delData(groupName)
    }

    override fun onUpdateGroupName(oldGroupName: String, newGroupName: String, merge: Boolean) {
        if (merge) {
            // 合并分组（删除旧分组）
            mMenuAdapter.delData(oldGroupName)
        } else {
            // 更新分组名称
            mMenuAdapter.updateData(oldGroupName, newGroupName)
        }

        // 如果是当前选中的分组名称变了，重新加载密码列表
        if (mGroupName == oldGroupName || mGroupName == newGroupName) {
            mGroupName = newGroupName
            mMainBinder?.getPasswords(this, newGroupName)
            pb.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
