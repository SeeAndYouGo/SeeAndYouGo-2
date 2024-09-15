package com.SeeAndYouGo.SeeAndYouGo.visitor;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

import static com.SeeAndYouGo.SeeAndYouGo.Config.Const.TOKEN_ID_KEY_NAME;


@Component
@Slf4j
@RequiredArgsConstructor
public class VisitorInterceptor implements HandlerInterceptor {
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
        String tokenId = request.getHeader(TOKEN_ID_KEY_NAME);

        if (tokenId != null && tokenId.length() != 0)
            user = tokenProvider.decodeToEmail(tokenId);
        String userKey = Const.PREFIX_VISITOR_USER + user;

        // 방문자 수 증가시키기
        if (ops.get(userKey) == null) {
            if (user.equals("unknown")) {
                if (ops.get(ipKey) == null)
                    ops.increment(Const.KEY_TOTAL_VISITOR_COUNT);
            } else {
                ops.increment(Const.KEY_TOTAL_VISITOR_COUNT);
            }
        }

        // user, ip 방문처리
        ops.set(ipKey, "1", Duration.ofMinutes(10));
        if (!userKey.equals(Const.PREFIX_VISITOR_USER + "unknown"))
            ops.set(userKey, "1", Duration.ofMinutes(10));

        // 참고: 이미 기록이 존재하는 public ip & 서로 다른 익명유저일 경우,
        //      "왜 방문자수 카운팅이 안되는거지?" 할 수도 있음
        return true;
    }

    public String getIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_CLIENT_IP");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();

        return ip;
    }
}