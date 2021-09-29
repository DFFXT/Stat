package com.android.activity.stat

import java.io.Serializable

class Info<T>(var hashCode: Int) : Serializable {
    var data: T? = null
    var startTime: Long = 0
    var pauseTime: Long = 0

    /**
     * 克隆对象
     */
    fun clone(): Info<T> {
        val info = Info<T>(hashCode)
        info.data = data
        info.startTime = startTime
        info.pauseTime = pauseTime
        return info
    }
}