package com.yjh.platform;

import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.user.service.TSysParamService;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"com.yjh.platform", "com.yjh.platform.common.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients 
public class PlatformApplication  implements CommandLineRunner {

    @Autowired
    private TSysParamService tSysParamService;
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        return redisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
//        QuartzTask quartzTask = new QuartzTask();
//        quartzTask.setJobName("PlatformScheduler");
//        quartzTask.setJobGroup("Platform");
//        jobManager.addJob(quartzTask);
        tSysParamService.insertIntoRedis();
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]");
            }
        });
        return factory;
    }

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }
}
