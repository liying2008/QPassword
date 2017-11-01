package cc.duduhuo.qpassword.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.model.*
import kotlinx.android.synthetic.main.item_drawer_menu.view.*
import kotlinx.android.synthetic.main.item_drawer_title.view.*


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 11:25
 * Description: 抽屉列表适配器
 * Remarks:
 * =======================================================
 */
class DrawerItemAdapter(private val mContext: Context) : RecyclerView.Adapter<DrawerItemAdapter.DrawerViewHolder>() {
    companion object {
        private val TYPE_DIVIDER = 0
        private val TYPE_NORMAL = 1
        private val TYPE_HEADER = 2
        private val TYPE_TITLE = 3
    }

    private var listener: OnItemClickListener? = null

    private val dataList = mutableListOf<DrawerItem>()

    fun initData(groups: List<Group>) {
        dataList.add(DrawerItemHeader())
        dataList.add(DrawerItemTitle(mContext.getString(R.string.menu_group)))
        dataList.add(DrawerItemNormal(R.drawable.ic_group, mContext.getString(R.string.group_all)))
        groups.mapTo(dataList) { DrawerItemNormal(R.drawable.ic_group, it.name) }
        dataList.add(DrawerItemDivider())
        dataList.add(DrawerItemTitle(mContext.getString(R.string.menu_operation)))
        dataList.add(DrawerItemNormal(R.drawable.ic_add_box, mContext.getString(R.string.group_add)))
        notifyDataSetChanged()
    }

    /**
     * 添加一个分组
     */
    fun addData(group: Group) {
        val index = dataList.size - 2
        dataList.add(index, DrawerItemNormal(R.drawable.ic_group, group.name))
        notifyItemInserted(index)
    }

    /**
     * 删除一个分组
     */
    fun delData(groupName: String) {
        val item = DrawerItemNormal(R.drawable.ic_group, groupName)
        val index = dataList.indexOf(item)
        dataList.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun getItemViewType(position: Int): Int {
        val drawerItem = dataList[position]
        if (drawerItem is DrawerItemDivider) {
            return TYPE_DIVIDER
        } else if (drawerItem is DrawerItemNormal) {
            return TYPE_NORMAL
        } else if (drawerItem is DrawerItemHeader) {
            return TYPE_HEADER
        } else if (drawerItem is DrawerItemTitle) {
            return TYPE_TITLE
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DrawerViewHolder {
        var viewHolder: DrawerViewHolder? = null
        val inflater = LayoutInflater.from(mContext)
        when (viewType) {
            TYPE_DIVIDER -> viewHolder = DividerViewHolder(inflater.inflate(R.layout.item_drawer_divider, parent, false))
            TYPE_HEADER -> viewHolder = HeaderViewHolder(inflater.inflate(R.layout.nav_header_main, parent, false))
            TYPE_NORMAL -> viewHolder = NormalViewHolder(inflater.inflate(R.layout.item_drawer_menu, parent, false))
            TYPE_TITLE -> viewHolder = NormalViewHolder(inflater.inflate(R.layout.item_drawer_title, parent, false))
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return if (dataList == null) 0 else dataList.size
    }

    override fun onBindViewHolder(holder: DrawerViewHolder, position: Int) {
        val item = dataList[position]
        val type = getItemViewType(position)
        when(type) {
            TYPE_NORMAL -> {
                val itemNormal = item as DrawerItemNormal
                holder.itemView.iv_ic.setBackgroundResource(itemNormal.iconRes)
                holder.itemView.tv_title.text = itemNormal.title

                holder.itemView.setOnClickListener {
                    if (listener != null) {
                        listener!!.onItemClick(itemNormal)
                    }
                }
            }
            TYPE_HEADER -> {

            }

            TYPE_TITLE -> {
                val itemTitle = item as DrawerItemTitle
                holder.itemView.tv_title_title.text = itemTitle.title
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(drawerItemNormal: DrawerItemNormal)
    }

    inner open class DrawerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 有图标和文字的菜单项
     */

    inner class NormalViewHolder(itemView: View) : DrawerViewHolder(itemView)

    /**
     * 分割线
     */
    inner class DividerViewHolder(itemView: View) : DrawerViewHolder(itemView)

    /**
     * 头部
     */
    inner class HeaderViewHolder(itemView: View) : DrawerViewHolder(itemView)
    /**
     * 标题菜单项
     */
    inner class TitleViewHolder(itemView: View) : DrawerViewHolder(itemView)
}
