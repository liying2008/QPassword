package cc.duduhuo.qpassword.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.ImportFile
import cc.duduhuo.qpassword.util.getFormatSize
import kotlinx.android.synthetic.main.item_import_file.view.*

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/20 22:55
 * Description:
 * Remarks:
 * =======================================================
 */
class FileListAdapter(private val mContext: Context,
                      private var mFileList: MutableList<ImportFile>) : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {
    private var mFileClickListener: OnFileClickListener? = null
    fun setOnFileClickListener(listener: OnFileClickListener) {
        this.mFileClickListener = listener
    }

    fun removeItem(position: Int) {
        mFileList.removeAt(position)
        this.notifyItemRemoved(position)
    }

    fun refresh(fileList: MutableList<ImportFile>) {
        this.mFileList = fileList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = mFileList[position]
        holder.itemView.tv_file.text = file.fileName
        holder.itemView.tv_file_size.text = getFormatSize(file.fileSize)
        holder.itemView.setOnClickListener {
            mFileClickListener?.onFileClick(file.absolutePath)
        }
        holder.itemView.setOnLongClickListener {
            if (mFileClickListener == null) {
                return@setOnLongClickListener true
            } else {
                return@setOnLongClickListener mFileClickListener!!.onFileLongClick(holder.adapterPosition, file.absolutePath)
            }
        }
    }

    override fun getItemCount(): Int {
        if (mFileList.isEmpty()) {
            return 0
        }
        return mFileList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_import_file, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 文件名点击监听
     */
    interface OnFileClickListener {
        /**
         * 文件名被点击
         * @param absolutePath 绝对路径
         */
        fun onFileClick(absolutePath: String)

        /**
         * 文件名被长按
         * @param position Item 位置
         * @param absolutePath 绝对路径
         */
        fun onFileLongClick(position: Int, absolutePath: String): Boolean
    }
}