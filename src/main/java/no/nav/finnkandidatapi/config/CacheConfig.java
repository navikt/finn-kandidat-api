package no.nav.finnkandidatapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String STS_CACHE = "sts_cache";

    @Bean
    public CaffeineCache stsCache() {
        return new CaffeineCache(STS_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(59, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
    }
}
