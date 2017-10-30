package cc.duduhuo.qpassword.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Password
import kotlinx.android.synthetic.main.item_password.view.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/30 21:10
 * Description: 密码列表适配器
 * Remarks:
 * =======================================================
 */
class PasswordListAdapter(private val mContext: Context) : RecyclerView.Adapter<PasswordListAdapter.ViewHolder>() {
    private var mPasswords: List<Password> = mutableListOf()
    fun setData(passwords: List<Password>) {
        mPasswords = passwords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_password, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val password = mPasswords[position]
        holder.itemView.tv_title.text = password.title
        //todo
        holder.itemView.tv_date.text = password.createDate.toString()
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
        } else {
            holder.itemView.iv_top.visibility = View.GONE
            holder.itemView.tv_date.setTextColor(mContext.resources.getColor(R.color.main_text_color))
        }
    }

    override fun getItemCount(): Int {
        return mPasswords.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}