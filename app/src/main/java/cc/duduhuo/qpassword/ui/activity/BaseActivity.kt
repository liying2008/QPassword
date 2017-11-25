package cc.duduhuo.qpassword.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.google.common.collect.ArrayListMultimap


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
    /** 存储所有Activity中的异步任务，便于统一删除  */
    private val mTasks = ArrayListMultimap.create<Class<out BaseActivity>, AsyncTask<*, *, *>>()

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

    /**
     * 注册任务
     *
     * @param clazz Activity Class
     * @param task  AsyncTask对象
     */
    fun registerAsyncTask(clazz: Class<out BaseActivity>, task: AsyncTask<*, *, *>) {
        val b = mTasks.put(clazz, task)
        if (!b) {
            Log.e(javaClass.simpleName, "AsyncTask Register Error!!")
        }
    }

    /**
     * 如果任务未执行完毕，则取消任务
     *
     * @param clazz Activity Class
     */
    fun unregisterAsyncTask(clazz: Class<out BaseActivity>) {
        val tasks = ArrayList(mTasks.removeAll(clazz))
        val size = tasks.size
        if (size > 0) {
            for (i in 0 until size) {
                val task = tasks[i]
                //you may call the cancel() method but if it is not handled in doInBackground() method
                if (task != null && task.status != AsyncTask.Status.FINISHED) {
                    task.cancel(true)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sActivityList.remove(this)
    }
}