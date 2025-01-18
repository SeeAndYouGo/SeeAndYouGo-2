package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorCount;
import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorCountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.SeeAndYouGo.SeeAndYouGo.visitor.Const.*;

@Component
@RequiredArgsConstructor
public class VisitorScheduler {
    private static final Logger logger = LoggerFactory.getLogger(VisitorScheduler.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final VisitorCountRepository repository;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetTodayVisitorCount() {
        // get Redis TODAY COUNT
        int countInRedis = getTodayVistorCountInRedis();

        // get DB today Count
        int countInDatabase = getTodayVisitorCountInDatabase();


        if (countInRedis > countInDatabase) {
            logger.info("[VISITOR COUNT] Redis data successfully backed up to DB.");
            VisitorCount entity = VisitorCount.from(countInRedis, false);
            repository.save(entity);
        }
        logger.info("[VISITOR COUNT] Today's data has been reset.");

        repository.deleteByIsTotalFalse();
        redisTemplate.delete(KEY_TODAY_VISITOR);
        redisTemplate.delete(PREFIX_VISITOR_IP);
        redisTemplate.delete(PREFIX_VISITOR_USER);
    }

    private int getTodayVisitorCountInDatabase() {
        int todayVisitorInDatabase = 0;
        Optional<VisitorCount> backupCount = repository.findRecentTodayBackup();
        if (backupCount.isPresent()) {
            todayVisitorInDatabase = backupCount.get().getCount();
        }
        return todayVisitorInDatabase;
    }

    private int getTodayVistorCountInRedis() {
        int todayVisitorInRedis;
        String countValue = redisTemplate.opsForValue().get(KEY_TODAY_VISITOR);
        if (countValue == null) {
            todayVisitorInRedis = 0;
        } else {
            todayVisitorInRedis = Integer.parseInt(countValue);
        }
        return todayVisitorInRedis;
    }

    @Scheduled(fixedRate = 60000 * 30) // 30분마다
    public void backupVisitorCount() {
        backupCount(KEY_TODAY_VISITOR, false);
        backupCount(KEY_TOTAL_VISITOR, true);
    }

    private void backupCount(String redisKey, boolean isTotal) {
        String tmp = redisTemplate.opsForValue().get(redisKey);

        if (tmp != null) {
            int cachedCount = Integer.parseInt(tmp);
            Optional<VisitorCount> optional = isTotal ? repository.findRecentTotalBackup() : repository.findRecentTodayBackup();

            if (optional.isEmpty()) {
                repository.save(VisitorCount.from(cachedCount, isTotal));
                return;
            }
            VisitorCount recentBackup = optional.get();

            if (recentBackup.getCount() > cachedCount) {
                logger.error("[ERROR] DB count is higher. Overwriting Redis.");
                redisTemplate.opsForValue().set(redisKey, String.valueOf(recentBackup.getCount()));
                return;
            }
            recentBackup.updateCount(cachedCount);
            repository.save(recentBackup);
        } else {
            logger.warn("[WARNING] No cached data for key {}.", redisKey);
            repository.findRecentTotalBackup().ifPresent(backupData ->
                    redisTemplate.opsForValue().set(redisKey, String.valueOf(backupData.getCount())));
        }
    }
}