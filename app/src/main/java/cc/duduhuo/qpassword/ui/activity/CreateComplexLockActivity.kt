package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnNewKeyListener
import cc.duduhuo.qpassword.util.sha1Hex
import kotlinx.android.synthetic.main.activity_create_complex_lock.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 20:04
 * Description: 创建复杂密码
 * Remarks:
 * =======================================================
 */
class CreateComplexLockActivity : BaseActivity() {
    /** 最小主密码长度 */
    private var mMinKeyLength: Int = 0
    /** 最大主密码长度 */
    private var mMaxKeyLength: Int = 0
    private var mMainBinder: MainBinder? = null

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, CreateComplexLockActivity::class.java)
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
        setContentView(R.layout.activity_create_complex_lock)
        setTitle(R.string.title_create_complex_key)

        mMaxKeyLength = resources.getInteger(R.integer.max_key_length)
        mMinKeyLength = resources.getInteger(R.integer.min_key_length)

        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    private fun initViews() {
        tv_complex_info.text = getString(R.string.please_enter_complex_key, mMinKeyLength, mMaxKeyLength)
        et_complex_lock.addTextChangedListener(PasswordTextWatcher())
        // 清空输入
        iv_clear_btn.setOnClickListener {
            et_complex_lock.setText("")
            iv_clear_btn.visibility = View.GONE
        }

    }

    /**
     * 确认主密码
     * @param view
     */
    fun ok(view: View) {
        val oriKey = et_complex_lock.text.toString()
        if (oriKey.length < mMinKeyLength) {
            AppToast.showToast(getString(R.string.key_length_can_not_too_short, mMinKeyLength))
        } else {
            btn_ok.isEnabled = false
            mMainBinder?.insertKey(Key(oriKey.sha1Hex(), Key.MODE_COMPLEX), object : OnNewKeyListener {
                override fun onNewKey(key: Key) {
                    Config.mKey = key
                    Config.mOriKey = oriKey
                    startActivity(MainActivity.getIntent(this@CreateComplexLockActivity))
                    finish()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }

    inner class PasswordTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // no op
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // no op
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.isEmpty()) {
                iv_clear_btn.visibility = View.GONE
            } else {
                iv_clear_btn.visibility = View.VISIBLE
            }
        }
    }

}
