package cc.duduhuo.qpassword.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.widget.TextView
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.util.sha1Hex
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 22:36
 * Description: 图案密码解锁界面
 * Remarks:
 * =======================================================
 */
class PatternLockActivity : BaseActivity() {
    private lateinit var mKey: String
    private lateinit var mPatternLockView: PatternLockView
    private lateinit var mTvInfo: TextView
    private val mHandler = Handler()
    /** 输错主密码的次数 */
    private var mWrongCount: Int = 0

    companion object {
        private const val INTENT_KEY = "key"
        fun getIntent(context: Context, key: String): Intent {
            val intent = Intent(context, PatternLockActivity::class.java)
            intent.putExtra(INTENT_KEY, key)
            return intent
        }
    }

    private val mClearPatternRunnable = Runnable { mPatternLockView.clearPattern() }

    private val mPatternLockViewListener = object : PatternLockViewListener {
        override fun onStarted() {
            mHandler.removeCallbacks(mClearPatternRunnable)
        }

        override fun onProgress(progressPattern: List<PatternLockView.Dot>) {
            // no op
        }

        override fun onComplete(pattern: List<PatternLockView.Dot>) {
            val oriKey = PatternLockUtils.patternToString(mPatternLockView, pattern)
            val key = oriKey.sha1Hex()
            if (pattern.size < 4) {
                mTvInfo.setText(R.string.connect_at_least_4_points)
                mHandler.removeCallbacks(mClearPatternRunnable)
                mHandler.postDelayed(mClearPatternRunnable, 1000)
                mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG)
                return
            }
            if (key == mKey) {
                Config.mOriKey = oriKey
                mTvInfo.setText(R.string.pattern_correct)
                mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT)
                startActivity(MainActivity.getIntent(this@PatternLockActivity))
                finish()
            } else {
                mWrongCount++
                mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG)
                mHandler.removeCallbacks(mClearPatternRunnable)
                mHandler.postDelayed(mClearPatternRunnable, 1000)
                if (mWrongCount >= 5) {
                    mPatternLockView.isInputEnabled = false
                    AppToast.showToast(getString(R.string.pattern_wrong_try_again_later, 30))
                    countDown()
                } else {
                    mTvInfo.text = getString(R.string.pattern_wrong_try_again, 5 - mWrongCount)
                }
            }
        }

        override fun onCleared() {
            // no op
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_lock)
        setTitle(R.string.title_pattern_unlock)
        mKey = intent.getStringExtra(INTENT_KEY)!!
        mPatternLockView = findViewById(R.id.pattern_lock_view)
        mTvInfo = findViewById(R.id.tv_info)
        mPatternLockView.addPatternLockListener(mPatternLockViewListener)
        // 关闭震动反馈
        mPatternLockView.isTactileFeedbackEnabled = false
    }

    /**
     * 30s倒计时
     */
    private fun countDown() {
        val timer = object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                mTvInfo.text = getString(R.string.please_try_again_later, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                mTvInfo.setText(R.string.please_draw_unlock_pattern)
                mPatternLockView.isInputEnabled = true
                mWrongCount = 0
            }
        }
        timer.start()
    }

}
