package com.gexx.lock.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.el.parser.ParseException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.xml.bind.ValidationException;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Slf4j
@Component
public class RedisLockInterceptor {

    @Autowired
    private RedissonClient redissonClient;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();


    @Around("@annotation(com.gexx.lock.interceptor.RedisLockKey)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedisLockKey redisLockKey = method.getAnnotation(RedisLockKey.class);
        if (redisLockKey.key().isEmpty()) {
            throw new ValidationException("锁名称不能为空！");
        }
        //加锁
        String lockName = parseLockKey(redisLockKey.key(), discoverer.getParameterNames(method), joinPoint.getArgs());
        RLock lock = redissonClient.getLock(lockName);
        lock.lock();
        try {
            Thread.sleep(100000);
            //执行业务代码
            return joinPoint.proceed();
        } finally {
            //释放锁
            lock.unlock();
        }
    }


    private String parseLockKey(String KeyExpress, String[] parameterNames, Object[] objects) throws Exception {
        if (Objects.isNull(objects) || objects.length == 0) {
            return KeyExpress;
        }
        try {
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
            for (int i = 0; i < objects.length; i++) {
                evaluationContext.setVariable(parameterNames[i], objects[i]);
            }
            Expression expression = expressionParser.parseExpression(KeyExpress);
            return expression.getValue(evaluationContext, String.class);
        } catch (Exception ex) {
            throw new Exception(String.format("解析表达式(%s)出错", KeyExpress)+ ex);
        }

    }


}
