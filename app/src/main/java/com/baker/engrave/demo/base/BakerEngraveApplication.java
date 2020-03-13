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
    }

    public static Context getContext() {
        return mApplication;
    }
}
