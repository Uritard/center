package com.yjh.gateway.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

        JedisConnectionFactory factory;

        RedisClusterConfiguration clusterConfiguration = redisConn.getClusterConfiguration();
        if (clusterConfiguration.getClusterNodes().isEmpty()) {
            factory = new JedisConnectionFactory(standaloneConfig, clientConfiguration);
        } else {
            factory = new JedisConnectionFactory(clusterConfiguration, clientConfiguration);
        }

        return factory;
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
}
