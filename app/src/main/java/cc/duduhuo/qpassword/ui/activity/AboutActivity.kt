package cc.duduhuo.qpassword.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import cc.duduhuo.qpassword.R

class AboutActivity : BaseActivity() {
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AboutActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.title_about)

    }

    /**
     * 开源地址
     */
    fun openSource(view: View) {

    }

    /**
     * 分享应用
     */
    fun shareApp(view: View) {

    }

    /**
     * 意见反馈
     */
    fun feedback(view: View) {

    }

    /**
     * 关于作者
     */
    fun aboutMe(view: View) {

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
