package cc.duduhuo.qpassword.ui.dialog

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import cc.duduhuo.applicationtoast.AppToast
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.service.MainBinder
import cc.duduhuo.qpassword.service.listener.OnGetPasswordsListener
import cc.duduhuo.qpassword.service.listener.OnPasswordsChangeListener
import cc.duduhuo.qpassword.ui.activity.MainActivity
import java.util.*


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/26 15:46
 * Description: 密码去重 Dialog
 * Remarks:
 * =======================================================
 */
class DistinctDialog(private val mActivity: MainActivity,
                     val mMainBinder: MainBinder) : ProgressDialog(mActivity), OnGetPasswordsListener, OnPasswordsChangeListener {
    /** 删除的重复密码的个数 */
    private var mCount = 0

    init {
        setCancelable(false)
    }

    companion object {
        private const val COUNT_INC = 0x0000
        private const val DELETING = 0x0001
        private const val DONE = 0x0002
    }

    private val mHandler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            COUNT_INC -> {
                setMessage(mActivity.getString(R.string.duplicate_passwords_found, mCount))
            }
            DELETING -> {
                setMessage(mActivity.getString(R.string.deleting_duplicate_passwords))
            }
            DONE -> {
                dismiss()
                AppToast.showToast(mActivity.getString(R.string.remove_duplicate_passwords_count, mCount))
                mActivity.refreshAll()
            }
        }
        return@Callback true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMessage(mActivity.getString(R.string.distincting))
    }

    override fun show() {
        super.show()
        mMainBinder.getPasswords(this)
        mMainBinder.registerOnPasswordsChangeListener(this)
    }

    override fun onGetPasswords(groupName: String?, passwords: List<Password>) {
        if (passwords.isEmpty()) {
            AppToast.showToast(R.string.no_password_data)
            dismiss()
            return
        }

        val distinctTask = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, Void?>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val tempPasswords = LinkedList<Password>()
                // 用来存储重复的密码
                val deletePasswords = LinkedList<Password>()
                tempPasswords.add(passwords[0])
                var currPassword: Password
                var hasFoundDuplicate: Boolean
                val size = passwords.size
                for (i in 1 until size) {
                    currPassword = passwords[i]
                    // 重复密码
                    hasFoundDuplicate = tempPasswords.any {
                        it.title == currPassword.title && it.username == currPassword.username
                            && it.password == currPassword.password && it.email == currPassword.email
                            && it.note == currPassword.note
                    }

                    if (hasFoundDuplicate) {
                        mCount++
                        mHandler.sendEmptyMessage(COUNT_INC)
                        deletePasswords.add(currPassword)
                    } else {
                        tempPasswords.add(currPassword)
                    }
                }
                // 删除
                mHandler.sendEmptyMessage(DELETING)
                mMainBinder.deletePasswords(deletePasswords)
                return null
            }
        }
        distinctTask.execute()
    }

    override fun onDeletePasswords(passwords: List<Password>) {
        mHandler.sendEmptyMessage(DONE)
    }

    override fun onNewPasswords(passwords: List<Password>) {
        // no op
    }
}