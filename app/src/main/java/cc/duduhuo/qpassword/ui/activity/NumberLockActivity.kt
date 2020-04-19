package cc.duduhuo.qpassword.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.adapter.NumberGridAdapter
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.util.sha1Hex
import kotlinx.android.synthetic.main.activity_create_number_lock.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 22:42
 * Description: 数字密码解锁界面
 * Remarks:
 * =======================================================
 */
class NumberLockActivity : BaseActivity(), NumberGridAdapter.OnNumberClickListener {
    /** 最小主密码长度 */
    private var mMinKeyLength: Int = 0
    /** 最大主密码长度 */
    private var mMaxKeyLength: Int = 0
    /** 用户输入的密码 */
    private var mKey: String = ""
    /** 显示的内容（*） */
    private var mAsterisk: String = ""
    /** 实际的主密码（SHA-1加密后） */
    private lateinit var mRealKey: String
    /** 输错主密码的次数 */
    private var mWrongCount: Int = 0
    private var mAdapter: NumberGridAdapter? = null

    companion object {
        private const val INTENT_KEY = "key"
        fun getIntent(context: Context, key: String): Intent {
            val intent = Intent(context, NumberLockActivity::class.java)
            intent.putExtra(INTENT_KEY, key)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_lock)
        mRealKey = intent.getStringExtra(INTENT_KEY)!!

        mMaxKeyLength = resources.getInteger(R.integer.max_key_length)
        mMinKeyLength = resources.getInteger(R.integer.min_key_length)

        initViews()
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
            mAsterisk = ""
            tv_number_lock.text = ""
            iv_clear_btn.visibility = View.GONE
        }
    }

    override fun onClickNum(number: String, view: TextView) {
        iv_clear_btn.visibility = View.VISIBLE
        if (mKey.length < mMaxKeyLength) {
            mKey += number
            mAsterisk += "●"
            tv_number_lock.text = mAsterisk
        } else {
            AppToast.showToast(getString(R.string.key_max_length_tip, mMaxKeyLength))
        }
    }

    override fun onClickDel(view: TextView) {
        if (mKey.isNotEmpty()) {
            mKey = mKey.substring(0, mKey.length - 1)
            mAsterisk = mAsterisk.substring(0, mAsterisk.length - 1)
            tv_number_lock.text = mAsterisk

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
            view.setTextColor(resources.getColorStateList(R.color.disable_text_color))
            if (mKey.sha1Hex() == mRealKey) {
                Config.mOriKey = mKey
                startActivity(MainActivity.getIntent(this@NumberLockActivity))
                finish()
            } else {
                mWrongCount++
                if (mWrongCount >= 5) {
                    view.isEnabled = false
                    view.setTextColor(resources.getColorStateList(R.color.disable_text_color))
                    AppToast.showToast(getString(R.string.key_wrong_try_again_later, 30))
                    countDown(view)
                } else {
                    view.isEnabled = true
                    view.setTextColor(resources.getColorStateList(android.R.color.black))
                    tv_number_info.text = getString(R.string.key_wrong_try_again, 5 - mWrongCount)
                }
            }
        }
    }

    /**
     * 30s倒计时
     */
    private fun countDown(view: TextView) {
        object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                tv_number_info.text = getString(R.string.please_try_again_later, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                tv_number_info.text = getString(R.string.please_enter_number_key, mMinKeyLength, mMaxKeyLength)
                view.isEnabled = true
                mWrongCount = 0
            }
        }.start()
    }
}
