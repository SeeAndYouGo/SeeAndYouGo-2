package com.SeeAndYouGo.SeeAndYouGo.aop.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class 쫑알Aspect {
    private static final Logger logger = LoggerFactory.getLogger(쫑알Aspect.class);

    // 쫑알쫑알 api 제공 메서드에 대해서만 로그 남기는 중
    @Around("@annotation(TraceMethodLog)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 메서드 실행 전
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("[API_JJONGAL] 실행: " + methodName);

        // 실행부
        Object result;
        try {
            result = joinPoint.proceed();  // 이 호출로 인해 타겟 메서드가 실행된다.
        } catch (Throwable throwable) {
            logger.error("[API_JJONGAL] 에러:" + methodName, throwable);
            throw throwable;
        }

        // 종료부
        logger.info("[API_JJONGAL] 완료: " + methodName);
        return result;
    }
}