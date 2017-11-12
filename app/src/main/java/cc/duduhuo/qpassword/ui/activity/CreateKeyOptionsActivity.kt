package cc.duduhuo.qpassword.ui.activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.view.View
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.config.Config
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.MainService
import cc.duduhuo.qpassword.service.listener.OnNewKeyListener
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

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, CreateKeyOptionsActivity::class.java)
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
        setTitle(R.string.title_create_key)
        // 绑定服务
        val intent = MainService.getIntent(this)
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    /** 创建图案密码 */
    fun createPatternKey(view: View) {
        startActivity(CreatePatternLockActivity.getIntent(this))
        this.finish()
    }

    /** 创建数字密码 */
    fun createNumberKey(view: View) {
        startActivity(CreateNumberLockActivity.getIntent(this))
        this.finish()
    }

    /** 创建复杂密码 */
    fun createComplexKey(view: View) {
        startActivity(CreateComplexLockActivity.getIntent(this))
        this.finish()
    }

    /** 不创建密码 */
    fun createNoKey(view: View) {
        AlertDialog.Builder(this)
            .setMessage(R.string.no_key_message)
            .setPositiveButton(R.string.cancel, null)
            .setNegativeButton(R.string.ok) { dialog, which ->
                mMainBinder?.insertKey(Key(key = Config.NO_PASSWORD), object : OnNewKeyListener {
                    override fun onNewKey(key: Key) {
                        startActivity(MainActivity.getIntent(this@CreateKeyOptionsActivity))
                        this@CreateKeyOptionsActivity.finish()
                    }

                })
            }
            .create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }
}
