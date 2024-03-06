package com.SeeAndYouGo.SeeAndYouGo.AOP;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TokenValidationAspect {
    private final TokenProvider provider;
    private static final String TOKEN_ID_NAME = "tokenId";

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
            if (paramNames[i].equals(TOKEN_ID_NAME)) {
                tokenId = (String) args[i];
                break;
            }
        }

        if (tokenId == null || tokenId.length() == 0) {
            throw new InvalidTokenException("Invalid Token");
        }
        if (!provider.validateToken(tokenId)) {
            throw new InvalidTokenException("Invalid Token");
        }
    }
}