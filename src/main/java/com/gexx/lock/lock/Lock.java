package com.gexx.lock.lock;

public interface Lock {
    void lock(String key, Runnable command);
}