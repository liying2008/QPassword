package cc.duduhuo.qpassword.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Group
import cc.duduhuo.qpassword.model.*
import kotlinx.android.synthetic.main.item_drawer_group.view.*
import kotlinx.android.synthetic.main.item_drawer_header.view.*
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
        private const val TYPE_DIVIDER = 0
        private const val TYPE_GROUP = 1
        private const val TYPE_OPERATION = 2
        private const val TYPE_HEADER = 3
        private const val TYPE_TITLE = 4
    }

    private var mLastIndex = -1
    private var mListener: OnItemClickListener? = null
    private var mCurrentGroup: String = ""
    private var mPasswordCount = 0
    private val mDataList = mutableListOf<DrawerItem>()

    /**
     * 初始化数据
     * @param groups 分组列表
     */
    fun initData(groups: List<Group>) {
        mDataList.add(HeaderDrawerItem())
        mDataList.add(TitleDrawerItem(mContext.getString(R.string.menu_group)))
        mDataList.add(GroupDrawerItem(R.drawable.ic_group, mContext.getString(R.string.group_all)))
        groups.mapTo(mDataList) { GroupDrawerItem(R.drawable.ic_group, it.name) }
        mDataList.add(DividerDrawerItem())
        mDataList.add(TitleDrawerItem(mContext.getString(R.string.menu_operation)))
        mDataList.add(OperationDrawerItem(R.drawable.ic_add_box, mContext.getString(R.string.group_add)))
        notifyDataSetChanged()
    }

    /**
     * 添加一个分组
     * @param group 分组
     */
    fun addData(group: Group) {
        val index = mDataList.size - 3
        mDataList.add(index, GroupDrawerItem(R.drawable.ic_group, group.name))
        notifyItemInserted(index)
    }

    /**
     * 删除一个分组
     * @param groupName 分组名称
     */
    fun delData(groupName: String): Boolean {
        val size = mDataList.size
        var curData: DrawerItem
        var index = -1
        for (i in 0 until size) {
            curData = mDataList[i]
            if (curData is GroupDrawerItem) {
                if (curData.title == groupName) {
                    index = i
                    break
                }
            }
        }
        return if (index >= 0) {
            mDataList.removeAt(index)
            notifyItemRemoved(index)
            true
        } else {
            false
        }
    }

    /**
     * 更新分组名称
     * @param oldGroupName 旧分组名称
     * @param newGroupName 新分组名称
     */
    fun updateData(oldGroupName: String, newGroupName: String) {
        val size = mDataList.size
        var curData: DrawerItem
        var index = 0
        for (i in 0 until size) {
            curData = mDataList[i]
            if (curData is GroupDrawerItem) {
                if (curData.title == oldGroupName) {
                    index = i
                    break
                }
            }
        }
        (mDataList[index] as GroupDrawerItem).title = newGroupName
        notifyItemChanged(index)
    }

    /**
     * 更新抽屉栏头部信息
     * @param currentGroup 当前分组名称
     * @param passwordCount 当前分组下密码个数
     */
    fun updateHeader(currentGroup: String?, passwordCount: Int) {
        if (currentGroup == null) {
            this.mCurrentGroup = mContext.getString(R.string.group_all)
            notifyItemChanged(2)
            notifyItemChanged(mLastIndex)
            mLastIndex = 2
        } else if (currentGroup == mContext.getString(R.string.search_result)) {
            this.mCurrentGroup = mContext.getString(R.string.search_result)
            notifyItemChanged(mLastIndex)
            mLastIndex = -1
        } else {
            this.mCurrentGroup = currentGroup
            val index = mDataList.filterIsInstance<GroupDrawerItem>().indexOfFirst { it.title == currentGroup }
            notifyItemChanged(index + 2)
            notifyItemChanged(mLastIndex)
            mLastIndex = index + 2
        }
        this.mPasswordCount = passwordCount
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        val drawerItem = mDataList[position]
        if (drawerItem is DividerDrawerItem) {
            return TYPE_DIVIDER
        } else if (drawerItem is GroupDrawerItem) {
            return TYPE_GROUP
        } else if (drawerItem is OperationDrawerItem) {
            return TYPE_OPERATION
        } else if (drawerItem is HeaderDrawerItem) {
            return TYPE_HEADER
        } else if (drawerItem is TitleDrawerItem) {
            return TYPE_TITLE
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerViewHolder {
        var viewHolder: DrawerViewHolder? = null
        val inflater = LayoutInflater.from(mContext)
        when (viewType) {
            TYPE_DIVIDER -> viewHolder = DividerViewHolder(inflater.inflate(R.layout.item_drawer_divider, parent, false))
            TYPE_HEADER -> viewHolder = HeaderViewHolder(inflater.inflate(R.layout.item_drawer_header, parent, false))
            TYPE_GROUP -> viewHolder = NormalViewHolder(inflater.inflate(R.layout.item_drawer_group, parent, false))
            TYPE_OPERATION -> viewHolder = NormalViewHolder(inflater.inflate(R.layout.item_drawer_operation, parent, false))
            TYPE_TITLE -> viewHolder = TitleViewHolder(inflater.inflate(R.layout.item_drawer_title, parent, false))
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: DrawerViewHolder, position: Int) {
        val item = mDataList[position]
        val type = getItemViewType(position)
        when (type) {
            TYPE_GROUP -> {
                val groupItem = item as GroupDrawerItem
                holder.itemView.iv_ic.setBackgroundResource(groupItem.iconRes)
                holder.itemView.tv_title.text = groupItem.title

                if (groupItem.title == mCurrentGroup) {
                    holder.itemView.tv_title.setTextColor(mContext.resources.getColor(R.color.selected_group_color))
                    holder.itemView.iv_ic.setBackgroundResource(R.drawable.ic_group_selected)
                } else {
                    holder.itemView.tv_title.setTextColor(mContext.resources.getColor(R.color.group_text_color))
                    holder.itemView.iv_ic.setBackgroundResource(R.drawable.ic_group)
                }

                holder.itemView.setOnClickListener {
                    mListener?.onGroupItemClick(groupItem)
                }
                holder.itemView.setOnLongClickListener {
                    mListener?.onGroupItemLongClick(groupItem)
                    return@setOnLongClickListener true
                }
            }
            TYPE_OPERATION -> {
                val opItem = item as OperationDrawerItem
                holder.itemView.iv_ic.setBackgroundResource(opItem.iconRes)
                holder.itemView.tv_title.text = opItem.title

                holder.itemView.setOnClickListener {
                    mListener?.onOperationItemClick(opItem)
                }
            }
            TYPE_HEADER -> {
                holder.itemView.tv_password_count.text = mContext.getString(R.string.group_password_count, mCurrentGroup, mPasswordCount)
            }

            TYPE_TITLE -> {
                val itemTitle = item as TitleDrawerItem
                holder.itemView.tv_title_title.text = itemTitle.title
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    interface OnItemClickListener {
        /**
         * 分组菜单点击事件监听
         * @param groupDrawerItem
         */
        fun onGroupItemClick(groupDrawerItem: GroupDrawerItem)

        /**
         * 分组菜单长按事件监听
         * @param groupDrawerItem
         */
        fun onGroupItemLongClick(groupDrawerItem: GroupDrawerItem)

        /**
         * 操作菜单点击事件监听
         * @param groupDrawerItem
         */
        fun onOperationItemClick(groupDrawerItem: OperationDrawerItem)
    }

    open inner class DrawerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 分组菜单和操作菜单
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
