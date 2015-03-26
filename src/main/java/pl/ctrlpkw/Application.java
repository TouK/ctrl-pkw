package pl.ctrlpkw;

import com.cloudinary.Cloudinary;
import com.google.common.cache.CacheBuilder;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.ws.rs.ApplicationPath;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
@EnableBatchProcessing
@Slf4j
public class Application {

    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory();
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        GuavaCacheManager cacheManager = new GuavaCacheManager();
        cacheManager.setCacheBuilder(CacheBuilder.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(100));
        return cacheManager;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    @Bean
    public CacheManager redisCacheManager(RedisTemplate redisTemplate) {
        RedisConnection connection = null;
        try {
            connection = redisTemplate.getConnectionFactory().getConnection();
            RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
            cacheManager.setDefaultExpiration(3000);
            return cacheManager;

        } catch (Exception ex) {
            log.warn("Not connected to Redis");
            return cacheManager();
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Bean
    public BeanConfig swaggerConfig() {
        BeanConfig config = new BeanConfig();
        config.setResourcePackage("pl.ctrlpkw.api");
        config.setVersion(Application.class.getPackage().getSpecificationVersion());
        config.setBasePath(JerseyConfig.class.getAnnotation(ApplicationPath.class).value());
        config.setTitle(Application.class.getPackage().getSpecificationTitle());
        config.setScan(true);
        return config;
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
