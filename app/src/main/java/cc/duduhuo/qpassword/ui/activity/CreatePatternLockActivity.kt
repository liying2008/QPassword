package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnKeyChangeListener
import cc.duduhuo.qpassword.service.listener.OnNewKeyListener
import cc.duduhuo.qpassword.util.keyLost
import cc.duduhuo.qpassword.util.sha1Hex
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import kotlinx.android.synthetic.main.activity_create_pattern_lock.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 20:03
 * Description: 创建图案密码
 * Remarks:
 * =======================================================
 */
class CreatePatternLockActivity : BaseActivity(), OnKeyChangeListener {
    private var mMode: Int = MODE_CREATE
    private var mKey: String? = null
    private var mMainBinder: MainBinder? = null
    private lateinit var mPatternLockView: PatternLockView
    private lateinit var mTvInfo: TextView

    companion object {
        private const val MODE = "mode"
        const val MODE_CREATE = 0
        const val MODE_UPDATE = 1
        fun getIntent(context: Context, mode: Int): Intent {
            val intent = Intent(context, CreatePatternLockActivity::class.java)
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
                mMainBinder?.registerOnKeyChangeListener(this@CreatePatternLockActivity)
            }
        }
    }

    private fun initViews() {
        mPatternLockView.isInputEnabled = true
        btn_redraw.isEnabled = true
        btn_redraw.setOnClickListener {
            // 重新绘制
            mTvInfo.setText(R.string.please_connect_at_least_4_points)
            mPatternLockView.clearPattern()
            btn_done.isEnabled = false
        }

        btn_done.setOnClickListener {
            if (mKey != null && mKey!!.length >= 4) {
                btn_done.isEnabled = false
                btn_redraw.isEnabled = false

                if (mMode == MODE_CREATE) {
                    mMainBinder?.insertKey(Key(mKey!!.sha1Hex(), Key.MODE_PATTERN), object : OnNewKeyListener {
                        override fun onNewKey(key: Key) {
                            Config.mKey = key
                            Config.mOriKey = mKey!!
                            startActivity(MainActivity.getIntent(this@CreatePatternLockActivity))
                            this@CreatePatternLockActivity.finish()
                        }
                    })
                } else if (mMode == MODE_UPDATE) {
                    AppToast.showToast(R.string.applying_key_changes)
                    mMainBinder?.updateKey(Config.mKey!!, Config.mOriKey, Key(mKey!!.sha1Hex(), Key.MODE_PATTERN), mKey!!)
                }
            } else {
                AppToast.showToast(R.string.connect_at_least_4_points)
            }
        }
    }

    private val mPatternLockViewListener = object : PatternLockViewListener {
        override fun onStarted() {
            mTvInfo.setText(R.string.please_connect_at_least_4_points)
            btn_done.isEnabled = false
        }

        override fun onProgress(progressPattern: List<PatternLockView.Dot>) {
            // no op
        }

        override fun onComplete(pattern: List<PatternLockView.Dot>) {
            mKey = PatternLockUtils.patternToString(mPatternLockView, pattern)
            if (pattern.size < 4) {
                mTvInfo.setText(R.string.connect_at_least_4_points)
                mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG)
                return
            }
            mTvInfo.setText(R.string.meet_requirement)
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT)
            btn_done.isEnabled = true
        }

        override fun onCleared() {
            mTvInfo.setText(R.string.please_connect_at_least_4_points)
            btn_done.isEnabled = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pattern_lock)
        mMode = intent.getIntExtra(MODE, MODE_CREATE)
        setTitle(R.string.title_create_pattern_key)
        mPatternLockView = findViewById(R.id.pattern_lock_view)
        mTvInfo = findViewById(R.id.tv_info)
        mPatternLockView.addPatternLockListener(mPatternLockViewListener)
        mPatternLockView.isInputEnabled = false
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

    override fun onUpdateKey(oldKey: Key, newKey: Key) {
        Config.mKey = newKey
        Config.mOriKey = mKey!!
        if (oldKey.key != newKey.key) {
            AppToast.showToast(R.string.key_updated)
            destroyAllActivities()
            startActivity(MainActivity.getIntent(this))
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
