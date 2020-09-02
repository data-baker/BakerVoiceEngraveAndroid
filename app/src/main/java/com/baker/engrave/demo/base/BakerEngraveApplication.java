package com.baker.engrave.demo.base;

import android.app.Application;
import android.content.Context;

import com.baker.engrave.demo.util.SharedPreferencesUtil;
import com.baker.engrave.lib.BakerVoiceEngraver;

/**
 * Create by hsj55
 * 2020/3/6
 */
public class BakerEngraveApplication extends Application {
    private static BakerEngraveApplication mApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        //初始化复刻SDK //QueryId的作用是xxx，非常建议设置。
        BakerVoiceEngraver.getInstance().initSDK(this, Constants.clientId, Constants.clientSecret, SharedPreferencesUtil.getQueryId());
    }

    public static Context getContext() {
        return mApplication;
    }
}