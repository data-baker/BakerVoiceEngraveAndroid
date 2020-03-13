package com.baker.engrave.demo.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;

/**
 * 权限工具类
 *
 * Create by hsj55
 * 2020/3/6
 */
public class PermissionUtil {
    public static void needPermission(Fragment context, int reqCode, String... permissions) {
        needPermission(context.getActivity(), reqCode, permissions);
    }

    public static void needPermission(Activity context, int reqCode, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //6.0以下版本不需要代码申请权限
            return;
        }

        //检查权限
        boolean granted = hasPermission(context, permissions);
        if (granted) {
            //已获得权限
            executeSuccessResult(context, reqCode);
        } else {
            //申请权限
            ActivityCompat.requestPermissions(context, permissions, reqCode);
        }
    }

    private static void executeSuccessResult(Object context, int reqCode) {
        Method successMethod = getTargetMethod(context, reqCode, PermissionSuccess.class);
        try {
            successMethod.invoke(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeFailResult(Object context, int reqCode) {
        Method successMethod = getTargetMethod(context, reqCode, PermissionFail.class);
        try {
            successMethod.invoke(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Method getTargetMethod(Object context, int reqCode, Class annotation) {
        Method[] declaredMethods = context.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!method.isAccessible()) {
                //私有的方法必须强制
                method.setAccessible(true);
            }
            //判断方法上是否使用了目标注解
            boolean annotationPresent = method.isAnnotationPresent(annotation);
            if (annotationPresent) {
                //比较requestCode是否相等
                if (isTargetMethod(method, reqCode, annotation)) {
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean isTargetMethod(Method method, int reqCode, Class cls) {
        if (cls.equals(PermissionSuccess.class)) {
            return reqCode == method.getAnnotation(PermissionSuccess.class).requestCode();
        } else if (cls.equals(PermissionFail.class)) {
            return reqCode == method.getAnnotation(PermissionFail.class).requestCode();
        }
        return false;
    }


    public static boolean hasPermission(Context context, String... permissions) {
        for (String permission : permissions) {
            int granted = ContextCompat.checkSelfPermission(context, permission);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    public static void onRequestPermissionsResult(Fragment context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        onRequestPermissionsResult(context, requestCode, permissions, grantResults);
    }

    public static void onRequestPermissionsResult(Activity context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permissionGranted = true;
        for (int grant : grantResults) {
            if (grant == PackageManager.PERMISSION_DENIED) {
                permissionGranted = false;
                break;
            }
        }
        if (permissionGranted) {
            //获得权限
            executeSuccessResult(context, requestCode);
        } else {
            //权限被用户拒绝
            executeFailResult(context, requestCode);
        }
    }
}
