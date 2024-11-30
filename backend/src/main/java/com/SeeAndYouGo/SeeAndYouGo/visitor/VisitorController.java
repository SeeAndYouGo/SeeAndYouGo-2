package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visitors")
public class VisitorController {
    private final RedisTemplate<String, String> redisTemplate;

    // 테스트용 API
    @GetMapping("/count")
    public VisitorCountDto index() {

        String count = redisTemplate.opsForValue().get(Const.KEY_TOTAL_VISITOR_COUNT);

        return new VisitorCountDto(count);
    }
}