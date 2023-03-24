package com.kotlin.practice.ui.about

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import by.kirich1409.viewbindingdelegate.ViewBindingPropertyDelegate
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.kotlin.practice.R
import com.kotlin.practice.databinding.ActivityAboutUsBinding
import com.kotlin.practice.util.logError

/**
 * 描述:关于我们界面
 * 功能介绍:集成音视频内容的界面 ExoPlayer是谷歌开源的一个应用级的音视频播放器
 * 官方参考链接：https://developer.android.google.cn/guide/topics/media/mediarecorder?hl=zh-cn
 * 创建者:翁益亨
 * 创建日期:2023/2/24 13:41
 */
class AboutUsActivity :AppCompatActivity(R.layout.activity_about_us){

    private val mBinding by viewBinding(ActivityAboutUsBinding::bind)

    private val mViewModel by viewModels<AboutUsViewModel>()

    /**
     * ExoPlayer 是一个不在 Android 框架内的开放源代码项目，它与 Android SDK 分开提供。ExoPlayer 的标准音频和视频组件基于 Android 的 MediaCodec API 构建
     * 该 API 是在 Android 4.1（API 级别 16）中发布的。由于 ExoPlayer 是一个库，因此您可以通过更新应用来轻松利用新推出的功能。
     * ExoPlayer 支持基于 HTTP 的动态自适应流 (DASH)、SmoothStreaming 和通用加密等功能，这些功能不受 MediaPlayer 的支持。它采用易于自定义和扩展的设计。
     * */


    //播放的url MP4格式
    var mPlayUrlMp4 = "https://upos-sz-mirrorali.bilivideo.com/upgcxcode/81/78/226417881/226417881-1-16.mp4?e=ig8euxZM2rNcNbRVhwdVhwdlhWdVhwdVhoNvNC8BqJIzNbfq9rVEuxTEnE8L5F6VnEsSTx0vkX8fqJeYTj_lta53NCM=&uipk=5&nbs=1&deadline=1677490215&gen=playurlv2&os=alibv&oi=3662288907&trid=08781f5dea0f498884fd599dcfb52ffdh&mid=1347860950&platform=html5&upsig=3d9f11b3d00e6b3c07dbd1d65504c140&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,mid,platform&bvc=vod&nettype=0&bw=52769&logo=80000000"

    //广告url
    var mAdUrl = ""

    private val mSimpleExoPlayer by lazy{
        SimpleExoPlayer.Builder(this).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding.includeTitle.text = mViewModel.title

        mBinding.includeTitle.onClick = View.OnClickListener{
            mViewModel.back(it)
        }


        //PS：使用Exoplayer应该尽量保证在主线程进行访问（不确定当前线程的情况下，播放器可以设置运行的线程为主线程的Looper/mainLooper来指定）
        //1.xml中添加播放视图组件 StyledPlayerView
        //默认的布局提供了进度、倍速、快进/退15s、播放/暂停、重新播放、音频字幕等。一般情况下需要用户自定义。
        // 需要覆盖原有绘制的UI，在当前项目中StyledPlayerControlView默认的布局文件名称相同的文件进行覆盖。
        //或者直接引入文件覆盖（可自定义名称）app:controller_layout_id
        //由于查找的id固定写死，所以要按照StyledPlayerControlView中的R.id.*做好对应，这样功能才会自动关联。内部有判空处理（内部已经实现所有支持的功能）


        //2.创建播放器
        //2.1系统提供简易的播放器，初始化了定制的选项 SimpleExoPlayer

        //3.播放视图绑定播放器，初始化状态
        mBinding.playerView.player = mSimpleExoPlayer
        //3.1要设置监听，全屏按钮才会显示
        mBinding.playerView.setControllerOnFullScreenModeChangedListener {
            isFullScreen->
            logError("是否全屏：$isFullScreen")

            //这里做全屏/半屏的切换
            switchLayoutParams(isFullScreen,false)
        }

        //4.创建播放的媒体类
        val mediaItem = MediaItem.fromUri(mPlayUrlMp4)
        //4.1创建播放的媒体类并插入广告AD，声明式广告支持
        val mediaItemWithAd = MediaItem.Builder().setUri(mPlayUrlMp4).setAdTagUri(mAdUrl).build()

        //5.将要播放的媒体类放入播放器，等待播放器调用播放API播放
        //这里的媒体类可以传入多个，将会依次播放
        mSimpleExoPlayer.setMediaItem(mediaItem)

        //6.播放准备
        mSimpleExoPlayer.prepare()

        //7.开始播放
        mSimpleExoPlayer.play()


    }


