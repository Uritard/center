package com.yjh.platform.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    @Autowired
    private RedisConn redisConn;

    private Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * 生产key的策略
     *
     * @return
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName()).append(":");
            sb.append(method.getName()).append(":");
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisConn.getHost(), redisConn.getPort());
        standaloneConfig.setDatabase(redisConn.getCacheDb());
        standaloneConfig.setPassword(RedisPassword.of(redisConn.getPassword()));

        // 使用缓存的默认配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 使用 GenericJackson2JsonRedisSerializer 作为序列化器
        config =
            config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(15));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>(8);
        cacheConfigurations.put("EMEC15", config.entryTtl(Duration.ofSeconds(15)));
        cacheConfigurations.put("EMEC60", config.entryTtl(Duration.ofSeconds(60)));

        RedisCacheManager.RedisCacheManagerBuilder builder =
            RedisCacheManager.builder(new JedisConnectionFactory(standaloneConfig)).cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations);

        return builder.build();
    }

    /**
     * redis 数据库连接池
     *
     * @return
     */
    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        logger.info("===============================REDIS=========================:{}|{}", redisConn.getHost(), redisConn.getPort());

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisConn.getHost(), redisConn.getPort());
        standaloneConfig.setDatabase(redisConn.getDatabase());
        standaloneConfig.setPassword(RedisPassword.of(redisConn.getPassword()));
        JedisClientConfiguration clientConfiguration = JedisClientConfiguration.builder()
            .readTimeout(redisConn.getTimeout())
            .usePooling()
            .poolConfig(redisConn.jedisPool())
            .build();
        return new JedisConnectionFactory(standaloneConfig, clientConfiguration);
    }

    /**
     * redisTemplate配置
     *
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.setHashValueSerializer(stringRedisSerializer);
        template.afterPropertiesSet();
        return template;

    }

    @Bean("redisTemplateForThree")
    public RedisTemplate<String, Object> redisTemplateForThree() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisConn.getHost(), redisConn.getPort());
        standaloneConfig.setDatabase(3);
        standaloneConfig.setPassword(RedisPassword.of(redisConn.getPassword()));

        JedisConnectionFactory factory = new JedisConnectionFactory(standaloneConfig);
        template.setConnectionFactory(factory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.setHashValueSerializer(stringRedisSerializer);
        template.afterPropertiesSet();
        return template;

    }
}
