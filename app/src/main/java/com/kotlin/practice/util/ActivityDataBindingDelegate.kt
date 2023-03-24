package com.kotlin.practice.util

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 描述:本地反射实现代理
 * 功能介绍:代理获取对应的生成ViewBinding。核心是将activity的绑定操作，在代理类中实现
 * 创建者:翁益亨
 * 创建日期:2023/2/24 10:48
 */
inline fun <reified T : ViewBinding> AppCompatActivity.binding() =
    ActivityDataBindingDelegate(T::class.java, this)

inline fun <reified T : ViewBinding> FragmentActivity.binding() =
    ActivityDataBindingDelegate(T::class.java, this)

inline fun <reified T : ViewBinding> Activity.binding() =
    ActivityDataBindingDelegate(T::class.java, this)

class ActivityDataBindingDelegate<T : ViewBinding>(
    classes: Class<T>,
    act: Activity
) : ReadOnlyProperty<Activity, T> {

    init {
        when (act) {
            is FragmentActivity -> act.lifecycle.addObserver(LifeCalcyObserver())
            is AppCompatActivity -> act.lifecycle.addObserver(LifeCalcyObserver())
        }
    }

    private val layoutInflater = classes.getMethod("inflate", LayoutInflater::class.java)

    private var viewBinding: T? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): T {
        viewBinding?.also {
            return it
        }

        val bind = layoutInflater.invoke(null, thisRef.layoutInflater) as T
        thisRef.setContentView(bind.root)

        return bind.also { viewBinding = it }
    }

    inner class LifeCalcyObserver : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            val state = source.lifecycle.currentState
            if (state == Lifecycle.State.DESTROYED) {
                viewBinding = null
            }
        }

    }
}