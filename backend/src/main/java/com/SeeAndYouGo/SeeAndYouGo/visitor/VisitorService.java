package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.SeeAndYouGo.SeeAndYouGo.visitor.Const.KEY_TODAY_VISITOR;
import static com.SeeAndYouGo.SeeAndYouGo.visitor.Const.KEY_TOTAL_VISITOR;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorService {

    private final VisitorCountRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    // 오늘 DB에 임시 저장된 방문자수 데이터가 있다면 redis 캐시에 복구함 
    public void init() {
        restoreVisitorCountToRedis(true);
        restoreVisitorCountToRedis(false);
    }

    private void restoreVisitorCountToRedis(boolean isTotal) {
        Optional<VisitorCount> recentBackup;
        if (isTotal) recentBackup = repository.findRecentTotalBackup();
        else recentBackup = repository.findRecentTodayBackup();

        if (recentBackup.isEmpty()) return;

        int count = recentBackup.get().getCount();
        String redisKey = isTotal ? KEY_TOTAL_VISITOR : KEY_TODAY_VISITOR;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(count));

        log.info("[VISITOR] restore from DB to Redis. Backup date: {}", recentBackup.get().getCreatedAt());    }
}