package com.kotlin.practice.helper.recyclerview

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * 描述:添加加载完成之后的状态展示
 * 功能介绍:额外添加没有更多数据的占位，如果不需要，就使用api的LoadStateAdapter即可
 * 创建者:翁益亨
 * 创建日期:2023/2/22 9:42
 */

/**
 * Adapter for displaying a RecyclerView item based on [LoadState], such as a loading spinner, or
 * a retry error button.
 *
 * By default will use one shared [view type][RecyclerView.Adapter.getItemViewType] for all
 * items.
 *
 * By default, both [LoadState.Loading] and [LoadState.Error] are presented as adapter items,
 * other states are not. To configure this, override [displayLoadStateAsItem].
 *
 * To present this Adapter as a header and or footer alongside your [PagingDataAdapter], see
 * [PagingDataAdapter.withLoadStateHeaderAndFooter], or use
 * [ConcatAdapter][androidx.recyclerview.widget.ConcatAdapter] directly to concatenate Adapters.
 *
 * @see PagingDataAdapter.withLoadStateHeaderAndFooter
 * @see PagingDataAdapter.withLoadStateHeader
 * @see PagingDataAdapter.withLoadStateFooter
 *
 * @sample androidx.paging.samples.loadStateAdapterSample
 */
abstract class LoadStateCustomAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    /**
     * 添加占位标识符，一旦插入之后，其他的都是修改状态即可
     * */
    private var insertCount = 0

    /**
     * LoadState to present in the adapter.
     *
     * Changing this property will immediately notify the Adapter to change the item it's
     * presenting.
     *
     * 这里要注意：field就是当前loadState的值
     * 逻辑梳理：
     * 只有NotLoading的状态才为false，所以这里关注!即可
     * PS：默认初始状态就是NotLoading
     * 1.newItem为NotLoading，那么就要额外判断是否需要展示
     * 2.oldItem为NotLoading，那么基于1的状态处理，对应处理2的信息
     */
    var loadState: LoadState = LoadState.NotLoading(endOfPaginationReached = false)
        set(loadState) {
            if (field != loadState) {

                val oldItem = displayLoadStateAsItem(field)
                val newItem = displayLoadStateAsItem(loadState)

                //当field为LoadState.ERROR，loadState为LoadState.NotLoading情况。网络异常下的下拉刷新情况。并且会重置当前加载的分页为第一页！！！！
                //正常的上拉加载后，流程：field为LoadState.Loading,loadState为LoadState.Error就会结束。下拉刷新多出上述一步骤。导致底部最后的获取状态是NotLoading
                //这里处理的方式：下拉刷新的时候异常就使用占位页面替换。

                if (oldItem && !newItem) {
                    //这里获取的newItem为结束滚动的标识，那么就需要判断是否无法继续加载的标识
                        if(loadState.endOfPaginationReached){
                            //添加底部没有更多数据的标识
                            notifyItemChanged(0)
                        }else{
                            //还可以继续加载，先删除底部展示，因为滑动加载一定会先流转Loading状态（保持原有逻辑）
                            insertCount = 0
                            notifyItemRemoved(0)
                        }

                } else if (newItem && !oldItem) {
                    //初始化上拉加载一定走这里进行元素的插入，添加插入标识
                        if(insertCount == 1){
                            //这个情况就是加载完毕了之后，下拉刷新之后处理的逻辑。在原有的基础上进行内容修改即可
                            notifyItemChanged(0)
                        }else{
                            insertCount = 1
                            notifyItemInserted(0)
                        }
                } else if (oldItem && newItem) {
                    notifyItemChanged(0)
                }else if(!oldItem && !newItem){
                    //适配只有一页数据/深色模式切换的异常状态，初始化数据之后还是NotLoading的状态
                    //说明两个都是NotLoading的状态，loadState状态是当前状态，field为初始化状态。此时应该恢复field的状态
                    //匹配当前是否已经是底部信息，如果是底部并且插入的个数为0，那么就进行一次手动插入
                    if(loadState.endOfPaginationReached&&insertCount==0){
                        insertCount = 1
                        notifyItemInserted(0)
                    }
                }
                field = loadState
            }
        }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return onCreateViewHolder(parent, loadState)
    }

    final override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, loadState)
    }

    final override fun getItemViewType(position: Int): Int = getStateViewType(loadState)

    //重要修改点：使用标识判断占位符
    final override fun getItemCount(): Int = insertCount

    /**
     * Called to create a ViewHolder for the given LoadState.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param loadState The LoadState to be initially presented by the new ViewHolder.
     *
     * @see [getItemViewType]
     * @see [displayLoadStateAsItem]
     */
    abstract fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): VH

    /**
     * Called to bind the passed LoadState to the ViewHolder.
     *
     * @param loadState LoadState to display.
     *
     * @see [getItemViewType]
     * @see [displayLoadStateAsItem]
     */
    abstract fun onBindViewHolder(holder: VH, loadState: LoadState)

    /**
     * Override this method to use different view types per LoadState.
     *
     * By default, this LoadStateAdapter only uses a single view type.
     */
    open fun getStateViewType(loadState: LoadState): Int = 0

    /**
     * Returns true if the LoadState should be displayed as a list item when active.
     *
     * By default, [LoadState.Loading] and [LoadState.Error] present as list items, others do not.
     */
    open fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        return loadState is LoadState.Loading || loadState is LoadState.Error
    }
}
