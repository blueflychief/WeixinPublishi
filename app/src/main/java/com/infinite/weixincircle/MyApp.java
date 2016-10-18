package com.infinite.weixincircle;

import android.app.Application;

import com.infinite.weixincircle.utils.ToastUtils;



public class MyApp extends Application {
    public static MyApp sInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        ToastUtils.init(this);
    }

    public static MyApp getInstance() {
        return sInstance;
    }
}
