package cc.duduhuo.qpassword.ui.activity

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.config.Config
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

    companion object {
        /** 打开的 Activity 列表 */
        private val sActivityList = mutableListOf<BaseActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sActivityList.add(this)
    }

    /**
     * 重启应用
     */
    protected fun restartApp() {
        if (!Config.mIsAllFinishing) {
            AppToast.showToast(R.string.restarting_app)
            destroyAllActivities()
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            startActivity(intent)
        }
    }

    /**
     * 关闭所有 Activity
     */
    protected fun destroyAllActivities() {
        if (!Config.mIsAllFinishing) {
            Config.mIsAllFinishing = true
            sActivityList
                .filterNot { it.isFinishing }
                .forEach { it.finish() }
        }
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