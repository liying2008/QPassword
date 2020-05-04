package cc.duduhuo.qpassword.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.duduhuo.qpassword.R
import kotlinx.android.synthetic.main.item_number_grid.view.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/14 21:37
 * Description: 数字面板适配器
 * Remarks:
 * =======================================================
 */
class NumberGridAdapter(private val mContext: Context) : RecyclerView.Adapter<NumberGridAdapter.ViewHolder>() {
    private val mNumbers: Array<String> = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9",
        mContext.getString(R.string.delete), "0", mContext.getString(R.string.ok))
    private var mListener: OnNumberClickListener? = null

    fun setOnNumberClickListener(listener: OnNumberClickListener) {
        this.mListener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val number = mNumbers[position]
        holder.itemView.tv_number_btn.text = number
        if (number == mContext.getString(R.string.delete)) {
            holder.itemView.setOnClickListener {
                mListener?.onClickDel(holder.itemView as TextView)
            }
        } else if (number == mContext.getString(R.string.ok)) {
            holder.itemView.setOnClickListener {
                mListener?.onClickOk(holder.itemView as TextView)
            }
        } else {
            holder.itemView.setOnClickListener {
                mListener?.onClickNum(number, holder.itemView as TextView)
            }
        }

    }

    override fun getItemCount(): Int {
        return mNumbers.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_number_grid, parent, false))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 数字面板点击监听
     */
    interface OnNumberClickListener {
        /**
         * 点击数字
         * @param number 点击的数字
         * @param view 点击的 View
         */
        fun onClickNum(number: String, view: TextView)

        /** 点击 删除 按钮 */
        fun onClickDel(view: TextView)

        /** 点击确定按钮 */
        fun onClickOk(view: TextView)
    }

}