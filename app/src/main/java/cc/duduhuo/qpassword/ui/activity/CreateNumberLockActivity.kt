package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.NumberGridAdapter
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnKeyChangeListener
import cc.duduhuo.qpassword.service.listener.OnNewKeyListener
import cc.duduhuo.qpassword.util.keyLost
import cc.duduhuo.qpassword.util.sha1Hex
import cc.duduhuo.qpassword.util.showSnackbar
import kotlinx.android.synthetic.main.activity_create_number_lock.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 20:03
 * Description: 创建数字密码
 * Remarks:
 * =======================================================
 */
class CreateNumberLockActivity : BaseActivity(), NumberGridAdapter.OnNumberClickListener {
    private var mMode: Int = MODE_CREATE

    /** 最小主密码长度 */
    private var mMinKeyLength: Int = 0

    /** 最大主密码长度 */
    private var mMaxKeyLength: Int = 0
    private var mKey: String = ""
    private var mMainBinder: MainBinder? = null
    private var mAdapter: NumberGridAdapter? = null

    /** 是否正在更改主密码（期间不允许finish Activity） */
    private var mUpdating = false

    companion object {
        private const val MODE = "mode"
        const val MODE_CREATE = 0
        const val MODE_UPDATE = 1
        fun getIntent(context: Context, mode: Int): Intent {
            val intent = Intent(context, CreateNumberLockActivity::class.java)
            intent.putExtra(MODE, mode)
            return intent
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mMainBinder = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mMainBinder = service as MainBinder
            if (mMainBinder != null) {
                initViews()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_number_lock)
        mMode = intent.getIntExtra(MODE, MODE_CREATE)
        setTitle(R.string.title_create_number_key)
        if (mMode == MODE_UPDATE) {
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        mMaxKeyLength = resources.getInteger(R.integer.max_key_length)
        mMinKeyLength = resources.getInteger(R.integer.min_key_length)

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        if (mMode == MODE_UPDATE) {
            if (keyLost()) {
                restartApp()
            }
        }
    }

    private fun initViews() {
        tv_number_info.text = getString(R.string.please_enter_number_key, mMinKeyLength, mMaxKeyLength)
        mAdapter = NumberGridAdapter(this)
        rv_number_grid.adapter = mAdapter
        rv_number_grid.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        mAdapter!!.setOnNumberClickListener(this)
        // 清空输入
        iv_clear_btn.setOnClickListener {
            mKey = ""
            tv_number_lock.text = ""
            iv_clear_btn.visibility = View.GONE
        }
    }

    override fun onClickNum(number: String, view: TextView) {
        iv_clear_btn.visibility = View.VISIBLE
        if (mKey.length < mMaxKeyLength) {
            mKey += number
            tv_number_lock.text = mKey
        } else {
            showSnackbar(main_layout, getString(R.string.key_max_length_tip, mMaxKeyLength))
        }
    }

    override fun onClickDel(view: TextView) {
        if (mKey.isNotEmpty()) {
            mKey = mKey.substring(0, mKey.length - 1)
            tv_number_lock.text = mKey

            if (mKey.isEmpty()) {
                iv_clear_btn.visibility = View.GONE
            }
        }
    }

    override fun onClickOk(view: TextView) {
        if (mKey.length < mMinKeyLength) {
            showSnackbar(
                main_layout,
                getString(R.string.key_length_can_not_too_short, mMinKeyLength)
            )
        } else {
            view.isEnabled = false
            view.setTextColor(resources.getColorStateList(R.color.disable_text_color))
            if (mMode == MODE_CREATE) {
                mMainBinder?.insertKey(
                    Key(mKey.sha1Hex(), Key.MODE_NUMBER),
                    object : OnNewKeyListener {
                        override fun onNewKey(key: Key) {
                            Config.mKey = key
                            Config.mOriKey = mKey
                            startActivity(MainActivity.getIntent(this@CreateNumberLockActivity))
                            finish()
                        }
                    })
            } else if (mMode == MODE_UPDATE) {
                AppToast.showToast(R.string.applying_key_changes)
                mMainBinder?.updateKey(
                    Config.mKey!!,
                    Config.mOriKey,
                    Key(mKey.sha1Hex(), Key.MODE_NUMBER),
                    mKey,
                    mOnKeyChangeListener
                )
                mUpdating = true
            }
        }
    }

    private val mOnKeyChangeListener = object : OnKeyChangeListener {
        override fun onUpdateKey(oldKey: Key, newKey: Key) {
            Config.mKey = newKey
            Config.mOriKey = mKey
            if (oldKey.key != newKey.key || oldKey.mode != newKey.mode) {
                AppToast.showToast(R.string.key_updated)
                destroyAllActivities()
                Config.mIsAllFinishing = false
                startActivity(MainActivity.getIntent(this@CreateNumberLockActivity))
            } else {
                finish()
            }
            mUpdating = false
        }
    }

    /**
     * 点击ActionBar返回图标回到上一个Activity
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (mUpdating) {
                AppToast.showToast(R.string.updating_please_wait)
            } else {
                finish()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (mUpdating) {
            AppToast.showToast(R.string.updating_please_wait)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        unbindService(mServiceConnection)
        super.onDestroy()
    }
}
