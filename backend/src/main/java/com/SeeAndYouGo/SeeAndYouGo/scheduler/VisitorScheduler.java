package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorCount;
import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorCountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.SeeAndYouGo.SeeAndYouGo.visitor.Const.*;

@Component
@RequiredArgsConstructor
public class VisitorScheduler {
    private static final Logger logger = LoggerFactory.getLogger(VisitorScheduler.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final VisitorCountRepository repository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetTodayVisitorCount() {
        logger.info("[VISITOR COUNT] Resetting today's visitor data...");
        backupVisitorCount();
        cleanupOldTotalRecords();

        redisTemplate.delete(KEY_TODAY_VISITOR);
        repository.save(VisitorCount.from(0, false));
    }

    @Transactional
    @Scheduled(fixedRate = 60000 * 30)
    public void backupVisitorCount() {
        syncRedisWithDatabase(KEY_TODAY_VISITOR, false);
        syncRedisWithDatabase(KEY_TOTAL_VISITOR, true);
    }

    private void syncRedisWithDatabase(String redisKey, boolean isTotal) {
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        int countInRedis = (redisValue != null) ? Integer.parseInt(redisValue) : 0;

        if (isTotal) {
            // 총 방문자 수는 최대값 유지
            Optional<VisitorCount> dbRecord = repository.findTopByIsTotalTrueOrderByCountDesc();
            int countInDatabase = dbRecord.map(VisitorCount::getCount).orElse(0);

            int finalCount = Math.max(countInRedis, countInDatabase);
            repository.save(VisitorCount.from(finalCount, true));
            redisTemplate.opsForValue().set(redisKey, String.valueOf(finalCount));

            logger.info("[VISITOR] (synchronized total) {}: Count {}", redisKey, finalCount);
        } else {
            // 오늘 방문자 수는 Redis 값을 기준으로 동기화
            // 또는 오늘 날짜의 DB 레코드만 고려하도록 수정
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);

            Optional<VisitorCount> todayRecord = repository.findTopByIsTotalFalseAndCreatedAtBetweenOrderByCountDesc(
                    startOfDay, endOfDay);

            // 오늘 생성된 레코드가 있으면 더 큰 값 사용, 없으면 Redis 값만 사용
            int countToSave = todayRecord.isPresent()
                    ? Math.max(countInRedis, todayRecord.get().getCount())
                    : countInRedis;

            repository.save(VisitorCount.from(countToSave, false));
            redisTemplate.opsForValue().set(redisKey, String.valueOf(countToSave));

            logger.info("[VISITOR] (synchronized today) {}: Count {}", redisKey, countToSave);
        }
    }

    @Transactional
    private void cleanupOldTotalRecords() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<VisitorCount> oldRecords = repository.findByIsTotalTrueAndCreatedAtBefore(todayStart);

        if (!oldRecords.isEmpty()) {
            List<Long> excludeIds = oldRecords.stream()
                    .map(VisitorCount::getId)
                    .collect(Collectors.toList());

            repository.deleteByIsTotalTrueAndIdNotIn(excludeIds);
        }
    }
}