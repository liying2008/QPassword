package cc.duduhuo.qpassword.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AlertDialog
import android.view.MenuItem
import android.view.View
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnKeyChangeListener
import cc.duduhuo.qpassword.service.listener.OnNewKeyListener
import cc.duduhuo.qpassword.util.keyLost
import kotlinx.android.synthetic.main.activity_create_key_options.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 19:35
 * Description: 选择 创建主密码 的方式
 * Remarks:
 * =======================================================
 */
class CreateKeyOptionsActivity : BaseActivity() {
    private var mMainBinder: MainBinder? = null
    private var mMode: Int = MODE_CREATE
    /** 是否正在更改主密码（期间不允许finish Activity） */
    private var mUpdating = false

    companion object {
        private const val MODE = "mode"
        const val MODE_CREATE = 0
        const val MODE_UPDATE = 1
        fun getIntent(context: Context, mode: Int): Intent {
            val intent = Intent(context, CreateKeyOptionsActivity::class.java)
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

    private fun initViews() {
        btn_pattern_key.isEnabled = true
        btn_number_key.isEnabled = true
        btn_complex_key.isEnabled = true
        btn_no_key.isEnabled = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_key_options)
        mMode = intent.getIntExtra(MODE, MODE_CREATE)
        if (mMode == MODE_CREATE) {
            setTitle(R.string.title_create_key)
        } else if (mMode == MODE_UPDATE) {
            setTitle(R.string.title_update_key)
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    /** 创建图案密码 */
    fun createPatternKey(view: View) {
        startActivity(CreatePatternLockActivity.getIntent(this, mMode))
        this.finish()
    }

    /** 创建数字密码 */
    fun createNumberKey(view: View) {
        startActivity(CreateNumberLockActivity.getIntent(this, mMode))
        this.finish()
    }

    /** 创建复杂密码 */
    fun createComplexKey(view: View) {
        startActivity(CreateComplexLockActivity.getIntent(this, mMode))
        this.finish()
    }

    /** 不创建密码 */
    fun createNoKey(view: View) {
        AlertDialog.Builder(this)
            .setMessage(R.string.no_key_message)
            .setPositiveButton(R.string.cancel, null)
            .setNegativeButton(R.string.ok) { _, _ ->
                btn_pattern_key.isEnabled = false
                btn_number_key.isEnabled = false
                btn_complex_key.isEnabled = false
                btn_no_key.isEnabled = false
                if (mMode == MODE_CREATE) {
                    mMainBinder?.insertKey(Key(Config.NO_PASSWORD, Key.MODE_NO_KEY), object : OnNewKeyListener {
                        override fun onNewKey(key: Key) {
                            Config.mKey = key
                            Config.mOriKey = Config.NO_PASSWORD
                            startActivity(MainActivity.getIntent(this@CreateKeyOptionsActivity))
                            this@CreateKeyOptionsActivity.finish()
                        }

                    })
                } else if (mMode == MODE_UPDATE) {
                    AppToast.showToast(R.string.applying_key_changes)
                    mMainBinder?.updateKey(Config.mKey!!, Config.mOriKey, Key(Config.NO_PASSWORD, Key.MODE_NO_KEY), Config.NO_PASSWORD, mOnKeyChangeListener)
                    mUpdating = true
                }
            }
            .create().show()
    }

    private val mOnKeyChangeListener = object : OnKeyChangeListener {
        override fun onUpdateKey(oldKey: Key, newKey: Key) {
            Config.mKey = newKey
            Config.mOriKey = Config.NO_PASSWORD
            if (oldKey.key != newKey.key || oldKey.mode != newKey.mode) {
                AppToast.showToast(R.string.key_updated)
                destroyAllActivities()
                Config.mIsAllFinishing = false
                startActivity(MainActivity.getIntent(this@CreateKeyOptionsActivity))
            } else {
                this@CreateKeyOptionsActivity.finish()
            }
            mUpdating = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (mMode == MODE_UPDATE) {
            if (keyLost()) {
                restartApp()
            }
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
