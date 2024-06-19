package com.yjh.platform.configuration;

import com.alibaba.druid.filter.config.ConfigTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConn {
    private String host;

    private int port;

    private String password;
    private int database;

    private int cacheDb = 5;

    private RedisProperties.Jedis jedis;

    private Duration timeout = Duration.ofMillis(6000);

    @Value("${spring.datasource.platform.publicKey}")
    private String publicKey;

    @Value("${spring.redis.password}")
    private String passwordSec;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        try {
            this.password = ConfigTools.decrypt(publicKey, passwordSec);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public int getCacheDb() {
        return cacheDb;
    }

    public RedisConn setCacheDb(int cacheDb) {
        this.cacheDb = cacheDb;
        return this;
    }

    public JedisPoolConfig jedisPool() {
        RedisProperties.Pool pool = this.jedis.getPool();

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if (pool.getMaxWait() != null) {
            config.setMaxWaitMillis(pool.getMaxWait().toMillis());
        }
        return config;
    }

    public RedisConn setJedis(RedisProperties.Jedis jedis) {
        this.jedis = jedis;
        return this;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public RedisConn setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
}

