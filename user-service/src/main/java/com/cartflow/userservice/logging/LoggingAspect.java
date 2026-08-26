package com.cartflow.userservice.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.cartflow.userservice.service..*) || " +
              "within(com.cartflow.userservice.controller..*)")
    public void applicationLayer() {}

    @Around("applicationLayer()")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String method    = pjp.getSignature().getName();
        long start       = System.currentTimeMillis();

        log.debug(">> {}.{}()", className, method);

        try {
            Object result = pjp.proceed();
            log.info("OK {}.{}() — {}ms", className, method, elapsed(start));
            return result;
        } catch (Exception ex) {
            log.error("FAIL {}.{}() — {}ms — {}", className, method, elapsed(start), ex.getMessage());
            throw ex;
        }
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }
}
