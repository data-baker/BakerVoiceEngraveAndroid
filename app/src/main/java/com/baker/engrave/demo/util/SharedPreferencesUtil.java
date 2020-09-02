package com.baker.engrave.demo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.baker.engrave.demo.base.BakerEngraveApplication;

import java.util.UUID;

/**
 * Create by hsj55
 * 2020/3/10
 */
public class SharedPreferencesUtil {
    //保存在手机里面的文件名
    private static final String FILE_NAME = "bakerMouldInfo";
    private static final String FIELD_NAME = "query_id";

    /**
     * 将mould存在手机本地，方便体验自己的声音模型。
     */
    public static String getQueryId() {
        SharedPreferences sp = BakerEngraveApplication.getContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String queryId = sp.getString(FIELD_NAME, null);
        if (TextUtils.isEmpty(queryId)) {
            queryId = UUID.randomUUID().toString();
            sp.edit().putString(FIELD_NAME, queryId).apply();
        }
        return queryId;
    }
}
