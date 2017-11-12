package cc.duduhuo.qpassword.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import cc.duduhuo.qpassword.R

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 20:03
 * Description: 创建数字密码
 * Remarks:
 * =======================================================
 */
class CreateNumberLockActivity : BaseActivity() {
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, CreateNumberLockActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_number_lock)
        setTitle(R.string.title_create_number_key)
    }
}
