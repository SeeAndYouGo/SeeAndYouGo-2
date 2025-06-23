package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visitors")
public class VisitorController {
    private final HashOperations<String, String, String> todayRedisTemplate;
    private final ValueOperations<String, String> totalRedisTemplate;

    @GetMapping("/count")
    public VisitorCountDto index() {
        LocalDate date = LocalDate.now();

        String today = todayRedisTemplate.get(Const.KEY_TODAY_VISITOR, date.toString());
        String total = totalRedisTemplate.get(Const.KEY_TOTAL_VISITOR);
        return new VisitorCountDto(total, today);
    }
}