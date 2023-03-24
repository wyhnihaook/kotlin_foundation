package com.kotlin.practice.util

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kotlin.practice.base.BaseApp



/**
 * 描述:权限授权帮助类
 * 功能介绍:权限授权帮助类
 * 将所有权限相关的功能，挂载到ActivityResultCaller接口上（该接口处理的是注册请求，并返回结果信息）
 * registerForActivityResult中传递的是当前要处理的任务，内部实际是构建一个意图intent来处理
 * 创建者:翁益亨
 * 创建日期:2023/1/12 12:07
 */


//ActivityResultLauncher启动器，相当于startActivity
//ActivityResultContract协议，定义如何传递数据和接收数据

//权限分类 ： 1.安装时权限；2. 运行时权限；3.特殊权限；

/**
 * 申请权限后的相关操作
 * 特殊权限处理
 * */
interface IPermissionScope{
    fun appSetting()//设置页面跳转
    fun backLocation()//APP后台应用定位权限
    fun installPackage(callback:()->Unit)//允许安装未知应用开关
}

/**
 * 如果提前需要检测是否存在对应的硬件时，需要调用hasSystemFeature来判断
 * @param hardwareType 指的是需要检测的硬件类型 例如：PackageManager.FEATURE_CAMERA_FRONT
 * */
fun ActivityResultCaller.hasSystemFeature(hardwareType:String):Boolean = BaseApp.getContext().packageManager.hasSystemFeature(hardwareType)


/**
 * 获取上下文
 * */
fun ActivityResultCaller.context() =
    if (this is AppCompatActivity) this else (this as Fragment).requireActivity()


/**
 * 调用registerForActivityResult必须声明为成员变量或者在生命周期中声明（不能在点击事件中注册）
 * */


/**
 * 替换原有的注册权限结果响应功能，往方法参数里面添加回调函数，提高可用性
 *
 * @param onGranted 已授权回调
 * @param onDenied 拒绝授权回调
 * @param onShowRequestRationale 拒绝授权后，描述请求原因
 * */
fun ActivityResultCaller.registerForPermissionResult(
    onGranted:((String)->Unit)?=null,//已同意
    onDenied:((IPermissionScope?,String)->Unit)?=null,//拒绝
    onShowRequestRationale: ((IPermissionScope?, String) -> Unit)? = null,
    ):ActivityResultLauncher<String>{

    //额外情况处理
    val appSettingLauncher = appSettingsLauncher()
    val backLocationLauncher = backLocationLauncher(appSettingLauncher, onGranted)
    val fineLocationLauncher = fineLocationLauncher(appSettingLauncher, backLocationLauncher)
    val installPackageLauncher = installPackageLauncher()
    // 初始化Scope
    val permissionScope =generatorDefaultScope(context(),appSettingLauncher,fineLocationLauncher,backLocationLauncher,installPackageLauncher)

    return registerForActivityResult(RequestPermissionContract()){
        result->
        //这里处理的是，选择完授权弹窗的选项后，将结果返回

        //授权是否成功标识
        val (permissionName,authorizeStatus) = result
        when{
            authorizeStatus->onGranted?.let { it(permissionName) }
            permissionName.isNotEmpty()&&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context(),
                        permissionName
                    )->{
                        //触发到这里说明点击了禁止按钮

                        //未授权并且拒绝授权弹窗弹出的情况，能正常唤起授权弹窗（没有禁止弹出）
                        //这里处理的是，提示用户需要授权的原因，并且点击确认可以二次弹出授权窗口
                        onShowRequestRationale?.let { it(permissionScope,permissionName) }
                    }
            else->{
                //触发到这里一定是禁止授权弹窗弹起，执行对应到左
                onDenied?.let {
                    it(permissionScope,permissionName)
                }
            }
        }
    }
}

/**
 * 请求多个权限
 * */
