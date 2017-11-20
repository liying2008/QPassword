package cc.duduhuo.qpassword.ui.activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AlertDialog
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
class CreateKeyOptionsActivity : BaseActivity(), OnKeyChangeListener {
    private var mMainBinder: MainBinder? = null
    private var mMode: Int = MODE_CREATE

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
                mMainBinder?.registerOnKeyChangeListener(this@CreateKeyOptionsActivity)
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
            .setNegativeButton(R.string.ok) { dialog, which ->
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
                    mMainBinder?.updateKey(Config.mKey!!, Config.mOriKey, Key(Config.NO_PASSWORD, Key.MODE_NO_KEY), Config.NO_PASSWORD)
                }
            }
            .create().show()
    }

    override fun onUpdateKey(oldKey: Key, newKey: Key) {
        Config.mKey = newKey
        Config.mOriKey = Config.NO_PASSWORD
        if (oldKey.key != newKey.key) {
            AppToast.showToast(R.string.key_updated)
            destroyAllActivities()
            startActivity(MainActivity.getIntent(this))
        } else {
            this@CreateKeyOptionsActivity.finish()
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

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
