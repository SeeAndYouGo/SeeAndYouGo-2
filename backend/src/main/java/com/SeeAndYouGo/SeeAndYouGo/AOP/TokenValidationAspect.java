package com.SeeAndYouGo.SeeAndYouGo.AOP;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.SeeAndYouGo.SeeAndYouGo.Config.Const.TOKEN_ID_KEY_NAME;

@Aspect
@Component
public class TokenValidationAspect {
    private final TokenProvider provider;

    @Autowired
    public TokenValidationAspect(TokenProvider provider) {
        this.provider = provider;
    }

    @Before("@annotation(ValidateToken)")
    public void validateToken(JoinPoint joinPoint) throws InvalidTokenException {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String tokenId = null;
        for (int i = 0; i < args.length; i++) {
            if (paramNames[i].equals(TOKEN_ID_KEY_NAME)) {
                tokenId = (String) args[i];
                break;
            }
        }

        if (tokenId == null || tokenId.length() == 0) {
            // token이 있을때와, 익명이라 token이 없을 때 같은 메서드를 사용중이라서 이 Aspect가 공통 적용되고 있다.
            // tokenId가 넘어오지 않으므로 정상으로 간주해야 한다.
            return;
        }
        if (!provider.validateToken(tokenId)) {
            throw new InvalidTokenException("Invalid Token");
        }
    }
}