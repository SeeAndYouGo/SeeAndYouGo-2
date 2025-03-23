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
import java.util.Optional;

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

        Optional<VisitorCount> dbRecord = isTotal ?
                repository.findTopByIsTotalTrueOrderByCountDesc() :
                repository.findTopByIsTotalFalseOrderByCountDesc();
        int countInDatabase = dbRecord.map(VisitorCount::getCount).orElse(0);

        // save higher data
        int finalCount = Math.max(countInRedis, countInDatabase);
        repository.save(VisitorCount.from(finalCount, isTotal));
        redisTemplate.opsForValue().set(redisKey, String.valueOf(finalCount));

        logger.info("[VISITOR] (synchronized) {}: Count {}", redisKey, finalCount);
    }

    private void cleanupOldTotalRecords() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        repository.findByIsTotalTrueAndCreatedAtBefore(todayStart)
                .ifPresent(yesterdayRecord -> repository.deleteByIsTotalTrueAndIdNot(yesterdayRecord.getId()));
    }
}