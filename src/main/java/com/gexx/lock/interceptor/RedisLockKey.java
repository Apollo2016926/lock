package com.gexx.lock.interceptor;

import java.lang.annotation.*;

/*
默认30 秒过期，过期后会自动续期
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLockKey {
    /*
     *锁名称，支持SPEL表达式
     */
    String key();


}
