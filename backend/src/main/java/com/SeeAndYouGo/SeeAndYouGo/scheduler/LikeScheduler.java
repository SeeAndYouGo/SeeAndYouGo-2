package com.SeeAndYouGo.SeeAndYouGo.scheduler;

import com.SeeAndYouGo.SeeAndYouGo.like.Like;
import com.SeeAndYouGo.SeeAndYouGo.like.LikeRepository;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewRepository;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LikeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LikeScheduler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, String> redisTemplate;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Scheduled(fixedRate = 60000 * 30) // 30분마다 실행
    public void backupVisitorCount() {
        try {
            // Redis에서 review:like:* 패턴의 모든 키를 가져옴
            Set<String> keys = redisTemplate.keys("review:like:*");

            if (keys == null || keys.isEmpty()) {
                logger.info("Redis에 백업할 데이터가 없습니다.");
                return;
            }

            for (String key : keys) {
                // reviewId 추출 (키에서 'review:like:' 이후 부분만 가져옴)
                String reviewId = key.substring("review:like:".length());

                // Redis에서 해당 키의 해시 데이터를 가져옴 (email -> like 상태)
                Map<Object, Object> likeData = redisTemplate.opsForHash().entries(key);

                for (Map.Entry<Object, Object> entry : likeData.entrySet()) {
                    String userEmail = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    LikeResponseDto dto = objectMapper.readValue(value, LikeResponseDto.class);
                    if (!dto.isLike()) continue;

                    User user = userRepository.findByEmail(userEmail);
                    Review review = reviewRepository.findById(Long.parseLong(reviewId)).get();

                    if (user != null) {
                        Like like = new Like(review, user);
                        likeRepository.save(like);
                    } else {
                        logger.warn("User 또는 Review를 찾을 수 없습니다. UserEmail: {}, ReviewId: {}", userEmail, reviewId);
                    }
                }
            }
            logger.info("Redis 데이터를 성공적으로 DB에 백업했습니다.");
        } catch (Exception e) {
            logger.error("백업 과정에서 오류가 발생했습니다.", e);
        }
    }
}