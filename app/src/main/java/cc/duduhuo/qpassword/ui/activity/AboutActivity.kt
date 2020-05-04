package cc.duduhuo.qpassword.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.MenuItem
import android.view.View
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.app.App
import cc.duduhuo.qpassword.util.*
import kotlinx.android.synthetic.main.activity_about.*


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

        tv_version.text = getString(R.string.version, getVersionName())
        if (App.isDebugVersion(this)) {
            tv_name.setText(R.string.app_name_debug)
        } else {
            tv_name.setText(R.string.app_name)
        }
    }

    /**
     * 开源地址
     */
    fun openSource(view: View) {
        val openUrl = getString(R.string.open_url)
        val items = arrayOf(getString(R.string.copy_url), getString(R.string.open_in_browser))
        val builder = AlertDialog.Builder(this)
        builder.setItems(items) { _, which ->
            when (which) {
                0 -> {
                    copyText(this, openUrl)
                    showSnackbar(view, R.string.url_copied)
                }
                1 -> {
                    if (!openBrowser(this, openUrl)) {
                        showSnackbar(view, R.string.browser_not_found)
                    }
                }
            }
        }
        builder.create().show()
    }

    /**
     * 分享应用
     */
    fun shareApp(view: View) {
        val shareText = getString(R.string.share_description, getString(R.string.app_name), getString(R.string.open_url))
        if (!shareText(this, shareText)) {
            showSnackbar(view, R.string.can_not_share)
        }
    }

    /**
     * 意见反馈
     */
    fun feedback(view: View) {
        val email = getString(R.string.about_email)
        // 获取设备分辨率大小
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val resolution = "Resolution: " + point.x + "x" + point.y + "; "
        val deviceInfo = resolution + "\nAndroid: " + android.os.Build.VERSION.RELEASE +
            "; \nPhone: " + android.os.Build.MODEL +
            "; \nVersion: " + getVersionName() +
            "; \n"
        val presetText = getString(R.string.email_pre_text, deviceInfo)
        val subject = getString(R.string.email_subject, getString(R.string.app_name))
        if (!sendEmail(this, email, subject, presetText)) {
            // 复制邮箱地址
            copyText(this, email)
            showSnackbar(view, R.string.email_app_not_found)
        }
    }

    @SuppressLint("InflateParams")
        /**
         * 关于作者
         */
    fun aboutMe(v: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.about_author)
        val view = layoutInflater.inflate(R.layout.dialog_about_me, null, false)
        builder.setView(view)
        builder.setPositiveButton(R.string.ok, null)
        builder.create().show()
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

    /**
     * 获取版本名称
     *
     * @return 版本名称
     */
    private fun getVersionName(): String {
        return packageManager.getPackageInfo(packageName, 0).versionName
    }
}
