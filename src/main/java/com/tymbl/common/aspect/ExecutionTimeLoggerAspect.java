package com.tymbl.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class ExecutionTimeLoggerAspect {

    @Around("execution(* com.tymbl..service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            
            if (executionTime > 1000) { // Log warning for slow methods (>1s)
                log.warn("Slow method execution detected - {}.{} took {} ms", 
                    className, methodName, executionTime);
            } else {
                log.info("Method execution - {}.{} took {} ms", 
                    className, methodName, executionTime);
            }
        }
    }
} 