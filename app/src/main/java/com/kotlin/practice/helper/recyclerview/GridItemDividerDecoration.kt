package com.kotlin.practice.helper.recyclerview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 描述:网格分割处理
 * 功能介绍:网格分割处理
 * 创建者:翁益亨
 * 创建日期:2023/2/10 15:48
 */
class GridItemDividerDecoration(
    var lineColor: Int,//分割线的颜色
    var lineThickness: Int//分割线的厚度
) : RecyclerView.ItemDecoration() {

    private val rectVertical = Rect()
    private val rectHorizontal = Rect()

    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    init{
        mPaint.apply {
            style = Paint.Style.FILL
            color = lineColor
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount

        for (i in 0 until childCount) {
            val childView = parent.getChildAt(i)
            val leftAndRight = getViewPositionArea(childCount,spanCount,i)

            //普通绘制横线
            rectHorizontal.left = childView.left - (leftAndRight[leftAndRight.lastIndex-1]*lineThickness).toInt()
            rectHorizontal.right = childView.right + (leftAndRight.last()*lineThickness).toInt()
            rectHorizontal.top = childView.bottom
            rectHorizontal.bottom = rectHorizontal.top + lineThickness


            c.drawRect(rectHorizontal,mPaint)

            if (i < spanCount) {
                //如果是第一行的情况,就要全部添加高度，当前显示的高度
                rectHorizontal.top = childView.top - lineThickness
                rectHorizontal.bottom = childView.top

                c.drawRect(rectHorizontal,mPaint)
            }


            //竖直方向左侧绘制
            rectVertical.left = childView.left - (leftAndRight[leftAndRight.lastIndex-1]*lineThickness).toInt()
            rectVertical.right = childView.left
            rectVertical.top = childView.top
            rectVertical.bottom = childView.bottom + lineThickness

            c.drawRect(rectVertical,mPaint)


            //竖直方向右侧绘制
            rectVertical.left = childView.right
            //竖直右侧方向存在最后一个数据需要补全的情况，最后一条数据高度就要设置比例1
            rectVertical.right =  childView.right + if(i == childCount -1) lineThickness else (leftAndRight.last()*lineThickness).toInt()

            c.drawRect(rectVertical,mPaint)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        //设定好位移之后再执行onDraw方法，onDraw中view的位置为已经移动过的内容

        //获取当前view在布局中的position
        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        //每一行要显示的个数
        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount

        //获取对应的处理数据，直接获取最后两位即可，倒数第一位为最右侧，倒数第二位为最左侧
        //保证均分空白区域
        val leftAndRight = getViewPositionArea(parent.adapter?.itemCount?:0,spanCount,itemPosition)

        //处理第一行的情况
        if (itemPosition < spanCount) {
            outRect.top += lineThickness
        }

        outRect.left += (leftAndRight[leftAndRight.lastIndex-1]*lineThickness).toInt()

        outRect.right += (leftAndRight.last()*lineThickness).toInt()

        outRect.bottom += lineThickness
    }


    /**计算每一个item占用的空白区域的大小，当前是用于竖直分割。例如：一行排4个元素，那么就有5个间隔。以此类推
     * 注意：最后一条数据需要手动补全剩余比例
     * */
    private fun getViewPositionArea(totalCount :Int,spanCount: Int,itemPosition :Int):MutableList<Float> {
        //按顺序存储每一个Item的左右数值
        val mutableList = mutableListOf<Float>()
        //每一个元素要分配areaRatio的空间，保证每一个Item缩放一致
        val areaRatio = (spanCount + 1) * 1f / spanCount

        //最左边比例数据初始化
        mutableList.add(1f)
        mutableList.add((areaRatio - 1))

        if(itemPosition%spanCount>0){
            //计算当前角标对应的数据，每一行的从左到右（除最左边初始化数据以外）
            for(i in 1..(itemPosition%spanCount)){
                //添加最左边的元素间距比例，获取当前最后一个直接用基数区减即可
                mutableList.add(1 - mutableList.last())
                mutableList.add(areaRatio - mutableList.last())
            }
        }


        return mutableList
    }
}