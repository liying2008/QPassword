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
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.DrawerItemAdapter
import cc.duduhuo.qpassword.adapter.PasswordListAdapter
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.model.DrawerItemNormal
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
    private var mGroupName: String by Delegates.observable("") { prop, old, new ->
        if (new != "") {
            title = new
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
            val build = AlertDialog.Builder(this@MainActivity)
            build.setItems(items) { dialog, which ->
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
            build.create().show()
        }

        override fun onDelete(password: Password) {
            val build = AlertDialog.Builder(this@MainActivity)
            build.setTitle(password.title)
            build.setMessage(R.string.alert_delete_message)
            build.setNegativeButton(R.string.delete) { dialog, which ->
                mMainBinder?.deletePassword(password)
            }
            build.setPositiveButton(R.string.cancel, null)
            build.create().show()
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
        override fun onItemClick(drawerItemNormal: DrawerItemNormal) {
            val title = drawerItemNormal.title
            if (title == getString(R.string.group_add)) {
                // 添加分组
            } else {
                // 切换分组
                mGroupName = title
                PreferencesUtils.putString(this@MainActivity, Config.LAST_GROUP, title)
                if (mGroupName == getString(R.string.group_all)) {
                    mMainBinder?.getPasswords(this@MainActivity, null)
                } else {
                    mMainBinder?.getPasswords(this@MainActivity, mGroupName)
                }
            }
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onGetAllGroups(groups: List<Group>) {
        mMenuAdapter.initData(groups)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
