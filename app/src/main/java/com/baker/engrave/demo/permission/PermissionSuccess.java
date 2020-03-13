package com.baker.engrave.demo.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识权限获取成功
 *
 * Create by hsj55
 * 2020/3/6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermissionSuccess {
    int requestCode();
}