package com.kotlin.practice.ui.train

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kotlin.practice.R
import com.kotlin.practice.databinding.ActivityTrainRecordsBinding
import com.kotlin.practice.util.binding

/**
 * 描述:培训记录
 * 功能介绍:使用ViewBinding进行处理。这里将viewModel和viewBinding使用代理获取
 * 相较于DataBinding，作为显式的操作。
 * 比较DataBinding和ViewBinding比较
 * DataBinding是一个比较重的library, 因为需要编译期间annotation处理，所以需要花费更长的编译时间
 * DataBinding 会增大包的体积。一个简单的demo databinding比viewbinding大50kb左右，如果项目越大，区别肯定会更大
 * DataBinding支持数据双向绑定，但是用起来很容易陷入歧途，即在xml中加入过多的逻辑判断，这对代码维护是恐怖的，如果java和xml都写逻辑判断，后期维护起来是非常痛苦的（@{} 不会去做检查）
 *多模块依赖问题！！！！最严重！！！！
 * DataBinding在多模块开发的时候，有这样一个机制：如果子模块使用了 DataBinding，那么主模块也必须在 gradle 加上配置，不然就会报错；
 *如果主模块和子模块都添加上了 DataBinding 的配置，那么在编译时，子模块的 XML 文件产生的 Binding 类除了在自己的 build 里会有一份外，在主模块下也会有一份。
 *那么，如果主模块与子模块都有一个 layout 根目录的 activity_main.xml，主模块生成的 ActivityMainBinding 会是根据子模块的文件生成的！这种情况下就只能规范命名问题！！！！
 *
 *
 * 而ViewBinding相当于式DataBinding的子功能集（主要作为findViewById的语法糖。bind.组件id获取组件）
 * 通过视图绑定功能，您可以更轻松地编写可与视图交互的代码。在模块中启用视图绑定之后，系统会为该模块中的每个 XML 布局文件生成一个绑定类。绑定类的实例包含对在相应布局中具有 ID 的所有视图的直接引用
 *
 * 这里提供推荐使用ViewBinding的写法：1.编译速度加快。2.对xml没有侵入性。3.定位错误便捷。4.多模块自己管自己的内容
 * 创建者:翁益亨
 * 创建日期:2023/2/23 19:04
 */
//使用ViewBindingPropertyDelegate依赖库时，必须要使用传递布局id的构造函数（因为要通过布局id和）
//使用反射时可以不传递布局id
class TrainingRecordsActivity: AppCompatActivity(R.layout.activity_train_records){

    //显式的获取对应数据内容对象

    //需要自己实现或者依赖第三方代理
    //1.不反射的方式（推荐）- ViewBindingPropertyDelegate依赖库
    //自动生成的绑定类也并不复杂，主要就是两个inflate重载方法以及一个bind方法。我们获取的对viewId引用就是在bind方法中进行的，内部其实也是通过findViewById来获取相关view的。所以初始化要设置对应的布局id
    private val mBinding by viewBinding(ActivityTrainRecordsBinding::bind)
    //2.反射方式（会耗时）- 本地实现
//    private val mBinding:ActivityTrainRecordsBinding by binding()

    //系统自带代理方式获取
    private val mViewModel by viewModels<TrainingRecordsViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //官方默认写法
        //ActivityTrainRecordsBinding.inflate(layoutInflater) 获取binding实例。通过setContentView(binding.root)设置布局信息

        mBinding.includeTitle.text = mViewModel.title
        mBinding.includeTitle.onClick = View.OnClickListener {
            mViewModel.back(it)
        }

        mBinding.text.text="修改后的内容"
    }

}
