package cc.duduhuo.qpassword.activity

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.DrawerItemAdapter
import cc.duduhuo.qpassword.adapter.PasswordListAdapter
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.db.GroupService
import cc.duduhuo.qpassword.model.DrawerItemNormal
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener
import cc.duduhuo.qpassword.util.PreferencesUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : BaseActivity(), OnGetPasswordsListener {

    private lateinit var mMenuAdapter: DrawerItemAdapter
    private lateinit var mPasswordAdapter: PasswordListAdapter
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            initData()
        }
    }

    private fun initData() {
        mPasswordAdapter = PasswordListAdapter(this)
        rv_password.adapter = mPasswordAdapter
        // 得到上次选中的分组名称
        val lastGroup = PreferencesUtils.getString(this, Config.LAST_GROUP, getString(R.string.group_default))
        mMainBinder?.getPasswords(this, lastGroup)
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

        setupDrawer()
        initGroupData()

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun initGroupData() {
        val groupService = GroupService(this)
        val groups = groupService.getAllPasswordGroup()
        mMenuAdapter.initData(groups)
    }

    private fun setupDrawer() {
        mMenuAdapter = DrawerItemAdapter(this)
        rv_left_menu.layoutManager = LinearLayoutManager(this)
        mMenuAdapter.setOnItemClickListener(DrawerItemClickListener())
        rv_left_menu.adapter = mMenuAdapter
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
                intent.putExtra(EditActivity.PASSWORD_GROUP, getString(R.string.group_default))
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class DrawerItemClickListener : DrawerItemAdapter.OnItemClickListener {
        override fun onItemClick(drawerItemNormal: DrawerItemNormal) {
            AppToast.showToast(drawerItemNormal.title)
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onGetPasswords(groupName: String?, passwords: List<Password>) {
        mPasswordAdapter.setData(passwords)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
