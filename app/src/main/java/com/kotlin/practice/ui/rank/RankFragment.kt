package com.kotlin.practice.ui.rank

import android.Manifest.permission.*
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.fragment.navArgs
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.databinding.FragmentRankBinding
import com.kotlin.practice.util.*
import com.zeekrlife.base.utils.setClickDebouncing


/**
 * 描述:排行榜列表
 * 功能介绍:列表内容展示
 * 创建者:翁益亨
 * 创建日期:2023/1/9 14:41
 */
class RankFragment : BaseFragment<FragmentRankBinding, RankViewModel>() {
    override fun getLayoutId(): Int = R.layout.fragment_rank

    override fun initVariableId(): Int = BR.vm

    //获取传递的参数内容（自动生成的类型，依据传递的数据生成对象  类名+Args类型）
    private val args: RankFragmentArgs by navArgs()


    //授权相关全部由界面完成
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
//            ActivityResultLauncher<String>


    override fun initParams() {
        super.initParams()

        //这里要处理的数据是先判断数据是否为默认参数，如果不是就要进行数据赋值
        //安全模式传参
        viewModel.count.value = args.count

        //获取对应的实体类信息
        var name = args.rankData?.name
        var ranking = args.rankData?.ranking

        //普通接收方式
//        arguments?.let {
//            val count = it.getInt("count")
//            viewModel.count.value = count
//        }
    }

    override fun initView() {
        super.initView()

        //必须在fragment挂载到Activity上时，context才会存在，所以之类初始化需要放在onAttach周期之后
//        permissionLauncher = registerForPermissionResult(
//            onGranted = { Log.e("wyh", "授权成功") },
//            onDenied = { permissionScope, name ->
//                //后台定位权限设定
//                permissionScope?.backLocation()
//                Log.e("wyh", "拒绝授权，不能弹窗") },
//            onShowRequestRationale = { permissionScope, name ->
//                Log.e("wyh", "权限拒绝，但是能继续弹窗授权")
//            }
//        )


        permissionLauncher = registerForPermissionsResult(
            onAllGranted = { logError("全部授权成功")},
            onShowRequestRationale = {a,b,c->
                for(deny in b){
                    logError("拒绝,但是可以继续弹窗授权：$deny")
                }

                for(deny in c){
                    logError("所有拒绝权限：$deny")
                }
            },
            onDenied = {a,b->
                for(deny in b){
                    logError("拒绝,不能弹窗授权：$deny")
                }
            }
        )

        binding.permissionBtn.setClickDebouncing {
            //单个授权
//            permissionLauncher?.launchX(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            //多个授权
            permissionLauncher?.launchX(arrayOf(
                CAMERA,
                RECORD_AUDIO,
                ACCESS_COARSE_LOCATION,
                WRITE_EXTERNAL_STORAGE
            ))
        }


    }




}