package com.cotaseguro.config;

import com.cotaseguro.dto.CustomerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CUSTOMERS_CACHE = "customers";

    private static final Duration CUSTOMERS_TTL = Duration.ofMinutes(10);

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer<CustomerResponse> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, CustomerResponse.class);

        RedisCacheConfiguration customersConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CUSTOMERS_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return builder -> builder.withCacheConfiguration(CUSTOMERS_CACHE, customersConfiguration);
    }

}
