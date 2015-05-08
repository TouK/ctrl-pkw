package pl.ctrlpkw;

import com.cloudinary.Cloudinary;
import com.google.common.cache.CacheBuilder;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
import com.wordnik.swagger.model.AuthorizationType;
import com.wordnik.swagger.model.GrantType;
import com.wordnik.swagger.model.ImplicitGrant;
import com.wordnik.swagger.model.LoginEndpoint;
import com.wordnik.swagger.model.OAuthBuilder;
import io.undertow.server.handlers.ProxyPeerAddressHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.ctrlpkw.api.dto.BallotResult;

import javax.ws.rs.ApplicationPath;
import java.util.Arrays;
import java.util.List;
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
    public ComparableVersion requiredClientVersion(@Value("${api.client.requiredVersion}") String value) {
        return new ComparableVersion(value);
    }

    public boolean stormpathEnabled(@Value("${stormpath.enabled:false}") boolean flag) {
        return flag;
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        GuavaCacheManager cacheManager = new GuavaCacheManager();
        cacheManager.setCacheBuilder(CacheBuilder.newBuilder()
                        .expireAfterWrite(300, TimeUnit.SECONDS)
                        .maximumSize(50000)
        );
        return cacheManager;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(cf);
        redisTemplate.setKeySerializer(redisTemplate.getStringSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(BallotResult.class));
        return redisTemplate;
    }

    @Bean
    public CacheManager redisCacheManager(RedisTemplate redisTemplate) {
        RedisConnection connection = null;
        try {
            connection = redisTemplate.getConnectionFactory().getConnection();
            RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
            cacheManager.setUsePrefix(true);
            //cacheManager.setDefaultExpiration(0);
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

        List<GrantType> grantTypes = Arrays.asList(
                new ImplicitGrant(
                        new LoginEndpoint("http://touk.pl"),
                        "access_token"
                )
        );
        AuthorizationType oauth = new OAuthBuilder()
                .grantTypes(grantTypes)
                .build();
        ConfigFactory.config().addAuthorization(oauth);

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

    @Bean
    public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
        factory.addDeploymentInfoCustomizers(deploymentInfo ->
                deploymentInfo.addInitialHandlerChainWrapper(ProxyPeerAddressHandler::new)
        );
        return factory;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
