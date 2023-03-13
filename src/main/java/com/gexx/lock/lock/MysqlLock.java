package com.gexx.lock.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * mySQL 分布式锁
 */
@Component
@Slf4j
public class MysqlLock implements Lock {

    private static final ThreadLocal<SqlSessionWrapper> localSession = new ThreadLocal<>();

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Override
    public void lock(String key, Runnable command) {
        // 加锁、释放锁必须使用同一个session
        SqlSessionWrapper sqlSessionWrapper = localSession.get();
        if (sqlSessionWrapper == null) {
            // 第一次获取锁
            localSession.set(new SqlSessionWrapper(sqlSessionFactory.openSession()));
        }
        try {
            // -1表示没获取到锁一直等待
            if (getLock(key, -1)) {
                command.run();
            }
        } catch (Exception e) {
            log.error("lock error", e);
        } finally {
            releaseLock(key);
        }
    }

    private boolean getLock(String key, long timeout) {
        Map<String, Object> param = new HashMap<>();
        param.put("key", key);
        param.put("timeout", timeout);
        SqlSessionWrapper sqlSessionWrapper = localSession.get();
        Integer result = sqlSessionWrapper.sqlSession.selectOne("LockMapper.getLock", param);
        if (result != null && result.intValue() == 1) {
            // 获取到了锁，state加1
            sqlSessionWrapper.state++;
            return true;
        }
        return false;
    }

    private boolean releaseLock(String key) {
        SqlSessionWrapper sqlSessionWrapper = localSession.get();
        Integer result = sqlSessionWrapper.sqlSession.selectOne("LockMapper.releaseLock", key);
        if (result != null && result.intValue() == 1) {
            // 释放锁成功，state减1
            sqlSessionWrapper.state--;
            // 当state减为0的时候说明当前线程获取的锁全部释放了，则关闭session并从ThreadLocal中移除
            if (sqlSessionWrapper.state == 0) {
                sqlSessionWrapper.sqlSession.close();
                localSession.remove();
            }
            return true;
        }
        return false;
    }


    private static class SqlSessionWrapper {
        int state;
        SqlSession sqlSession;

        public SqlSessionWrapper(SqlSession sqlSession) {
            this.state = 0;
            this.sqlSession = sqlSession;
        }
    }
}
