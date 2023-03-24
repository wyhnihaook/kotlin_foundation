package com.kotlin.practice.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.*
import java.util.ArrayDeque

/**
 * 描述:CameraX帮助类
 * 功能介绍:向应用添加相机功能，每次使用时，都要创建对应的对象去初始化相机数据，并和展示页面绑定
 * 注意：相机权限要在onResume中进行判断是否存在，如果不存在要做相应的处理
 * @param previewView 布局中的预览界面
 * 创建者:翁益亨
 * 创建日期:2023/1/16 16:24
 */

//页面上监听按钮事件后，通过广播发送的对应事件（这里处理的是音量键的按下）：
// val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
// LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"

typealias LumaListener = (luma: Double) -> Unit

class CameraXUtils(val context: Context,private val previewView: PreviewView){

    companion object {
        private const val TAG = "CameraXUtils"
        const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val PHOTO_TYPE = "image/png"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }


    private var windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var camera: Camera? = null

    /**可用镜头识别，提供外部点击切换的功能*/
    var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    /**实际操作照相机功能的对象*/
    val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val cameraProvider: ProcessCameraProvider by lazy {
        ProcessCameraProvider.getInstance(context).get()
    }

    /**图片拍摄的实际操作对象*/
    var imageCapture: ImageCapture? = null

    private var imageAnalyzer: ImageAnalysis? = null

    private var preview: Preview? = null

