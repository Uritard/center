package com.yjh.logs;

import com.yjh.logs.common.Constant;
import com.yjh.logs.commons.restTemplate.ServiceRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = "com.yjh.logs")
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class, basePackages = "com.yjh")
public class LogsApplication implements CommandLineRunner {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${spring.websocket.send.url}")
    private String url;

    @Value("${spring.interface.api}")
    private String interfaceApi;

    public static void main(String[] args) {
        SpringApplication.run(LogsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Constant.WEBSOCKET_URL = url;
        Constant.redisTemplate = redisTemplate;
        Constant.apiPermissions= Boolean.valueOf(interfaceApi);
    }

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }
}
