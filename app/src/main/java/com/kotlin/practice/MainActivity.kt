package com.kotlin.practice


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.databinding.ActivityMainBinding
import com.kotlin.practice.ui.MainFragment


/**
 * 用于承载所有的Fragment页面
 * */
class MainActivity : BaseActivity<ActivityMainBinding, MainPageViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//分享功能
//        shareText("分享文本","分享标题")

//        lifecycleScope.launch {
//            //路径示例
//            //file: /storage/emulated/0/Pictures/e943015e8278e23937649d986db2b1d21f60f9addc61cb46b3cf203289211381.0.PNG
//            //uri: content://media/external/images/media/186
//            var images = MediaStoreUtils(this@MainActivity).getImages()
//            images.firstNotNullOf {
//                this@MainActivity.shareImages(listOf(it.uri,it.uri))
//            }
//        }



    }


    override fun getLayoutId(): Int = R.layout.activity_main

    //根Activity页面不能有loading动画，切换横竖屏会导致内容丢失！！！
    override fun initLoadingAnimStatus(): Boolean = false

    //返回当前需要被替换的布局信息
    override fun getLoadingTargetView(): View? = binding.varyLayout

    //返回实例名称：用于创建对应变量名称的实例
    override fun initVariableId(): Int = BR.vm

    //返回按钮处理
    override fun onBackPressed() {
        //获取hostFragment
        val mMainNavFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        //获取当前所在的fragment
        val fragment =
            mMainNavFragment?.childFragmentManager?.primaryNavigationFragment
        //如果当前处于根fragment即HostFragment
        if (fragment is MainFragment) {
            //Activity退出但不销毁
            moveTaskToBack(false)
        } else {
            //由系统处理Navigation中的跳转返回逻辑
            super.onBackPressed()
        }
    }
}





