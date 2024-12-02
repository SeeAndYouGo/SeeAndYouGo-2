package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorService {

    private final VisitorCountRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    // 오늘 DB에 임시 저장된 방문자수 데이터가 있다면 redis 캐시에 복구함 
    public void init() {
//        Optional<VisitorCount> backupVisitorCount = repository.findTodayTempData();
//        if (backupVisitorCount.isEmpty())
//            return;
//
//        VisitorCount data = backupVisitorCount.get();
//        LocalDateTime createdAt = data.getCreatedAt();
//        redisTemplate.opsForValue().set(Const.KEY_TOTAL_VISITOR_COUNT,
//                String.valueOf(data.getCount()));
//        log.info("=== Visitor Count Restore Done! count: {} of {}", data.getCount(), createdAt);
    }
}