fun ActivityResultCaller.registerForPermissionsResult(
    onAllGranted: ((List<String>) -> Unit)? = null,
    onDenied: ((IPermissionScope, List<String>) -> Unit)? = null,
    onShowRequestRationale: ((IPermissionScope, List<String>,List<String>) -> Unit)? = null
): ActivityResultLauncher<Array<String>> {
    // 初始化要用到的Launcher
    val appSettingLauncher = appSettingsLauncher()
    val backLocationLauncher = backLocationLauncher(appSettingLauncher) { s -> }
    val fineLocationLauncher = fineLocationLauncher(appSettingLauncher, backLocationLauncher)
    val installPackageLauncher = installPackageLauncher()
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultMap ->
        //返回的是key：权限名称  value：权限是否授权状态
        if (resultMap.containsValue(false)) {
            val permissionScope =
                generatorDefaultScope(
                    context(),
                    appSettingLauncher,
                    fineLocationLauncher,
                    backLocationLauncher,
                    installPackageLauncher
                )
            // 获得未授权权限列表、第一次拒绝列表
            val deniedList = mutableListOf<String>()
            for (entry in resultMap.entries) if (!entry.value) deniedList.add(entry.key)
            val explainableList = deniedList.filter {
                //过滤出能二次弹窗授权的权限类型
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context(),
                    it
                )
            }
            if (explainableList.isNotEmpty()) {
                // 不为空回调提示的方法，并回传了一个List，可以按需处理，如发起第二次授权请求
                onShowRequestRationale?.let { it -> it(permissionScope, explainableList,deniedList) }
            } else {
                //全部都不能二次弹窗授权情况
                // 回调拒绝权限的方法，同样回传未授权的List，可以做灵活判断
                onDenied?.let { it -> it(permissionScope, deniedList) }
            }
        } else {
            // 没有false说明授权通过
            onAllGranted?.let { it -> it(resultMap.keys.toList()) }
        }
    }
}



/**
 * 单个权限授权，启动ActivityResultLauncher启动器，尝试授权
 * */
fun ActivityResultLauncher<String>.launchX(permissionStr: String) {
    // 针对后台权限的特殊处理
    if (permissionStr == ACCESS_BACKGROUND_LOCATION) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            launch(ACCESS_FINE_LOCATION)
            return
        }
    } else if(permissionStr == REQUEST_INSTALL_PACKAGES) {
        //android 8.0出现的未知应用安装权限，对应的API版本为26。跳转到设置页面手动开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            launch(REQUEST_INSTALL_PACKAGES)
            return
        }
    }
    launch(permissionStr)
}

/**
 * 多个权限授权，特殊权限过滤
 * */
fun ActivityResultLauncher<Array<String>>.launchX(permissionsArray: Array<String>) {
    // 转化为集合去重
    val permissionsSet = permissionsArray.toMutableSet()
    // 针对后台权限的特殊处理
    if (permissionsSet.contains(ACCESS_BACKGROUND_LOCATION)) {
        // Android 10下直接移除这个权限，添加普通定位权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsSet.remove(ACCESS_BACKGROUND_LOCATION)
            permissionsSet.add(ACCESS_FINE_LOCATION)
        }
    }
    launch(permissionsSet.toTypedArray())
}


/**
 * 单个权限的协定，传入权限字符串，返回Pair<权限，授权结果>
 *     参考RequestMultiplePermissions类的实现。在基础上修改返回类型，构建成自己想要的格式
 * */
class RequestPermissionContract : ActivityResultContract<String, Pair<String, Boolean>>() {
    //权限字符串
    private lateinit var mPermission: String

    //创建intent意图，相当于调用startActivity时传递的intent
    override fun createIntent(context: Context, input: String): Intent {
        mPermission = input
        return Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS).putExtra(
            ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS, arrayOf(input)
        )
    }

    //构造结束返回值，复写时确认返回类型。相当于复写onActivityResult监听操作完毕返回时携带的参数
    override fun parseResult(resultCode: Int, intent: Intent?): Pair<String, Boolean> {
        if (intent == null || resultCode != Activity.RESULT_OK) return mPermission to false
        val grantResults =
            intent.getIntArrayExtra(ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS)
        return mPermission to
                if (grantResults == null || grantResults.isEmpty()) false
                else grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    //主要来处理不需要启动Activity就能获取预期结果的场景
    //RequestPermission和RequestMultiplePermissions里会用得到
    //能同步直接判断结果，那么就直接略过意图的创建和发送，直接返回（否则返回null，由后续程序执行意图的创建和发送）
    override fun getSynchronousResult(
        context: Context,
        input: String
    ): SynchronousResult<Pair<String, Boolean>>? = when {
            //没有检测到权限名称，直接返回授权失败（理论上这个情况不存在，兜底）
            null == input -> SynchronousResult("" to false)
            //如果检测到已授权，那么就直接返回授权成功标识
            ContextCompat.checkSelfPermission(
                context,
                input
            ) == PackageManager.PERMISSION_GRANTED -> {
                SynchronousResult(input to true)
            }
            else -> null
    }
}


