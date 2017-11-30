package cc.duduhuo.qpassword.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import cc.duduhuo.qpassword.R
import android.content.pm.PackageManager
import android.R.attr.versionName
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.support.v7.app.AlertDialog
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.util.copyText
import cc.duduhuo.qpassword.util.openBrowser
import cc.duduhuo.qpassword.util.shareText
import kotlinx.android.synthetic.main.activity_about.*
import android.content.ClipData
import android.content.ComponentName
import android.graphics.Point
import android.net.Uri
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.Display
import android.widget.Button
import cc.duduhuo.qpassword.util.sendEmail


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
    }

    /**
     * 开源地址
     */
    fun openSource(view: View) {
        val openUrl = getString(R.string.open_url)
        val items = arrayOf(getString(R.string.copy_url), getString(R.string.open_in_browser))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(openUrl)
        builder.setItems(items) { dialog, which ->
            when (which) {
                0 -> {
                    copyText(this, openUrl)
                    AppToast.showToast(R.string.url_copied)
                }
                1 -> {
                    if (!openBrowser(this, openUrl)) {
                        AppToast.showToast(R.string.browser_not_found)
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
            AppToast.showToast(R.string.can_not_share)
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
            AppToast.showToast(R.string.email_app_not_found)
        }
    }

    /**
     * 关于作者
     */
    fun aboutMe(v: View) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_about_me, null, false)
        val btnOk = view.findViewById<Button>(R.id.btn_ok)
        builder.setView(view)
        val dialog = builder.create()
        btnOk.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
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
