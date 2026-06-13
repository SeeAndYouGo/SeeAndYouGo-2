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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.SeeAndYouGo.SeeAndYouGo.global.DateTimeFormatters.DATE;
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
    @Scheduled(cron = "${scheduler.visitor.daily-sync}")
    public void syncDBAndRedis() {
        logger.info("[VISITOR COUNT] Syncing DB and Redis data...");

        // Hash의 모든 field(날짜들) 가져오기
        Set<String> fields = todayRedisTemplate.keys(KEY_TODAY_VISITOR);
        LocalDate today = LocalDate.now();

        logger.info("[VISITOR COUNT] Found {} visitor dates in Redis", fields.size());

        // 날짜들을 파싱하고 정렬 (가장 이른 날짜부터)
        List<LocalDate> sortedDates = fields.stream()
                .map(field -> LocalDate.parse(field.toString(), DATE))
                .sorted()
                .collect(Collectors.toList());

        // Redis에 데이터가 없으면 오늘부터 시작, 있으면 가장 이른 날짜부터 시작
        LocalDate startDate = sortedDates.isEmpty() ? today : sortedDates.get(0);

        logger.info("[VISITOR COUNT] Processing from {} to {}", startDate, today);

        // 시작 날짜부터 오늘까지 DB의 모든 데이터와 sync
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(today)) {
            // DB에서 해당 날짜 데이터 조회
            Optional<VisitorCount> dbTodayData = visitorCountRepository.findByIsTotalFalseAndCreatedAt(currentDate);

            if (dbTodayData.isPresent()) {
                // DB에 데이터가 있는 날짜: 기존 sync 로직으로 안전하게 처리
                logger.info("[VISITOR COUNT] Found DB data for date: {}, syncing safely", currentDate);
                syncTodayAndTotal(currentDate);
            } else {
                // DB에도 데이터가 없는 날짜: 0으로 초기화
                logger.info("[VISITOR COUNT] No DB data, setting zero for date: {}", currentDate);
                setVisitorCount(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    @Transactional
    @Scheduled(fixedRateString = "${scheduler.visitor.backup-rate}")
    public void backupVisitorCount() {
        LocalDate today = LocalDate.now();
        syncTodayAndTotal(today);
    }

    private void setVisitorCount(LocalDate date) {
        logger.info("[VISITOR COUNT] Initializing visitor count to 0 for date: {}", date);

        // 오늘자 redis today를 0으로 세팅
        todayRedisTemplate.put(KEY_TODAY_VISITOR, date.toString(), "0");

        // DB에도 오늘자 today를 0으로 세팅
        VisitorCount todayVisitorCount = VisitorCount.from(0, date, false);
        visitorCountRepository.save(todayVisitorCount);

        // DB의 가장 최근 total을 가져와서 해당 날짜의 total로 세팅
        int totalCount = visitorCountRepository.findTopByIsTotalTrueOrderByCreatedAtDesc()
                .map(VisitorCount::getCount)
                .orElse(0);

        VisitorCount todayTotal = VisitorCount.from(totalCount, date, true);
        visitorCountRepository.save(todayTotal);
    }

    private void syncTodayAndTotal(LocalDate date) {
        syncToday(date);
        syncTotal(date);
    }

    private void syncTotal(LocalDate date) {
        logger.info("[VISITOR COUNT] Syncing total visitor count for date: {}", date);

        Optional<VisitorCount> dbTotalVisitorCount = visitorCountRepository.findByIsTotalTrueAndCreatedAt(date);
        LocalDate today = LocalDate.now();

        if (date.equals(today)) {
            // 오늘 날짜인 경우에만 Redis total과 비교하여 sync
            String redisTotal = totalRedisTemplate.get(KEY_TOTAL_VISITOR);
            logger.info("[VISITOR COUNT] Redis total count: {}", redisTotal);
            if(redisTotal == null) {
                redisTotal = "0";
            }

            int dbTotal = dbTotalVisitorCount.map(VisitorCount::getCount).orElse(0);
            logger.info("[VISITOR COUNT] DB total count for {}: {}", date, dbTotal);

            int resultCount = getResultValue(Integer.parseInt(redisTotal), dbTotal);
            logger.info("[VISITOR COUNT] Final total count after sync: {}", resultCount);
            totalRedisTemplate.set(KEY_TOTAL_VISITOR, String.valueOf(resultCount));

            // DB에 해당 날짜 total이 있으면 업데이트, 없으면 새로 생성
            if(dbTotalVisitorCount.isPresent()){
                logger.info("[VISITOR COUNT] Updating existing total entry for date: {}", date);
                VisitorCount vc = dbTotalVisitorCount.get();
                vc.updateCount(resultCount);
                visitorCountRepository.save(vc);
            }else{
                logger.info("[VISITOR COUNT] Creating new total entry for date: {}", date);
                VisitorCount visitorCountByDate = VisitorCount.from(resultCount, date, true);
                visitorCountRepository.save(visitorCountByDate);
            }
        } else {
            // 과거 날짜는 DB만 확인하여 정합성 유지 (Redis total은 건드리지 않음)
            if(dbTotalVisitorCount.isPresent()) {
                logger.info("[VISITOR COUNT] Past date {} DB total: {} (no Redis update)", date, dbTotalVisitorCount.get().getCount());
            } else {
                logger.info("[VISITOR COUNT] No DB total data for past date: {}, skipping", date);
            }
        }
    }

    private void syncToday(LocalDate date) {
        logger.info("[VISITOR COUNT] Syncing today visitor count for date: {}", date);

        String redisToday = todayRedisTemplate.get(KEY_TODAY_VISITOR, date.toString());
        Optional<VisitorCount> dbTodayVisitorCount = visitorCountRepository.findByIsTotalFalseAndCreatedAt(date);

        logger.info("[VISITOR COUNT] Redis today count: {}, DB today count: {}", redisToday, dbTodayVisitorCount.map(vc -> String.valueOf(vc.getCount())).orElse("null"));
        if (redisToday == null) {
            redisToday = "0";
        }

        int dbToday = dbTodayVisitorCount.map(VisitorCount::getCount).orElse(0);
        int resultCount = getResultValue(Integer.parseInt(redisToday), dbToday);
        logger.info("[VISITOR COUNT] Final today count after sync: {}", resultCount);

        todayRedisTemplate.put(KEY_TODAY_VISITOR, date.toString(), String.valueOf(resultCount));

        // DB에 기존 데이터가 있으면 업데이트, 없으면 새로 생성
        if (dbTodayVisitorCount.isPresent()) {
            VisitorCount vc = dbTodayVisitorCount.get();
            vc.updateCount(resultCount);
            visitorCountRepository.save(vc);
        } else {
            VisitorCount newTodayCount = VisitorCount.from(resultCount, date, false);
            visitorCountRepository.save(newTodayCount);
        }

    }

    private int getResultValue(int redisValue, int dbValue) {
        // redis >= db면 redis가 다운되지 않고 유의미한 값을 갖고 있으므로 return
        // 만약 redis < db면 redis가 초기화되었으므로 db값에서 더하고 return
        return redisValue >= dbValue ? redisValue : (dbValue + redisValue);
    }
}