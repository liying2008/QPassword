package cc.duduhuo.qpassword.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cc.duduhuo.qpassword.config.Config

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
    /** 打开的 Activity 列表 */
    companion object {
        private val sActivityList = mutableListOf<BaseActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sActivityList.add(this)
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

//    /**
//     * 检查主密码（是否未被回收）
//     * 如果应用长时间处于后台，Key对象可能会被回收，如果被回收，需要用户重新进入应用输入主密码
//     *
//     */
//    protected fun checkKey() {
//        if (Config.mKey == null || Config.mOriKey.isNullOrEmpty()) {
//            sActivityList
//                .filterNot { it.isFinishing }
//                .forEach { it.finish() }
//            val intent = packageManager.getLaunchIntentForPackage(packageName)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(intent)
//        }
//    }

    /**
     * 重启应用
     */
    protected fun restartApp() {
        sActivityList
            .filterNot { it.isFinishing }
            .forEach { it.finish() }
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    /**
     * 关闭所有 Activity
     */
    protected fun destroyAllActivities() {
        sActivityList
            .filterNot { it.isFinishing }
            .forEach { it.finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        sActivityList.remove(this)
    }
}