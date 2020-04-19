package cc.duduhuo.qpassword.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.View


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/14 22:13
 * Description: 数字面板 Item 之间的分割线
 * Remarks:
 * =======================================================
 */
class FileItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {
    private var mPaint: Paint? = null
    private var mDivider: Drawable
    /** 分割线高度 */
    private var mDividerHeight = 2
    private val mAttrs = intArrayOf(android.R.attr.listDivider)

    init {
        val attr = context.obtainStyledAttributes(mAttrs)
        mDivider = attr.getDrawable(0)!!
        attr.recycle()
    }

    /**
     * 获取分割线尺寸
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, mDividerHeight)
    }

    /**
     * 绘制分割线
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    /**
     * 绘制横向 item 分割线
     */
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.measuredWidth - parent.paddingRight
        val childSize = parent.childCount
        for (i in 0 until childSize) {
            val child = parent.getChildAt(i)
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + layoutParams.bottomMargin
            val bottom = top + mDividerHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(canvas)
            if (mPaint != null) {
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint!!)
            }
        }
    }

    /**
     * 绘制纵向 item 分割线
     */
    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.measuredHeight - parent.paddingBottom
        val childSize = parent.childCount
        for (i in 0 until childSize) {
            val child = parent.getChildAt(i)
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            val left = child.right + layoutParams.rightMargin
            val right = left + mDividerHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(canvas)
            if (mPaint != null) {
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint!!)
            }
        }
    }

}