    private val displayManager by lazy {
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private var displayListener:DisplayListener? = null

    //广播管理，这里用于注册音量广播的接收
    private lateinit var broadcastManager: LocalBroadcastManager

    private var volumeDownReceiver:VolumeDownReceiver? = null


    /**初始化CameraX，并准备绑定相机实例*/
    fun setUpCamera(){
        //采用懒加载，避免hasBackCamera/hasFrontCamera执行时cameraProvider为null导致摄像头可用状态无法判断
//        cameraProvider = ProcessCameraProvider.getInstance(context).get()

        //判断能使用的摄像头
        lensFacing = when {
            hasBackCamera() -> CameraSelector.LENS_FACING_BACK
            hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
            else -> throw IllegalStateException("Back and front camera are unavailable")
        }

        //构建并绑定相机用例
        bindCameraUseCases()
    }


    /**声明和绑定预览、捕获和分析用例*/
    fun bindCameraUseCases() {

        // 获取用于设置相机全屏分辨率的屏幕指标，API版本有限制
//        val metrics = windowManager.getCurrentWindowMetrics().bounds

        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        val screenAspectRatio = aspectRatio(dm.widthPixels, dm.heightPixels)

        //获取当前移动端的旋转角度
        val rotation = previewView.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // 摄影机选择器
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // 预览页面的配置
        preview = Preview.Builder()
            //横纵比设置
            .setTargetAspectRatio(screenAspectRatio)
            // 初始化旋转的角度设定
            .setTargetRotation(rotation)
            .build()

        // 图像拍摄（提供拍摄api的功能调用）
        imageCapture = ImageCapture.Builder()
            //CAPTURE_MODE_MINIMIZE_LATENCY：缩短图片拍摄的延迟时间。默认
            //CAPTURE_MODE_MAXIMIZE_QUALITY：提高图片拍摄的图片质量。
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // 图像分析
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // 我们在这里记录图像分析结果
                })
            }

        // 使用前必须保证解绑状态
        cameraProvider.unbindAll()

        if (camera != null) {
            //必须从上一个相机实例中删除观察者
            removeCameraStateObservers(camera!!.cameraInfo)
        }

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                previewView.findViewTreeLifecycleOwner()!!, cameraSelector, preview, imageCapture, imageAnalyzer)

            // 将取景器的表面提供程序连接到预览用例，将采集到的数据显示到界面上展示
            preview?.setSurfaceProvider(previewView.surfaceProvider)
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            logError( "Use case binding failed", exc)
        }
    }

    /**移除本身挂载在生命周期上的监听器，重新注入*/
    private fun removeCameraStateObservers(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.removeObservers(previewView.findViewTreeLifecycleOwner()!!)
    }

    /**当前设备硬件状态监听*/
    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(previewView.findViewTreeLifecycleOwner()!!) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                        Toast.makeText(context,
                            "CameraState: Pending Open",
                            Toast.LENGTH_SHORT).show()
                    }
                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        Toast.makeText(context,
                            "CameraState: Opening",
                            Toast.LENGTH_SHORT).show()
                    }
                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                        Toast.makeText(context,
                            "CameraState: Open",
                            Toast.LENGTH_SHORT).show()
                    }
                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                        Toast.makeText(context,
                            "CameraState: Closing",
                            Toast.LENGTH_SHORT).show()
                    }
                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                        Toast.makeText(context,
                            "CameraState: Closed",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        Toast.makeText(context,
                            "Stream config error",
                            Toast.LENGTH_SHORT).show()
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        Toast.makeText(context,
                            "Camera in use",
                            Toast.LENGTH_SHORT).show()
                    }
                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        Toast.makeText(context,
                            "Max cameras in use",
                            Toast.LENGTH_SHORT).show()
                    }
                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                        Toast.makeText(context,
                            "Other recoverable error",
                            Toast.LENGTH_SHORT).show()
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        Toast.makeText(context,
                            "Camera disabled",
                            Toast.LENGTH_SHORT).show()
                    }
                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        Toast.makeText(context,
                            "Fatal error",
                            Toast.LENGTH_SHORT).show()
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Toast.makeText(context,
                            "Do not disturb mode enabled",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**获取最合适的展示比例。摄像头采集图像比例*/
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** 后置摄像头是否能使用方法 */
    fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** 前置摄像头是否能使用方法 */
    fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    /**初始化创建屏幕的刷新率变化监听器*/
    fun setDisplayListener(block: (Int) -> Unit):DisplayListener = displayListener?: synchronized(this){
        displayListener?:DisplayListener(block).also {
            displayListener = it

            //屏幕的刷新率变化监听和回调注册
            displayManager.registerDisplayListener(displayListener, null)
        }
    }

    /**
     * 屏幕的刷新率变化监听（刷新率是指电子束对屏幕上的图像重复扫描的次数。刷新率越高，所显示的图象（画面）稳定性就越好）
     * */
    class DisplayListener(private val block: (Int) -> Unit):DisplayManager.DisplayListener{
        override fun onDisplayAdded(displayId: Int) = Unit

        override fun onDisplayRemoved(displayId: Int) = Unit

        override fun onDisplayChanged(displayId: Int) {
            //方向变化时进行处理
            block(displayId)
        }
    }


    /**初始化创建监听器，返回对象用于注册监听和解绑监听*/
    fun setVolumeDownReceiver(context: Context,block: () -> Unit):VolumeDownReceiver = volumeDownReceiver?: synchronized(this){
            volumeDownReceiver?:VolumeDownReceiver(block).also {
                volumeDownReceiver = it

                //广播注册，监听事件
                broadcastManager = LocalBroadcastManager.getInstance(context)
                val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }

                broadcastManager.registerReceiver(volumeDownReceiver!!,filter)
            }
        }


    /**音量键点击同步拍照，通过广播接收器监听事件*/
    class VolumeDownReceiver(private val block:()->Unit) : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.getIntExtra(KEY_EVENT_EXTRA,KeyEvent.KEYCODE_UNKNOWN)){
                //当监听到音量键按下的时候，触发拍照功能
                KeyEvent.KEYCODE_VOLUME_DOWN,KeyEvent.KEYCODE_VOLUME_UP->{
                    block.invoke()
                }
            }
        }
    }


    /**官方自定义图像分析类*/
    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * 用于从图像平面缓冲区提取字节数组的Helper扩展函数
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    //将缓冲区回滚到零
            val data = ByteArray(remaining())
            get(data)   // 将缓冲区复制到字节数组中
            return data // 返回字节数组
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // 如果没有附加侦听器，则不需要执行分析
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // 跟踪分析的帧
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // 使用移动平均值计算FPS（每秒传输帧数）
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // 分析可能需要任意长的时间
            // 因为我们在不同的线程中运行，所以它不会暂停其他用例

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // 从回调对象提取图像数据
            val data = buffer.toByteArray()

            // 将数据转换为0-255范围内的像素值数组
            val pixels = data.map { it.toInt() and 0xFF }

            // 计算图像的平均亮度
            val luma = pixels.average()

            // 将平均亮度传递到回调函数
            listeners.forEach { it(luma) }

            image.close()
        }
    }


    /**！！！！！！！！！！！生命周期下要执行的对象绑定！！！！！！！！！！！重要！！！必须要调用的方法**/


    /**销毁时的生命周期要执行的内容，回收照相机相关的绑定到页面的对象（destroy）*/
    fun recycleCameraAsset(){
        //关闭线程中执行的函数体
        cameraExecutor?.shutdown()

        //取消监听器
        if(displayListener!=null)
        displayManager.unregisterDisplayListener(displayListener)

        if(volumeDownReceiver!=null)
        broadcastManager?.unregisterReceiver(volumeDownReceiver!!)
    }




}