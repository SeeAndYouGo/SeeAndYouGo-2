package com.SeeAndYouGo.SeeAndYouGo.menu.mainCache;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 통합 테스트를 위한 설정
 *
 * @DataJpaTest는 기본적으로 Redis Bean을 로드하지 않으므로,
 * 이 설정 클래스를 @Import하여 Redis 관련 Bean을 수동으로 등록합니다.
 *
 * localhost:6379의 Redis 서버를 사용합니다.
 */
@TestConfiguration
public class RedisLocalTestConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6379);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public NewDishCacheService newDishCacheService(
            RedisTemplate<String, Object> redisTemplate,
            com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository menuRepository
    ) {
        return new NewDishCacheService(redisTemplate, menuRepository);
    }
}
