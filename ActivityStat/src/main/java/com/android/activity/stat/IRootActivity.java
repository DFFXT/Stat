package com.android.activity.stat;

import android.app.Activity;

/**
 * author:yychai2
 * create on:星期一 2020/12/07
 * description: activity 所属的module的处理器
 */
public interface IRootActivity<T> {

    T getActivityInfo(Activity activity);
}
