package cc.duduhuo.qpassword.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.util.formatDate
import cc.duduhuo.qpassword.util.mComparator
import kotlinx.android.synthetic.main.item_password.view.*
import java.util.*


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/30 21:10
 * Description: 密码列表适配器
 * Remarks:
 * =======================================================
 */
class PasswordListAdapter(private val mContext: Context) : RecyclerView.Adapter<PasswordListAdapter.ViewHolder>() {
    private var mPasswords: MutableList<Password> = mutableListOf()
    private var mActionListener: OnPasswordActionListener? = null

    fun setData(passwords: List<Password>) {
        mPasswords.clear()
        mPasswords.addAll(passwords)
        Collections.sort(mPasswords, mComparator)
        notifyDataSetChanged()
    }

    /**
     * 添加一条数据
     * @param password
     */
    fun addData(password: Password) {
        mPasswords.add(0, password)
        Collections.sort(mPasswords, mComparator)
        notifyDataSetChanged()
    }

    /**
     * 删除一条数据
     * @param password 要删除的密码数据
     */
    fun delData(password: Password) {
        val index = mPasswords.indexOf(password)
        if (index >= 0) {
            mPasswords.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * 更新一条数据
     * @param newPassword 新密码数据
     * @param curGroup 当前所处分组
     *
     * @return false：适配器数据空
     */
    fun updateData(newPassword: Password, curGroup: String): Boolean {
        var curPassword: Password
        val size = mPasswords.size
        if (size == 0) {
            return false
        }

        var index = -1
        var needSort = false
        var needRemove = false
        for (i in 0 until size) {
            curPassword = mPasswords[i]
            if (curPassword.id == newPassword.id) {
                index = i
                if (curPassword.groupName != newPassword.groupName && curGroup != mContext.getString(R.string.group_all)) {
                    needRemove = true
                    mPasswords.removeAt(index)
                } else {
                    if (curPassword.isTop != newPassword.isTop) {
                        needSort = true
                    }
                    // newPassword 里 createDate 为 0
                    val createDate = curPassword.createDate
                    mPasswords[i] = newPassword
                    mPasswords[i].createDate = createDate
                }
                break
            }
        }

        if (needRemove) {
            notifyItemRemoved(index)
        } else {
            if (needSort) {
                Collections.sort(mPasswords, mComparator)
                notifyDataSetChanged()
            } else if (index >= 0) {
                notifyItemChanged(index)
            }
        }
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_password, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val password = mPasswords[position]
        holder.itemView.tv_title.text = password.title
        holder.itemView.tv_date.text = formatDate(mContext, password.createDate)
        holder.itemView.tv_username.text = password.username
        holder.itemView.tv_password.text = password.password

        val email = password.email
        if (TextUtils.isEmpty(email)) {
            holder.itemView.ll_email_container.visibility = View.GONE
        } else {
            holder.itemView.ll_email_container.visibility = View.VISIBLE
            holder.itemView.tv_email.text = email
        }

        val note = password.note
        if (TextUtils.isEmpty(note)) {
            holder.itemView.tv_note.visibility = View.GONE
        } else {
            holder.itemView.tv_note.visibility = View.VISIBLE
            holder.itemView.tv_note.text = note
        }

        if (password.isTop) {
            holder.itemView.iv_top.visibility = View.VISIBLE
            holder.itemView.tv_date.setTextColor(mContext.resources.getColor(R.color.title_color))
            holder.itemView.iv_date_icon.setImageResource(R.drawable.ic_date_highlight)
        } else {
            holder.itemView.iv_top.visibility = View.GONE
            holder.itemView.tv_date.setTextColor(mContext.resources.getColor(R.color.main_text_color))
            holder.itemView.iv_date_icon.setImageResource(R.drawable.ic_date)
        }

        holder.itemView.rl_item_copy.setOnClickListener {
            mActionListener?.onCopy(password)
        }

        holder.itemView.rl_item_delete.setOnClickListener {
            mActionListener?.onDelete(password)
        }

        holder.itemView.rl_item_edit.setOnClickListener {
            mActionListener?.onEdit(password)
        }
    }

    override fun getItemCount(): Int {
        return mPasswords.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 设置密码操作监听器
     * @param actionListener
     */
    fun setOnPasswordActionListener(actionListener: OnPasswordActionListener) {
        this.mActionListener = actionListener
    }

    /**
     * 密码操作 Listener
     */
    interface OnPasswordActionListener {
        /**
         * 复制密码信息
         * @param password
         */
        fun onCopy(password: Password)

        /**
         * 删除密码
         * @param password
         */
        fun onDelete(password: Password)

        /**
         * 编辑密码
         * @param password
         */
        fun onEdit(password: Password)
    }
}