package press.mizhifei.dentist.reporting.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration for Reporting Service
 * 
 * Implements multi-level caching strategy:
 * - L1 Cache: Caffeine (in-memory, fast access for frequently used data)
 * - L2 Cache: Redis (distributed, for larger datasets and cross-instance sharing)
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.caffeine.spec:maximumSize=1000,expireAfterWrite=30m}")
    private String caffeineSpec;

    @Value("${cache.redis.ttl:1800}")
    private long redisTtlSeconds;

    /**
     * Primary cache manager using Caffeine for high-performance local caching
     */
    @Primary
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Parse caffeine spec and configure
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats(); // Enable statistics for monitoring
        
        cacheManager.setCaffeine(caffeineBuilder);
        
        // Define cache names for different types of data
        cacheManager.setCacheNames(List.of(
            "reportTemplates",
            "analyticsQueries",
            "userPermissions",
            "clinicMetadata",
            "reportMetrics"
        ));
        
        log.info("Configured Caffeine cache manager with spec: {}", caffeineSpec);
        return cacheManager;
    }

    /**
     * Redis cache manager for distributed caching of larger datasets
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisTtlSeconds))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(config);

        // Configure specific TTL for different cache types
        builder.withCacheConfiguration("reportData", 
                config.entryTtl(Duration.ofHours(2)));
        builder.withCacheConfiguration("analyticsResults", 
                config.entryTtl(Duration.ofMinutes(15)));
        builder.withCacheConfiguration("scheduledReports", 
                config.entryTtl(Duration.ofHours(24)));
        builder.withCacheConfiguration("exportFiles", 
                config.entryTtl(Duration.ofHours(1)));

        log.info("Configured Redis cache manager with TTL: {} seconds", redisTtlSeconds);
        return builder.build();
    }

    /**
     * Cache configuration properties
     */
    @Bean
    public CacheProperties cacheProperties() {
        return CacheProperties.builder()
                .caffeineSpec(caffeineSpec)
                .redisTtlSeconds(redisTtlSeconds)
                .build();
    }

    /**
     * Cache properties holder
     */
    public static class CacheProperties {
        private final String caffeineSpec;
        private final long redisTtlSeconds;

        private CacheProperties(String caffeineSpec, long redisTtlSeconds) {
            this.caffeineSpec = caffeineSpec;
            this.redisTtlSeconds = redisTtlSeconds;
        }

        public static CachePropertiesBuilder builder() {
            return new CachePropertiesBuilder();
        }

        public String getCaffeineSpec() { return caffeineSpec; }
        public long getRedisTtlSeconds() { return redisTtlSeconds; }

        public static class CachePropertiesBuilder {
            private String caffeineSpec;
            private long redisTtlSeconds;

            public CachePropertiesBuilder caffeineSpec(String caffeineSpec) {
                this.caffeineSpec = caffeineSpec;
                return this;
            }

            public CachePropertiesBuilder redisTtlSeconds(long redisTtlSeconds) {
                this.redisTtlSeconds = redisTtlSeconds;
                return this;
            }

            public CacheProperties build() {
                return new CacheProperties(caffeineSpec, redisTtlSeconds);
            }
        }
    }
}
