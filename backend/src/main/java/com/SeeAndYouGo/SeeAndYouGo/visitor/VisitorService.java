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

    // DB에 백업된 가장 최근 방문자수로 복구한다.
    // 단, 오늘 백업된 건이 없으면 아무 일도 하지 않는다.
    public void init() {
        Optional<VisitorCount> backupVisitorCount = repository.findTodayTempData();
        if (backupVisitorCount.isEmpty())
            return;

        VisitorCount data = backupVisitorCount.get();
        LocalDateTime createdAt = data.getCreatedAt();
        redisTemplate.opsForValue().set(Const.KEY_TOTAL_VISITOR_COUNT,
                String.valueOf(data.getCount()));
        log.info("=== Visitor Count Restore Done! count: {} of {}", data.getCount(), createdAt);
    }
}