    override fun onDestroy() {
        super.onDestroy()

        //释放播放器。懒加载模式下，播放器一定存在
        mSimpleExoPlayer.release()
    }

    //控制播放器的api，用于绑定到自定义样式的组件上的控制方法
    fun operatorPlayer(){
        //1.开始/暂停播放
        mSimpleExoPlayer.play()
        mSimpleExoPlayer.pause()

        //2.搜索到当前窗口中以毫秒为单位指定的位置
        mSimpleExoPlayer.seekTo(10*1000)

        //3.设置是否循环以及如何循环播放/Player.java文件中定义对应类型
        //Player.REPEAT_MODE_OFF：正常播放，无重复。“上一步”和“下一步”动作移动到上一步和下一步
        //Player.REPEAT_MODE_ONE：在正在播放的过程中无限重复当前播放的窗口
        //Player.REPEAT_MODE_ALL：无限重复整个时间线
        mSimpleExoPlayer.repeatMode = Player.REPEAT_MODE_OFF

        //4.调整播放速度和音频音高
        //speed：加快播放速度的因素。必须大于零
        //pitch：调整音频音调的因素。必须大于零
        val playbackParameters = PlaybackParameters(1f,1f)
        mSimpleExoPlayer.playbackParameters = playbackParameters

        //5.是否随机播放
        mSimpleExoPlayer.shuffleModeEnabled = true

        //6.检查是否有上/下一个媒体。播放上/下一个媒体
        mSimpleExoPlayer.hasPreviousWindow()
        mSimpleExoPlayer.seekToPreviousWindow()

        mSimpleExoPlayer.hasNextWindow()
        mSimpleExoPlayer.seekToNextWindow()

    }

    /**
     * 监听屏幕配置的变化，设置全屏/半屏模式
     * PS：要执行回调监听需要在xml中注册configChanges
     * */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                //倾斜90度持有手机，设置布局全屏
                switchLayoutParams(true)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                //竖直方向持有手机。
                switchLayoutParams(false)
            }
        }
    }


    /**
     * 嵌入的控制器获取示例。自定义内容需要暴露外部可操作的方法即可
     * 默认情况下，会添加一个StyledPlayerControlView
     * */
    private val styledPlayerControlView by lazy{
        mBinding.playerView.findViewById<StyledPlayerControlView>(com.google.android.exoplayer2.ui.R.id.exo_controller)
    }


    /**
     * @param isAuto ：是否系统的旋转触发。默认true，不需要手动设置页面的状态
     * */
    private fun switchLayoutParams(isFullScreen:Boolean,isAuto:Boolean = true){
        if(!isAuto){
            //手动切换的情况需要手动设置当前APP显示模式
            this.requestedOrientation = if(isFullScreen) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }else{
            //自动切换的情况，要设置全屏/半屏的按钮状态
            styledPlayerControlView?.apply {

            }
        }

        //这里每次都进行操作也可以采用以下方式更新
        mBinding.playerView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if(isFullScreen){
                height = ConstraintLayout.LayoutParams.MATCH_PARENT
                topToBottom = R.id.root
            }else{
                height = 0
                topToBottom = R.id.include_title
            }
        }

    }



}