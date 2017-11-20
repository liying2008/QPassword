package cc.duduhuo.qpassword.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.ImportFile
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
                      private val mFileList: List<ImportFile>) : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val file = mFileList[position]
        if (holder != null) {
            holder.itemView.tv_file.text = file.fileName
        }
    }

    override fun getItemCount(): Int {
        if (mFileList.isEmpty()) {
            return 0
        }
        return mFileList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_import_file, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}