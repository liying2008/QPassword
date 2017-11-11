package cc.duduhuo.qpassword.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 17:05
 * Description: Activity的基类
 * Remarks:
 * =======================================================
 */
@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * 点击ActionBar返回图标回到上一个Activity
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}