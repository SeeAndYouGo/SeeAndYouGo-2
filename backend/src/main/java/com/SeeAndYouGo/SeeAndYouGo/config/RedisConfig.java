package com.SeeAndYouGo.SeeAndYouGo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    @Bean
    public RedisTemplate<String, byte[]> byteRedisTemplate() {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
//
//    @Bean("redisCacheManager")
//    public CacheManager redisCacheManager() {
//
//
//        return RedisCacheManager.RedisCacheManagerBuilder
//                .fromConnectionFactory(redisConnectionFactory())
////                .cacheDefaults(defaultConfiguration())
//                .withInitialCacheConfigurations(configureMap())
//                .build();
//    }


//    RedisCacheConfiguration은 그럼 어노테이션을 사용하지 않고 template에다가 put get 할때는 전혀 상관이 없어? Spring Cache만 관련있는거라서?
// 그렇다고 함


//    // 모든 키에 대한 기본값 설정
//    private RedisCacheConfiguration defaultConfiguration() {
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//    }


//    역할: RedisCacheConfiguration은 Spring Cache를 통해 Redis를 캐시로 사용할 때 설정을 제공합니다. 주로 캐시 관리 목적으로 사용되며, Spring에서 제공하는 캐시 추상화(@Cacheable, @CachePut, @CacheEvict 등)를 사용할 때 이 설정이 적용됩니다.
//    // 키별로 설정
//    private Map<String, RedisCacheConfiguration> configureMap() {
//        Map<String, RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>();
//
//        cacheConfigurationMap.put("menu:daily:", defaultConfiguration().entryTtl(Duration.ofHours(20)));
//        cacheConfigurationMap.put("menu:weekly:", defaultConfiguration().entryTtl(Duration.ofHours(20)));
//        cacheConfigurationMap.put("review:image:", defaultConfiguration().entryTtl(Duration.ofDays(7)));
//
//        // 이미지 직렬화기는 별도로 설정 (바이트 저장)
//        RedisCacheConfiguration imageCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));
//        cacheConfigurationMap.put("review:image:", imageCacheConfig);
//
//        return cacheConfigurationMap;
//    }
}