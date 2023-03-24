package com.kotlin.practice.view.horizontal

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.zeekrlife.base.utils.setClickDebouncing

/**
 * 描述:水平视图可滚动控件
 * 功能介绍:可设定最大的平铺数量，一旦超过就提供水平滚动的功能，并且可控制滑动进度
 *
 * @JvmOverloads注解的作用是为我们生成多个重载函数，有几个参数就有几个构造函数的创建
 *
 * 创建者:翁益亨
 * 创建日期:2023/2/2 16:46
 */
@SuppressLint("ResourceType")
class HorizontalPageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), AdaptationScrollView.OnAdaptationScrollChanged {

    /**属性值提供初始化功能*/
    //解析显示的文本以及图片资源
    private var containerDetailInfo = arrayOf<String>()
    private var containerDrawableInfo: TypedArray? = null

    //水平一整屏幕能容纳的最大的组件个数
    private var pageWidthMaxFixedItem: Int = 5

    //超出一屏幕之后是否展示指示器
    private var showIndicator: Boolean = true

    //占位图片的高度设置
    private var pictureHeight: Int = 0

    //根据设计图获取可滑动差距，默认：30
    private var scrollableDistance: Int = 0

    //文本图片的间距
    private var textPictureSpacing: Int = 0

    //文本属性
    private var textConvertSize: Float = 0f

    //滚轮器和文本的间距
    private var indicatorTextSpacing: Int = 0


    /**对象变量*/
    //居于最左边的最大的宽度（能滑动到最右侧的移动距离）
    private var maxLeft: Int = 0

    //距离左边距宽度
    private var currentLeft: Int = 0;

    //屏幕尺寸
    private var screenWidth: Int = 0

    //居中的位置设置
    private val centerPoint by lazy {
        //计算显示滚动条的中心位置。高度=图片高度+文本高度+边距距离
        Point(
            screenWidth / 2,
            pictureHeight + textConvertSize.toInt() + textPictureSpacing + indicatorTextSpacing
        )
    }

    ///动态内容绘制画笔设置
    private var bgPaint: Paint? = null
    private var activePaint: Paint? = null

    //当前导航器样式设定
    private var indicatorType = IndicatorType.NORMAL

    /**
     * 界面绘制对象创建
     * */
    private var rect: RectF? = null
    private var rectActive: RectF? = null


    /**点击回调的数组*/
    private var clickCallbackList = mutableListOf<() -> Unit>()

    /**
     * 适配亮色、暗色主题（系统的深色模式）
     * 当前获取文本颜色、移动样式的颜色、移动样式背景颜色
     * */
    private var themeTextColor: Int = 0
    private var themeMoveColor: Int = 0
    private var themeMoveBgColor: Int = 0

