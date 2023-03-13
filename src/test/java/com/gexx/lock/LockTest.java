package com.gexx.lock;

import com.gexx.lock.lock.Lock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
public class LockTest {

    @Resource(name = "mysqlLock")
    private Lock mysqlLock;
    @Resource(name = "zkLock")
    private Lock zkLock;
    @Resource(name = "redisLock")
    private Lock redisLock;


    @Test
    public void testMysqlLock() throws Exception {
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                mysqlLock.lock("lock", () -> {
                    // 可重入测试
                    mysqlLock.lock("lock", () -> {
                        System.out.println(String.format("time: %d, threadName: %s", System.currentTimeMillis(), Thread.currentThread().getName()));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                });
            }, "Thread-" + i);
            thread.start();
            thread.join();

        }


    }

    @Test
    public void testZkLocker() throws Exception {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                zkLock.lock("user_1", () -> {
                    try {
                        System.out.println(String.format("user_1 time: %d, threadName: %s", System.currentTimeMillis(), Thread.currentThread().getName()));
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }, "Thread-" + i).start();


        }
        for (int i = 100; i < 200; i++) {
            new Thread(() -> {
                zkLock.lock("user_2", () -> {
                    try {
                        System.out.println(String.format("user_2 time: %d, threadName: %s", System.currentTimeMillis(), Thread.currentThread().getName()));
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }, "Thread-" + i).start();


        }
        System.in.read();

    }


    @Test
    public void testRedisLock() throws Exception {
        for (int i = 0; i < 1000; i++) {
            new Thread(()->{
                redisLock.lock("lock", ()-> {
                    // 可重入锁测试
                    redisLock.lock("lock", ()-> {
                        System.out.println(String.format("time: %d, threadName: %s", System.currentTimeMillis(), Thread.currentThread().getName()));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                });
            }, "Thread-"+i).start();
        }

        System.in.read();
    }
}
