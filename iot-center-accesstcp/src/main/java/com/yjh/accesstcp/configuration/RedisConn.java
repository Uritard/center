package com.yjh.accesstcp.configuration;

import com.alibaba.druid.filter.config.ConfigTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Slf4j
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConn {
    @Autowired
    private ConfigurableEnvironment environment;

    private String host;

    private int port;

    private String password;
    private int database;

    private RedisProperties.Jedis jedis;

    private Duration timeout = Duration.ofMillis(6000);

    @Value("${spring.datasource.accesstcp.publicKey}")
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
            log.error(e.getMessage());
            this.password = password;
        }
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
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

    public RedisClusterConfiguration getClusterConfiguration() {

        MutablePropertySources propertySources = environment.getPropertySources();
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(propertySources.iterator().next());

        if (!clusterConfiguration.getPassword().isPresent()) {
            clusterConfiguration.setPassword(RedisPassword.of(getPassword()));
        } else {
            // 对密码尝试解密，若可以解密，则解密后传入
            String pwd = new String(clusterConfiguration.getPassword().get());
            try {
                pwd = ConfigTools.decrypt(publicKey, pwd);
                clusterConfiguration.setPassword(RedisPassword.of(pwd));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return clusterConfiguration;
    }
}

