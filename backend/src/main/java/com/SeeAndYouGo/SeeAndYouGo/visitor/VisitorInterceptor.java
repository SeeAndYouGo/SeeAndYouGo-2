package com.SeeAndYouGo.SeeAndYouGo.visitor;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDate;

import static com.SeeAndYouGo.SeeAndYouGo.config.Const.KEY_OF_TOKEN_ID;


@Component
@Slf4j
@RequiredArgsConstructor
public class VisitorInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(VisitorInterceptor.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // ip키 생성
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String ipKey = Const.PREFIX_VISITOR_IP + getIp(request);

        // 유저키 생성
        String user = "unknown";
        String tokenId = request.getHeader(KEY_OF_TOKEN_ID);
        if (tokenId != null && !tokenId.isEmpty())
            user = tokenProvider.decodeToEmailByAccess(tokenId);
        String userRedisKey = Const.PREFIX_VISITOR_USER + user;

        // 방문자 수 증가시키기
        if (ops.get(userRedisKey) == null) {
            if (user.equals("unknown")) {
                if (ops.get(ipKey) == null) {
                    increase();
                    logger.debug("counting visitor: {}, {}", "unknown", ipKey);
                }
            } else {
                increase();
                logger.debug("counting visitor: {}, {}", user, ipKey);
            }
        }

        // user, ip 방문처리
        ops.set(ipKey, "1", Duration.ofMinutes(10));
        if (!userRedisKey.equals(Const.PREFIX_VISITOR_USER + "unknown"))
            ops.set(userRedisKey, "1", Duration.ofMinutes(10));

        // 참고: 이미 기록이 존재하는 public ip & 서로 다른 익명유저일 경우,
        //      "왜 방문자수 카운팅이 안되는거지?" 할 수도 있음
        return true;
    }

    public String getIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_CLIENT_IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();

        return ip;
    }

    private void increase() {
        HashOperations<String, LocalDate, Integer> todayOps = redisTemplate.opsForHash();
        todayOps.increment(Const.KEY_TODAY_VISITOR, LocalDate.now(), 1);

        ValueOperations<String, String> totalOps = redisTemplate.opsForValue();
        totalOps.increment(Const.KEY_TOTAL_VISITOR);
    }
}