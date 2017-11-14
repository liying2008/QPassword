package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.NumberGridAdapter
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnNewKeyListener
import cc.duduhuo.qpassword.util.sha1Hex
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
    /** 最小主密码长度 */
    private var mMinKeyLength: Int = 0
    /** 最大主密码长度 */
    private var mMaxKeyLength: Int = 0
    private var mKey: String = ""
    private var mMainBinder: MainBinder? = null
    private var mAdapter: NumberGridAdapter? = null

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, CreateNumberLockActivity::class.java)
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
        setTitle(R.string.title_create_number_key)

        mMaxKeyLength = resources.getInteger(R.integer.max_key_length)
        mMinKeyLength = resources.getInteger(R.integer.min_key_length)

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    private fun initViews() {
        tv_number_info.text = getString(R.string.please_enter_number_key, mMinKeyLength, mMaxKeyLength)
        mAdapter = NumberGridAdapter(this)
        rv_number_grid.adapter = mAdapter
        rv_number_grid.layoutManager = GridLayoutManager(this, 3)
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
            AppToast.showToast(getString(R.string.key_max_length_tip, mMaxKeyLength))
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
            AppToast.showToast(getString(R.string.key_length_can_not_too_short, mMinKeyLength))
        } else {
            view.isEnabled = false
            mMainBinder?.insertKey(Key(mKey.sha1Hex(), Key.MODE_NUMBER), object : OnNewKeyListener {
                override fun onNewKey(key: Key) {
                    Config.mKey = key
                    Config.mOriKey = mKey
                    startActivity(MainActivity.getIntent(this@CreateNumberLockActivity))
                    finish()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
