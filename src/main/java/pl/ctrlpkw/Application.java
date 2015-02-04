package pl.ctrlpkw;

import com.google.common.cache.CacheBuilder;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
@EnableBatchProcessing
public class Application {

    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory();
    }

    @Bean
    public CacheManager cacheManager() {
        GuavaCacheManager cacheManager = new GuavaCacheManager();
        cacheManager.setCacheBuilder(CacheBuilder.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(100));
        return cacheManager;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
