package com.yjh.gateway.configuration;

import com.alibaba.druid.filter.config.ConfigTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConn {
    private String host;

    private int port;

    private String password;
    private int database;

    @Value("${spring.datasource.gateway.publicKey}")
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
}