/**
 * 跳转设置页面方法挂载
 * */
fun ActivityResultCaller.appSettingsLauncher() = registerForActivityResult(LaunchAppSettingsContract()){}

/**
 * 跳转到设置页面方法实现
 * PS：对于页面跳转结合 registerForActivity能有效减少startActivityForResult和onActivityResult结合使用产生较多代码的问题
 * */
class LaunchAppSettingsContract : ActivityResultContract<Unit, Unit>(){

    //构建意图->可以跳转到任意页面
    override fun createIntent(context: Context, input: Unit) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))

    //当前处理返回页面要携带的参数
    override fun parseResult(resultCode: Int, intent: Intent?) = Unit
}

/**
 * 跳转安装未知来源应用授权页面
 * */
fun ActivityResultCaller.installPackageLauncher()=registerForActivityResult(LaunchInstallPackageContract()){}

class LaunchInstallPackageContract:ActivityResultContract<Unit,Unit>(){

    override fun createIntent(context: Context, input: Unit): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.fromParts("package", context.packageName, null))
        } else {
            Intent()
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = Unit
}


/**
 * 定位特殊权限
 * 这里都使用ACCESS_BACKGROUND_LOCATION进行授权，内部处理ACCESS_FINE_LOCATION权限
 * Android 10.0 对应api为29
 * Android 10.0 以下，只需申请 ACCESS_FINE_LOCATION 权限，没有ACCESS_BACKGROUND_LOCATION权限
 * Android 10.0，可以同时申请ACCESS_FINE_LOCATION和ACCESS_BACKGROUND_LOCATION权限
 * Android 10.0 以上，必须先申请ACCESS_FINE_LOCATION（前台定位）权限并获得授权后，才能申请ACCESS_BACKGROUND_LOCATION（后台定位）权限，如果通知申请，不会弹窗，直接授权失败。
 * */


/**
 * 获取普通的定位权限
 * */
fun ActivityResultCaller.fineLocationLauncher(
    appSettingLauncher: ActivityResultLauncher<Unit>,
    backLocationLauncher: ActivityResultLauncher<String>
) = registerForActivityResult(ActivityResultContracts.RequestPermission()){
    if(it){
        //授权通过
        backLocationLauncher.launch(ACCESS_BACKGROUND_LOCATION)
    }else{
        //授权失败
        appSettingLauncher.launch(Unit)
    }
}

/**
 * 获取后台定位权限
 * */
fun ActivityResultCaller.backLocationLauncher(
    appSettingLauncher:ActivityResultLauncher<Unit>?=null,
    callback: ((String) -> Unit)?=null
) = registerForActivityResult(ActivityResultContracts.RequestPermission()){
    if(it){
        //执行后台定位逻辑
        callback?.let { it(ACCESS_BACKGROUND_LOCATION) }
    }else{
        //提示用户从设置中打开定位
        appSettingLauncher?.launch(Unit)
    }
}


/**
 * 生成默认权限后续处理
 * */
fun generatorDefaultScope(
    context: Context,
    appSettingLauncher: ActivityResultLauncher<Unit>,
    fineLocationLauncher: ActivityResultLauncher<String>,
    backLocationLauncher: ActivityResultLauncher<String>,
    installPackageLauncher: ActivityResultLauncher<Unit>,
) = object : IPermissionScope {

    override fun appSetting() {
        //弹窗提示跳转设置页面
        appSettingLauncher.launch(Unit)
    }

    override fun backLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selfPermission =
                ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
            when {
                selfPermission == PackageManager.PERMISSION_GRANTED -> backLocationLauncher.launch(
                    ACCESS_BACKGROUND_LOCATION
                )
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    ACCESS_FINE_LOCATION
                ) -> {
                    fineLocationLauncher.launch(ACCESS_FINE_LOCATION)
                }
                else -> {
                    //未授权定位基础权限，并且禁止弹窗显示
                    //这里按需实现弹窗，launch启动跳转对应的设置页面
                    fineLocationLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    override fun installPackage(callback: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                installPackageLauncher.launch(Unit)
            } else {
                callback()
            }
        }
    }
}