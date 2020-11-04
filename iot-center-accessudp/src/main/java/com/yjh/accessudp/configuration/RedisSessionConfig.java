package com.yjh.accessudp.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @Description
 * @Author tt
 * @Date 2019/5/7
 **/
@Configuration
@EnableRedisHttpSession
public class RedisSessionConfig {
}
