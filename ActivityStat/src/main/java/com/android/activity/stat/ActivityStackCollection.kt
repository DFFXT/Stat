package com.android.activity.stat

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import android.util.Log

/**
 * @author feiqin
 * @date 2021/9/28-15:46
 * @description activity 信息
 * ·初次进入的activity必须能够通过name获取对应的信息
 * ·后续的activity优先通过name获取信息，无法获取信息就继承上一个activity的信息
 * ·activity的信息通过hashCode存储
 * ·activity的信息需要通过onActivitySaveInstanceState存储
 * ·信息来源优先级：handler获取 > bundle存储 > 信息继承 （被多个模块使用的activity不能设置为singleTask）
 * ·done
 */
open class ActivityStackCollection<T>(private val handlerRoot: IRootActivity<T>) : Application.ActivityLifecycleCallbacks {
    private val activityInfoKey = "_ActivityStackCollection_"
    private val activityInfo = ArrayList<Info<T>>(16)

    // 可见activity
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.i("ActivityStackCollection", "create:" + activity::class.java.simpleName)
        val info = handlerRoot.getActivityInfo(activity)
        if (info != null) {
            // 通过handler获取的信息
            activityInfo.add(
                Info<T>(activity.hashCode()).apply {
                    this.data = info
                }
            )
        } else {
            val bundleInfo = savedInstanceState?.getSerializable(activityInfoKey) as? Info<T>
            if (bundleInfo != null) {
                // 通过bundle存储的信息，一般重建activity用到
                bundleInfo.hashCode = activity.hashCode()
                activityInfo.add(bundleInfo)
            } else {
                // 通过继承前一个获取信息
                activityInfo.lastOrNull()?.clone()?.let {
                    it.hashCode = activity.hashCode()
                    activityInfo.add(it)
                }
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
        Log.i("ActivityStackCollection", "start:" + activity::class.java.simpleName)
    }

    override fun onActivityResumed(activity: Activity) {
        Log.i("ActivityStackCollection", "resume:" + activity::class.java.simpleName)
        val info = getActivityInfo(activity)
        if (info != null) {
            info.startTime = SystemClock.elapsedRealtime()
            info.pauseTime = info.startTime
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.i("ActivityStackCollection", "pause:" + activity::class.java.simpleName)
        val info = getActivityInfo(activity)
        if (info != null) {
            info.pauseTime = SystemClock.elapsedRealtime()
            // todo report
            Log.i("ActivityStackCollection", activity::class.java.simpleName + " cmid:" + info.data.toString() + " duration:" + (info.pauseTime - info.startTime))
        }
    }

    override fun onActivityStopped(activity: Activity) {
        Log.i("ActivityStackCollection", "stop:" + activity::class.java.simpleName)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        outState.putSerializable(activityInfoKey, getActivityInfo(activity))
        Log.i("ActivityStackCollection", "save:" + activity::class.java.simpleName)
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.i("ActivityStackCollection", "destroyed:" + activity::class.java.simpleName)
        removeActivityInfo(activity)
    }

    /**
     * 获取activity的信息
     */
    fun getActivityInfo(activity: Activity): Info<T>? {
        val hashCode = activity.hashCode()
        return activityInfo.find { it.hashCode == hashCode }
    }

    private fun removeActivityInfo(activity: Activity) {
        val hashCode = activity.hashCode()
        activityInfo.removeAll { it.hashCode == hashCode }
    }
}