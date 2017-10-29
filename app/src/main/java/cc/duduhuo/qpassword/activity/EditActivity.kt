package cc.duduhuo.qpassword.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import cc.duduhuo.qpassword.R


class EditActivity : BaseActivity() {

    /** 当前模式，默认增加 */
    private var mMode = MODE_ADD
    /** 修改密码的ID */
    private var mId: Int = 0

    private var mPasswordGroup: String? = null

    companion object {
        /** 传入参数 密码 ID */
        val ID = "password_id"
        /** 传入参数 密码分组 */
        val PASSWORD_GROUP = "password_group"
        /** 添加模式 */
        private val MODE_ADD = 0
        /** 修改模式 */
        private val MODE_MODIFY = 1

        fun getIntent(context: Context): Intent {
            return Intent(context, EditActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        mId = intent.getIntExtra(ID, -1)
        mMode = if (mId == -1) {
            MODE_ADD
        } else {
            MODE_MODIFY
        }
        mPasswordGroup = intent.getStringExtra(PASSWORD_GROUP)
        if (mPasswordGroup == null || mPasswordGroup == "") {
            mPasswordGroup = getString(R.string.group_default)
        }
        initView()

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
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_save -> {
                //todo 保存密码
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
