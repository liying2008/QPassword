package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnGetAllGroupsListener
import cc.duduhuo.qpassword.service.listener.OnGetPasswordListener
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener
import kotlinx.android.synthetic.main.activity_edit.*
import java.util.*


class EditActivity : BaseActivity(), OnGetPasswordListener, OnGetPasswordsListener, OnGetAllGroupsListener {
    private var mMainBinder: MainBinder? = null
    /** 当前模式，默认增加 */
    private var mMode = MODE_ADD
    /** 修改密码的ID */
    private var mId: Long = 0

    private lateinit var mPasswordGroup: String

    companion object {
        /** 传入参数 密码 ID */
        const val ID = "password_id"
        /** 传入参数 密码分组 */
        const val PASSWORD_GROUP = "password_group"
        /** 添加模式 */
        private const val MODE_ADD = 0
        /** 修改模式 */
        private const val MODE_MODIFY = 1

        fun getIntent(context: Context): Intent {
            return Intent(context, EditActivity::class.java)
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMode == MODE_MODIFY) {
                mMainBinder?.getPassword(this@EditActivity, mId)
            }
            // 获得所有密码、用户名，用于自动完成
            mMainBinder?.getPasswords(this@EditActivity)
            mMainBinder?.getAllGroups(this@EditActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        mId = intent.getLongExtra(ID, -1L)
        mMode = if (mId == -1L) {
            MODE_ADD
        } else {
            MODE_MODIFY
        }
        mPasswordGroup = intent.getStringExtra(PASSWORD_GROUP)
        if (mPasswordGroup == "") {
            mPasswordGroup = getString(R.string.group_default)
        }
        initView()
        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)

    }

    private fun initView() {
        val actionbar = supportActionBar
        actionbar?.setDisplayShowTitleEnabled(true)
        actionbar?.setDisplayHomeAsUpEnabled(true)
        if (mMode == MODE_ADD) {
            actionbar?.setTitle(R.string.title_add_password)
        } else {
            actionbar?.setTitle(R.string.title_edit_password)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_password, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                if (mMainBinder != null) {
                    savePassword()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePassword() {
        if (et_title.text.toString().trim() == "") {
            AppToast.showToast(R.string.title_can_not_be_empty)
        } else {
            val password = Password()
            password.title = et_title.text.toString().trim()
            password.username = et_username.text.toString().trim()
            password.password = et_password.text.toString().trim()
            password.email = et_email.text.toString().trim()
            password.note = et_note.text.toString().trim()
            password.isTop = cb_is_top.isChecked
            password.groupName = mPasswordGroup
            if (mMode == MODE_ADD) {
                // 添加
                password.createDate = System.currentTimeMillis()
                mMainBinder?.insertPassword(password)
            } else {
                // 修改密码
                password.id = mId
                mMainBinder?.updatePassword(password)
            }
            finish()
        }
    }

    override fun onGetPassword(password: Password?) {
        if (password == null) {
            AppToast.showToast(R.string.password_has_been_deleted)
            finish()
            return
        }

        et_title.setText(password.title)
        et_username.setText(password.username)
        et_password.setText(password.password)
        mPasswordGroup = password.groupName
        et_email.setText(password.email)
        et_note.setText(password.note)
        cb_is_top.isChecked = password.isTop
        et_title.setSelection(et_title.text.length)   // 定位光标在最尾端
    }

    override fun onGetPasswords(groupName: String?, passwords: List<Password>) {
        // Set去掉重复
        val set = hashSetOf<String>()
        passwords.map {
            set.add(it.username)
            set.add(it.password)
            set.add(it.email)
        }

        // 自动完成
        val id = R.layout.simple_dropdown
        val arrayAdapter = ArrayAdapter(this, id, ArrayList(set))
        et_username.setAdapter(arrayAdapter)  // 用户名自动完成适配器
        et_password.setAdapter(arrayAdapter) // 密码自动完成适配器
        et_email.setAdapter(arrayAdapter) // 邮箱自动完成适配器
    }

    override fun onGetAllGroups(groups: List<Group>) {
        val arrays = mutableListOf<String>()
        val size = groups.size
        (0 until size)
            .map { groups[it] }
            .mapTo(arrays) { it.name }

        if (mPasswordGroup == getString(R.string.group_all)) {
            if (size > 0) {
                mPasswordGroup = arrays[0]
            } else {
                mPasswordGroup = getString(R.string.group_default)
                arrays.add(mPasswordGroup)
            }
        } else if (mPasswordGroup == getString(R.string.group_default)) {
            if (!arrays.contains(mPasswordGroup)) {
                arrays.add(mPasswordGroup)
            }
        }
        val positionS = arrays.indexOf(mPasswordGroup)

        val spinnerAdapter = ArrayAdapter(this, R.layout.simple_dropdown, arrays)
        sp_group.adapter = spinnerAdapter
        sp_group.setSelection(positionS)

        sp_group.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPasswordGroup = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unbindService(mServiceConnection)
    }
}
