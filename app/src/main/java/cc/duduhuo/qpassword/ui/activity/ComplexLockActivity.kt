package cc.duduhuo.qpassword.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import cc.duduhuo.qpassword.R

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 22:42
 * Description: 复杂密码解锁界面
 * Remarks:
 * =======================================================
 */
class ComplexLockActivity : BaseActivity() {
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ComplexLockActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complex_lock)
    }
}