    init {
        attrs?.apply {
            //布局类重写自定义默认不走onDraw方法，在构造方法中调用，设置允许绘制
            setWillNotDraw(false)

            screenWidth = context.resources.displayMetrics.widthPixels

            //获取具体的配置信息
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalPageView)

            val viewContainerTitle =
                typedArray.getResourceId(R.styleable.HorizontalPageView_view_container_title, -1)
            val viewContainerDrawable =
                typedArray.getResourceId(R.styleable.HorizontalPageView_view_container_drawable, -1)

            //getDimension将xml获取的结果转化为像素值返回
            scrollableDistance = typedArray.getDimension(
                R.styleable.HorizontalPageView_scrollable_distance,
                resources.getDimension(R.dimen.dp_30)
            ).toInt()
            pageWidthMaxFixedItem =
                typedArray.getInt(R.styleable.HorizontalPageView_page_width_max_fixed_item, 4)
            showIndicator =
                typedArray.getBoolean(R.styleable.HorizontalPageView_show_indicator, true)
            pictureHeight = typedArray.getDimension(
                R.styleable.HorizontalPageView_picture_height,
                resources.getDimension(R.dimen.dp_40)
            ).toInt()
            textPictureSpacing = typedArray.getDimension(
                R.styleable.HorizontalPageView_text_picture_spacing,
                resources.getDimension(R.dimen.dp_6)
            ).toInt()
            textConvertSize = typedArray.getDimension(
                R.styleable.HorizontalPageView_text_size,
                resources.getDimension(R.dimen.sp_14)
            )
            textPictureSpacing = typedArray.getDimension(
                R.styleable.HorizontalPageView_text_picture_spacing,
                resources.getDimension(R.dimen.dp_6)
            ).toInt()
            indicatorTextSpacing = typedArray.getDimension(
                R.styleable.HorizontalPageView_indicator_text_spacing,
                resources.getDimension(R.dimen.dp_8)
            ).toInt()

            val defaultThemeAttrs =
                context.theme.obtainStyledAttributes(
                    intArrayOf(
                        android.R.attr.textColor,
                        R.attr.backgroundColorPrimaryWithHorizontalPageView,
                        R.attr.backgroundColorSecondaryWithHorizontalPageView
                    )
                )
            themeTextColor = defaultThemeAttrs.getColor(0, normColor)
            themeMoveColor = defaultThemeAttrs.getColor(1, normColor)
            themeMoveBgColor = defaultThemeAttrs.getColor(2, normColor)

            themeTextColor = typedArray.getColor(
                R.styleable.HorizontalPageView_text_color,
                themeTextColor
            )
            themeMoveColor = typedArray.getColor(
                R.styleable.HorizontalPageView_backgroundColorPrimaryWithHorizontalPageView,
                themeMoveColor
            )

            themeMoveBgColor = typedArray.getColor(
                R.styleable.HorizontalPageView_backgroundColorSecondaryWithHorizontalPageView,
                themeMoveBgColor
            )

            defaultThemeAttrs.recycle()


            //配置了显示的标题以及图片之后才允许显示
            if (viewContainerDrawable != -1 && viewContainerTitle != -1) {
                setArraysId(viewContainerTitle, viewContainerDrawable)
            }

            typedArray.recycle()
        }
    }

    /**根据获取的id信息加载本地资源文件渲染*/
    private fun setArraysId(titleId: Int, drawableId: Int) {
        //根据容器内容获取当前显示的数据结构
        containerDetailInfo = resources.getStringArray(titleId)
        containerDrawableInfo = resources.obtainTypedArray(drawableId)

        if (containerDetailInfo.size <= pageWidthMaxFixedItem) {
            //直接水平均分排版展示
            addView(initLayout(true))
        } else {
            //最大滚动的距离计算
            maxLeft =
                (containerDetailInfo.size - pageWidthMaxFixedItem) * (screenWidth / pageWidthMaxFixedItem)

            if (showIndicator) {
                initPaint()
                initDrawData()
            }

            //超出位置，根据屏幕宽度拼接
            //提供可以水平滚动的组件

            val horizontalScrollView = AdaptationScrollView(context).apply {
                onAdaptationScrollChanged = this@HorizontalPageView

                isHorizontalScrollBarEnabled = false

                addView(initLayout(false))
            }


            addView(horizontalScrollView)

        }
    }

    /*** 创建实际内容容器内容
     * @param needEquipartition 是否需要均分，true：需要 false：不需要
     */
    private fun initLayout(needEquipartition: Boolean): LinearLayout {

        val rootLayoutParams = LayoutParams(screenWidth, LayoutParams.WRAP_CONTENT)

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL

            if (showIndicator) {
                //添加底部内边距用于添加底部的滚动范围
                when (indicatorType) {
                    IndicatorType.NORMAL -> {
                        //最后一个参数是保证内边距的空间，容错数据
                        setPadding(
                            0,
                            0,
                            0,
                            IndicatorType.NORMAL.viewSize(resources).height.toInt() + indicatorTextSpacing + resources.getDimensionPixelOffset(
                                R.dimen.dp_2
                            )
                        )
                    }
                }
            }
        }

        initChildLayout(rootLayout, needEquipartition)

        rootLayout.layoutParams = rootLayoutParams

        return rootLayout
    }

    /**
     * 每一个显示空间内容
     * */
    private fun initChildLayout(rootLayout: LinearLayout, needEquipartition: Boolean) {

        for ((index, value) in containerDetailInfo.withIndex()) {
            val textView = TextView(context).apply {
                text = value
                setTextColor(themeTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textConvertSize)

                val drawable = ContextCompat.getDrawable(
                    context,
                    containerDrawableInfo!!.getResourceId(index, 0)
                )!!
                //根据设置的图片高度动态计算对应的高度
                drawable.setBounds(
                    0,
                    0,
                    drawable.minimumWidth / drawable.minimumHeight * pictureHeight,
                    pictureHeight
                )
                setCompoundDrawables(null, drawable, null, null)

                compoundDrawablePadding = textPictureSpacing

                gravity = Gravity.CENTER_HORIZONTAL

            }
            //最后一个参数为均分weight标识，当前默认为UNSPECIFIED_GRAVITY -1
            textView.layoutParams = LinearLayout.LayoutParams(
                if (needEquipartition) 0 else screenWidth / pageWidthMaxFixedItem,
                LayoutParams.MATCH_PARENT,
                if (needEquipartition) 1f else 0f
            )

            textView.setClickDebouncing {
                //需要判断当前点击事件是否实现，如果未实现就不处理
                if (clickCallbackList.size - 1 >= index) {
                    //已经实现，直接执行回调
                    clickCallbackList[index].invoke()
                }
            }
            rootLayout.addView(textView)
        }

    }


    /**
     * 超出屏幕之后显示指示器画笔初始化
     * */
    private fun initPaint() {
        bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        activePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        //设置画笔颜色
        bgPaint!!.color = themeMoveBgColor
        activePaint!!.color = themeMoveColor
    }

    /**
     * 获取绘制界面的类型，初始化对象
     * 避免在onDraw方法中过渡创建对象，导致资源的浪费
     * */
    private fun initDrawData() {
        when (indicatorType) {
            IndicatorType.NORMAL -> {
                rect = RectF()
                rectActive = RectF()
            }
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (showIndicator && containerDetailInfo.size > pageWidthMaxFixedItem) {

            when (indicatorType) {
                IndicatorType.NORMAL -> {
                    //构建底部背景形状
                    val viewSize = IndicatorType.NORMAL.viewSize(resources)
                    rect!!.apply {
                        left = centerPoint.x - viewSize.width / 2
                        top = centerPoint.y.toFloat()
                        right = centerPoint.x + viewSize.width / 2
                        bottom = centerPoint.y + viewSize.height
                    }

                    canvas?.drawRoundRect(
                        rect!!,
                        viewSize.otherConfig[0],
                        viewSize.otherConfig[0],
                        bgPaint!!
                    )

                    //可滚动数据处理，定位滚动的距离
                    rectActive!!.apply {
                        left = centerPoint.x - viewSize.width / 2 + currentLeft
                        top = centerPoint.y.toFloat()
                        right =
                            centerPoint.x + viewSize.scrollWidth - viewSize.width / 2 + currentLeft
                        bottom = centerPoint.y + viewSize.scrollHeight
                    }

                    canvas?.drawRoundRect(
                        rectActive!!,
                        viewSize.otherConfig[0],
                        viewSize.otherConfig[0],
                        activePaint!!
                    )
                }
            }
        }
    }


    /**滚动事件的监听*/
    override fun onScroll(left: Int, top: Int, oldLeft: Int, oldTop: Int) {

        var leftMove = left
        if (maxLeft - leftMove < 0) {
            leftMove = maxLeft
        }

        //计算滑动过的比例
        var percent: Float = leftMove * 1f / maxLeft

        //兼容误差
        if (percent > 0.989) {
            percent = 1f
        }

        //计算进度比例
        when (indicatorType) {
            IndicatorType.NORMAL -> {
                currentLeft =
                    (percent * (IndicatorType.NORMAL.viewSize(resources).width - IndicatorType.NORMAL.viewSize(
                        resources
                    ).scrollWidth)).toInt()
            }
        }

        invalidate()
    }

    /**
     * 初始化点击事件的实现，直接按照顺序去保存，在需要点击时执行
     * */
    fun setClickCallback(vararg block: () -> Unit) {
        clickCallbackList.clear()

        clickCallbackList.addAll(block)
    }


    /**
     * getDimension获取指定资源的尺寸（像素值）-返回Float
     * getDimensionPixelOffset同getDimension,截断小数返回 -返回Int
     * getDimensionPixelSize同getDimension,四舍五入小数返回 -返回Int
     *
     * 设置字体。默认是以SP为单位设置
     * textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX,像素值);
     * */


    //可以在计划上拓展，样式设定，当前设定扁平的滚动条
    enum class IndicatorType {
        NORMAL {
            override fun viewSize(resources: Resources): ViewSize {
                //这里的otherConfig返回圆角
                return ViewSize(
                    resources.getDimension(R.dimen.dp_22), resources.getDimension(R.dimen.dp_4),
                    resources.getDimension(R.dimen.dp_16), resources.getDimension(R.dimen.dp_4),
                    mutableListOf(resources.getDimension(R.dimen.dp_3))
                )
            }
        };

        //背景的宽、高，内部可滑动的宽、高
        abstract fun viewSize(resources: Resources): ViewSize
    }

    data class ViewSize(
        val width: Float,
        val height: Float,
        val scrollWidth: Float,
        val scrollHeight: Float,
        val otherConfig: MutableList<Float>
    )

    companion object {
        /**默认色*/
        private const val normColor = 0XFFFFFF
    }

}