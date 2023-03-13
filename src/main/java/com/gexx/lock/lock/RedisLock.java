package com.gexx.lock.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisLock implements Lock {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void lock(String key, Runnable command) {
        RLock lock = redissonClient.getLock(key);
        try {
            lock.lock();
            command.run();
        } finally {
            lock.unlock();
        }
    }
}
