package com.kotlin.practice.ui.camera

import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.databinding.FragmentCameraBinding
import com.kotlin.practice.databinding.ViewCameraContainerBinding
import com.kotlin.practice.util.CameraXUtils
import com.kotlin.practice.util.MediaStoreUtils
import com.kotlin.practice.util.logError
import com.zeekrlife.base.utils.setClickDebouncing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

/**
 * 描述:统一照相机展示功能
 * 功能介绍:避免系统不同展示的照相机页面不同的问题
 * 创建者:翁益亨
 * 创建日期:2023/1/16 16:31
 */
class CameraFragment : BaseFragment<FragmentCameraBinding, BaseViewModel>() {

    //由于Navigation的特殊性，所有和ViewDataBinding挂钩的对象，都需要在生命周期内进行重新创建

    //屏幕旋转信息记录
    private var displayId: Int = -1

    //本地图库信息检索
    private lateinit var mediaStoreUtils: MediaStoreUtils

    private lateinit var cameraXUtils: CameraXUtils

    //嵌入页面的view申明

    private var cameraContainerBinding: ViewCameraContainerBinding? = null

    override fun getLayoutId(): Int = R.layout.fragment_camera

    override fun initVariableId(): Int = BR.vm

    override fun initView() {
        super.initView()

        binding.back.setClickDebouncing {
            requireActivity().finish()
        }

        cameraXUtils = CameraXUtils(requireContext(), binding.previewView)

        mediaStoreUtils = MediaStoreUtils(requireContext())

        //照相机功能注入
        cameraXUtils.setDisplayListener {
           logError( "刷新率改变=" + it)
        }

        cameraXUtils.setVolumeDownReceiver(requireContext()) {
            logError("点击音量键拍摄")
            //拍照点击事件触发
            cameraContainerBinding?.cameraCaptureView?.performClick()
        }

        //需要在页面渲染完毕后，再进行信息的初始化
        binding.previewView.post {

            //在界面渲染完毕之后，使用组件可以获取屏幕刷新率
            var displayId = binding.previewView.display.displayId

            // UI控件处理点击事件
            updateCameraUi()

            lifecycleScope.launch {
                cameraXUtils.setUpCamera()
            }
        }
    }

    /**
     * 添加控制器
     * */
    private fun updateCameraUi() {
        //从当前页面中移除控制器
        cameraContainerBinding?.root.let {
            (binding.root as ViewGroup).removeView(it)
        }

        //重新创建插入的控制器
        cameraContainerBinding = ViewCameraContainerBinding.inflate(
            LayoutInflater.from(this@CameraFragment.requireContext()),
            binding.root as ViewGroup,
            true
        )

        //异步加载当前图库中最后采集的图片信息
        lifecycleScope.launch {
            //注意：需要存储权限
            val thumbnailUri = mediaStoreUtils.getLatestImageFilename()

            //获取并展示缩略图信息
            withContext(Dispatchers.Main) {
                thumbnailUri?.let {
                    //这里要注意API版本的不同，要使用FileProvider进行处理
                    Glide.with(requireContext()).load((it))
                        .apply(RequestOptions.circleCropTransform())
                        .into(cameraContainerBinding!!.photoView)
                    logError("缩略图文件地址：$thumbnailUri")
                }
            }
        }

        //跳转图库-ViewPager2
//        cameraContainerBinding?.photoView?.setClickDebouncing {
//            it.findNavController().navigate(R.id.camera_to_gallery_fragment)
//        }
        //跳转图库 RecyclerView
        cameraContainerBinding?.photoView?.setClickDebouncing {
            findNavController().navigate(R.id.camera_to_gallery_list_fragment)
        }

        //判断照相机的摄像头情况，都存在的情况才展示允许切换摄像头的功能
        cameraContainerBinding?.cameraSwitchView?.visibility =
            if (cameraXUtils.hasBackCamera() && cameraXUtils.hasFrontCamera()) View.VISIBLE else View.INVISIBLE
        //切换摄像头
        cameraContainerBinding?.cameraSwitchView?.setClickDebouncing {
            cameraXUtils.lensFacing =
                if (CameraSelector.LENS_FACING_FRONT == cameraXUtils.lensFacing) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
            //重新加载摄像头配置
            cameraXUtils.bindCameraUseCases()
        }

        //拍照功能
        cameraContainerBinding?.cameraCaptureView?.setClickDebouncing {

            cameraXUtils.imageCapture?.let { imageCapture ->
                //1.创建拍摄的图片的名称。根据当前的时间戳进行命名
                val name =
                    SimpleDateFormat(CameraXUtils.FILENAME).format(System.currentTimeMillis())

                //2.MediaStore实例，用于存储到本地
                val contentValue = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, CameraXUtils.PHOTO_TYPE)

                    //将图片分区放置到文件夹下（分区存储，可以在公共目集合路径下，任意存储文件）
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        val appName = requireContext().resources.getString(R.string.app_name)
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")
                    }
                }

                //3.创建要输出文件的基础信息的对象
                val outputOptions = ImageCapture.OutputFileOptions.Builder(
                    requireContext().contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue
                ).build()

                //4.使用ImageCapture的API进行图像采集，注意：是在异步进行数据的采集和保存，所以要操作UI线程需要切换到Main线程处理
                imageCapture.takePicture(
                    outputOptions,
                    cameraXUtils.cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {

                        //保存采集图片时监听
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val saveUri = outputFileResults.savedUri

                            //官方说明注意事项：
                            // We can only change the foreground Drawable using API level 23+ API
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                cameraContainerBinding!!.photoView.post {
                                    Glide.with(requireContext()).load(saveUri)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(cameraContainerBinding!!.photoView)
                                }
                            }

                            //拍摄完毕，广播通知，刷新图库中的数据
                            //API大于等于24的情况，将忽略此广播
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                requireActivity().sendBroadcast(
                                    Intent(
                                        android.hardware.Camera.ACTION_NEW_PICTURE,
                                        saveUri
                                    )
                                )
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            //图片采集异常信息获取并处理
                        }
                    })


                //5.显示闪光动画，延时渲染界面数据（设置foreground为白色即可） foreground属性生效必须在API大于等于 23基础下
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //设置binding的根部局root的foreground为白色
                    binding.root.postDelayed({
                        binding.root.foreground = ColorDrawable(Color.WHITE)

                        //延时恢复
                        binding.root.postDelayed({
                            binding.root.foreground = null
                        }, 50)

                    }, 100)
                }

            }
        }
    }


    /**屏幕旋转之后重新设置相机配置*/
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        //重置相机配置
        cameraXUtils.bindCameraUseCases()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraXUtils.recycleCameraAsset()
    }
}