package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorCount;
import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorCountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.SeeAndYouGo.SeeAndYouGo.visitor.Const.KEY_TODAY_VISITOR;
import static com.SeeAndYouGo.SeeAndYouGo.visitor.Const.KEY_TOTAL_VISITOR;

@Component
@RequiredArgsConstructor
public class VisitorScheduler {
    private static final Logger logger = LoggerFactory.getLogger(VisitorScheduler.class);

    private final HashOperations<String, String, String> todayRedisTemplate;
    private final ValueOperations<String, String> totalRedisTemplate;

    private final VisitorCountRepository visitorCountRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void syncDBAndRedis() {
        logger.info("[VISITOR COUNT] Resetting visitor data...");

        List<String> keys = new ArrayList<>(todayRedisTemplate.keys(KEY_TODAY_VISITOR));

        if(keys.size() > 1){
            logger.error("[VISITOR COUNT] Found more than one visitor data. Skipping...");
        }

        LocalDate today = LocalDate.now();

        if(keys.isEmpty()) {
            setVisitorCount(today);
            return;
        }

        LocalDate date = LocalDate.parse(keys.get(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // redis에 존재한 날짜의 데이터를 sync한다.
        syncTodayAndTotal(date);

        // 만약 redis가 오늘의 today를 갖고 있다면, 종료.
        if(date.isAfter(today)) return;

        // redis에 존재한 날짜의 redis today를 삭제한다.
        todayRedisTemplate.delete(KEY_TODAY_VISITOR, date.toString());

        // redis에 존재한 날짜 이후부터 오늘까지의 값을 0으로 세팅
        LocalDate targetDate = date.plusDays(1);

        while (!targetDate.isAfter(today)) {
            setVisitorCount(targetDate);
            targetDate = targetDate.plusDays(1);
        }
    }

    @Transactional
    @Scheduled(fixedRate = 60000 * 30)
    public void backupVisitorCount() {
        LocalDate today = LocalDate.now();
        syncTodayAndTotal(today);
    }

    private void setVisitorCount(LocalDate date) {

        // 오늘자 redis today를 0으로 세팅
        todayRedisTemplate.put(KEY_TODAY_VISITOR, date.toString(), "0");

        // DB에도 오늘자 today를 0으로 세팅
        VisitorCount todayVisitorCount = VisitorCount.from(0, false);
        visitorCountRepository.save(todayVisitorCount);

        // DB의 오늘자 total을 어제자 total과 동일하게 세팅
        VisitorCount yesterday = visitorCountRepository.findByIsTotalTrueAndCreatedAt(date.minusDays(1));
        VisitorCount today = VisitorCount.from(yesterday.getCount(), true);
        visitorCountRepository.save(today);
    }

    private void syncTodayAndTotal(LocalDate date) {
        syncToday(date);
        syncTotal(date);
    }

    private void syncTotal(LocalDate date) { // date는 visitor_count 테이블에 데이터가 없을 때만 쓰임.₩
        String redisTotal = totalRedisTemplate.get(KEY_TOTAL_VISITOR);
        VisitorCount dbTotalVisitorCount = visitorCountRepository.findTopByIsTotalTrueOrderByCreatedAtDesc();

        if(dbTotalVisitorCount == null) {
            dbTotalVisitorCount = VisitorCount.from(0, date, true);
        }

        if(redisTotal == null) {
            redisTotal = "0";
        }

        int dbTotal = dbTotalVisitorCount.getCount();

        int resultCount = getResultValue(Integer.parseInt(redisTotal), dbTotal);

        totalRedisTemplate.set(KEY_TOTAL_VISITOR, String.valueOf(resultCount));

        dbTotalVisitorCount.updateCount(resultCount);
        visitorCountRepository.save(dbTotalVisitorCount);
    }

    private void syncToday(LocalDate date) {
        String redisToday = todayRedisTemplate.get(KEY_TODAY_VISITOR, date.toString());
        VisitorCount dbTodayVisitorCount = visitorCountRepository.findByIsTotalFalseAndCreatedAt(date);

        if (dbTodayVisitorCount == null) {
            dbTodayVisitorCount = VisitorCount.from(0, date, false);
        }

        if (redisToday == null) {
            redisToday = "0";
        }

        int dbToday = dbTodayVisitorCount.getCount();
        int resultCount = getResultValue(Integer.parseInt(redisToday), dbToday);

        todayRedisTemplate.put(KEY_TODAY_VISITOR, date.toString(), String.valueOf(resultCount));

        dbTodayVisitorCount.updateCount(resultCount);
        visitorCountRepository.save(dbTodayVisitorCount);
    }

    private int getResultValue(int redisValue, int dbValue) {
        // redis >= db면 redis가 다운되지 않고 유의미한 값을 갖고 있으므로 return
        // 만약 redis < db면 redis가 초기화되었으므로 db값에서 더하고 return
        return redisValue >= dbValue ? redisValue : (dbValue + redisValue);
    }
}