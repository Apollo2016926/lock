package com.gexx.lock.controller;

import com.gexx.lock.interceptor.RedisLockKey;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {

    @PostMapping("/hello")
    @RedisLockKey(key = "'hello:' + #map['id']")
    public String hello(@RequestBody Map map) {

        return "hello";
    }
}
