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
    private static final String FIELD_NAME_QUERY_ID = "query_id";
    private static final String FIELD_NAME_CLIENT_ID = "client_id";
    private static final String FIELD_NAME_CLIENT_SECRET = "client_secret";

    /**
     * 将mould存在手机本地，方便体验自己的声音模型。
     */
    public static String getQueryId() {
        SharedPreferences sp = BakerEngraveApplication.getContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String queryId = sp.getString(FIELD_NAME_QUERY_ID, null);
        if (TextUtils.isEmpty(queryId)) {
//            queryId = UUID.randomUUID().toString();
            queryId = "93bc125a-7004-4892-968f-d53c4913840e";
            sp.edit().putString(FIELD_NAME_QUERY_ID, queryId).apply();
            return queryId;
        } else {
            return queryId;
        }
    }

    public static String getClientId() {
        return BakerEngraveApplication.getContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .getString(FIELD_NAME_CLIENT_ID, null);
    }

    public static String getClientSecret() {
        return BakerEngraveApplication.getContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .getString(FIELD_NAME_CLIENT_SECRET, null);
    }

    public static void saveClientId(String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            SharedPreferences sp = BakerEngraveApplication.getContext()
                    .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            sp.edit().putString(FIELD_NAME_CLIENT_ID, clientId).apply();
        }
    }

    public static void saveClientSecret(String clientId) {
        if (!TextUtils.isEmpty(clientId)) {
            SharedPreferences sp = BakerEngraveApplication.getContext()
                    .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            sp.edit().putString(FIELD_NAME_CLIENT_SECRET, clientId).apply();
        }
    }
}
