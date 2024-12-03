package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VisitorScheduler {
    private static final Logger logger = LoggerFactory.getLogger(VisitorScheduler.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final VisitorCountRepository repository;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetVisitorCount() {
        // save total visitor count to DB
        int totalCount = 0;
        try {
            String countValue = Objects.requireNonNull(redisTemplate.opsForValue().get(Const.KEY_TODAY_VISITOR));
            totalCount = Integer.parseInt(countValue);
        } catch (NullPointerException e) {
//            Optional<VisitorCount> backupCount = repository.findTodayTempData();
//            if (backupCount.isPresent()) {
//                totalCount = backupCount.get().getCount();
//            } else {
//                logger.error("[ERROR] empty today's visitor count. passing.");
//                return;
//            }
        }
        VisitorCount entity = VisitorCount.from(totalCount, true);
        repository.save(entity);

        // delete all backup data
        repository.deleteByIsTotalFalse();

        // reset redis visitor key
        redisTemplate.delete(Const.PREFIX_VISITOR_IP);
        redisTemplate.delete(Const.PREFIX_VISITOR_USER);

        // reset visitor count
        redisTemplate.delete(Const.KEY_TODAY_VISITOR);

        logger.info("========== visitor count reset completed! ============>");
    }

    // Redis 서버 다운을 대비한 DB 백업
    @Scheduled(fixedRate = 60000 * 30) // 30분마다
    public void backupVisitorCount() {
        String newCount = redisTemplate.opsForValue().get(Const.KEY_TODAY_VISITOR);
        if (newCount != null) {
            int cnt = Integer.parseInt(newCount);
//            Optional<VisitorCount> todayTempData = repository.findTodayTempData();
//            if (todayTempData.isPresent()) {
//                repository.updateCountForTodayTempData(cnt);
//            } else {
//                repository.save(VisitorCount.from(cnt, false));
//            }
        }
    }
}