package cc.duduhuo.qpassword.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnGetAllGroupsListener
import cc.duduhuo.qpassword.service.listener.OnGetPasswordListener
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener
import cc.duduhuo.qpassword.service.listener.OnGroupChangeListener
import kotlinx.android.synthetic.main.activity_edit.*
import java.util.*


class EditActivity : BaseActivity(), OnGetPasswordListener, OnGetPasswordsListener, OnGetAllGroupsListener, OnGroupChangeListener {
    private var mMainBinder: MainBinder? = null
    /** 当前模式，默认增加 */
    private var mMode = MODE_ADD
    /** 修改密码的ID */
    private var mId: Long = 0
    /** 分组名称列表 */
    private var mGroupNameArray = mutableListOf<String>()
    private var mSpinnerAdapter: ArrayAdapter<String>? = null

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

    private val mCreateGroupClick = View.OnClickListener { createGroup() }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            if (mMainBinder != null) {
                mMainBinder!!.unregisterOnGroupChangeListener(this@EditActivity)
                mMainBinder = null
            }
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMainBinder != null) {
                if (mMode == MODE_MODIFY) {
                    mMainBinder!!.getPassword(this@EditActivity, mId)
                }
                // 获得所有密码、用户名，用于自动完成
                mMainBinder!!.getPasswords(this@EditActivity)
                mMainBinder!!.getAllGroups(this@EditActivity)
                mMainBinder!!.registerOnGroupChangeListener(this@EditActivity)
                ib_create_group.setOnClickListener(mCreateGroupClick)
            }
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

    /**
     * 创建新分组
     */
    @SuppressLint("InflateParams")
    private fun createGroup() {
        val builder = AlertDialog.Builder(this)
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
            } else if (groupName == getString(R.string.group_all) || mGroupNameArray.contains(groupName)) {
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
            } else if (groupName == getString(R.string.group_all) || mGroupNameArray.contains(groupName)) {
                AppToast.showToast(R.string.group_exists)
            } else {
                mMainBinder?.insertGroup(Group(groupName))
                dialog.dismiss()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_password, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
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
        val size = groups.size
        (0 until size)
            .map { groups[it] }
            .mapTo(mGroupNameArray) { it.name }

        if (mPasswordGroup == getString(R.string.group_all)) {
            if (size > 0) {
                mPasswordGroup = mGroupNameArray[0]
            } else {
                mPasswordGroup = getString(R.string.group_default)
                mGroupNameArray.add(mPasswordGroup)
            }
        } else if (mPasswordGroup == getString(R.string.group_default)) {
            if (!mGroupNameArray.contains(mPasswordGroup)) {
                mGroupNameArray.add(mPasswordGroup)
            }
        }
        mSpinnerAdapter = ArrayAdapter(this, R.layout.simple_dropdown)
        sp_group.adapter = mSpinnerAdapter
        setSpinnerData(mPasswordGroup, mGroupNameArray)
    }

    /**
     * 设置 Spinner 数据
     * @param currGroup 当前显示的分组名
     * @param arrays 分组列表
     */
    private fun setSpinnerData(currGroup: String, arrays: List<String>) {
        mSpinnerAdapter?.clear()
        mSpinnerAdapter?.addAll(mGroupNameArray)
        val position = arrays.indexOf(currGroup)
        sp_group.setSelection(position)
        sp_group.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPasswordGroup = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onNewGroup(group: Group) {
        mGroupNameArray.add(group.name)
        setSpinnerData(group.name, mGroupNameArray)
    }

    override fun onDeleteGroup(groupName: String) {
        // no op
    }

    override fun onUpdateGroupName(oldGroupName: String, newGroupName: String, merge: Boolean) {
        // no op
    }

    override fun onDestroy() {
        this.unbindService(mServiceConnection)
        super.onDestroy()
    }
}
