package com.SeeAndYouGo.SeeAndYouGo.visitor;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

import static com.SeeAndYouGo.SeeAndYouGo.Config.Const.TOKEN_ID_KEY_NAME;


@Component
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
        String ipKey = Const.PREFIX_VISITOR_IP + ipToInt(getIp(request));

        // 유저키 생성
        String user = "unknown";
        String tokenId = (String) request.getSession().getAttribute(TOKEN_ID_KEY_NAME);
        if (tokenId != null && tokenId.length() != 0)
            user = tokenProvider.decodeToEmail(tokenId);
        String userKey = Const.PREFIX_VISITOR_USER + user;

        // ip 방문 이력 없을경우
        if (ops.get(ipKey) == null) {
            // 카운팅 조건: 익명 유저거나, 유저 접속이력이 없는 경우
            if (userKey.equals(Const.PREFIX_VISITOR_USER + "unknown")
                || ops.get(userKey) == null) {
                ops.increment(Const.KEY_TOTAL_VISITOR_COUNT);
            }
        }

        // 현재 접속한 user, ip의 방문처리
        ops.set(ipKey, "1", Duration.ofMinutes(10));
        ops.set(userKey, "1", Duration.ofMinutes(10));

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

    public static int ipToInt(String ip) {
        String[] octets = ip.split("\\.");
        int result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("IP address octets must be in the range 0-255");
            }
            result |= (octet << (8 * (3 - i))); // 8비트씩 왼쪽으로 이동
        }
        return result;
    }
}