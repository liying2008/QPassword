package cc.duduhuo.qpassword.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.util.sha1Hex
import cc.duduhuo.qpassword.util.showSnackbar
import kotlinx.android.synthetic.main.activity_complex_lock.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 22:42
 * Description: 复杂密码解锁界面
 * Remarks:
 * =======================================================
 */
class ComplexLockActivity : BaseActivity() {
    /** 最小主密码长度 */
    private var mMinKeyLength: Int = 0
    /** 最大主密码长度 */
    private var mMaxKeyLength: Int = 0
    /** 实际的主密码（SHA-1加密后） */
    private lateinit var mRealKey: String
    /** 输错主密码的次数 */
    private var mWrongCount: Int = 0

    companion object {
        private const val INTENT_KEY = "key"
        fun getIntent(context: Context, key: String): Intent {
            val intent = Intent(context, ComplexLockActivity::class.java)
            intent.putExtra(INTENT_KEY, key)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complex_lock)

        mRealKey = intent.getStringExtra(INTENT_KEY)!!

        mMaxKeyLength = resources.getInteger(R.integer.max_key_length)
        mMinKeyLength = resources.getInteger(R.integer.min_key_length)

        initViews()
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

    fun ok(view: View) {
        val oriKey = et_complex_lock.text.toString()
        if (oriKey.length < mMinKeyLength) {
            showSnackbar(view, getString(R.string.key_length_can_not_too_short, mMinKeyLength))
        } else {
            if (oriKey.sha1Hex() == mRealKey) {
                Config.mOriKey = oriKey
                startActivity(MainActivity.getIntent(this@ComplexLockActivity))
                finish()
            } else {
                mWrongCount++
                if (mWrongCount >= 5) {
                    view.isEnabled = false
                    showSnackbar(view, getString(R.string.key_wrong_try_again_later, 30))
                    countDown(view)
                } else {
                    tv_complex_info.text = getString(R.string.key_wrong_try_again, 5 - mWrongCount)
                }

            }
        }

    }

    /**
     * 30s倒计时
     */
    private fun countDown(view: View) {
        object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                tv_complex_info.text = getString(R.string.please_try_again_later, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                tv_complex_info.text = getString(R.string.please_enter_complex_key, mMinKeyLength, mMaxKeyLength)
                view.isEnabled = true
                mWrongCount = 0
            }
        }.start()
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
