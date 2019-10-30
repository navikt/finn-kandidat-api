package no.nav.tag.finnkandidatapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public final static String STS_CACHE = "sts_cache";
    public final static String ABAC_CACHE = "abac_cache";

    @Bean
    public CaffeineCache stsCache() {
        return new CaffeineCache(STS_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(59, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
    }

    @Bean
    public CaffeineCache abacCache() {
        return new CaffeineCache(ABAC_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(10000)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build());
    }
}
