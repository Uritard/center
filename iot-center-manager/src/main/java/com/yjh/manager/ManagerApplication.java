package com.yjh.manager;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.yjh.manager.common.Constant;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@EnableEurekaServer
@EnableDiscoveryClient
@RestController
@SpringBootApplication(scanBasePackages = {"com.yjh.manager", "org.springframework.cloud.netflix.zuul.filters"})
// @ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@EnableZuulProxy
@EnableScheduling
@EnableCircuitBreaker
public class ManagerApplication implements CommandLineRunner {
    @Value("${voice.data.receive.url}")
    private String voiceDataReceiveUrl;
    @Value("${video.stop.voice.url}")
    private String videoCloseVoice;
    @Value("${spring.interface.api}")
    private String interfaceApi;
    @Value("${eureka.client.serviceUrl.defaultZone}")
    String defaultZone;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ApplicationInfoManager applicationInfoManager;
    @Autowired
    private TaskScheduler taskScheduler;

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

    @Override
    public void run(String... strings) {
        Constant.VIDEO_RECEIVE_DATA = voiceDataReceiveUrl;
        Constant.VIDEO_CLOSE_VOICE = videoCloseVoice;
        Constant.apiPermissions= Boolean.valueOf(interfaceApi);
        Constant.IS_PCM_ENCODE = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isPcmEncode", "content"));
        defaultManager();
    }

    public void defaultManager() {
        PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();

        InstanceInfo instanceInfo = applicationInfoManager.getInfo();
        instanceInfo.setStatus(InstanceInfo.InstanceStatus.UP);
        registry.register(instanceInfo, false);

        taskScheduler.scheduleAtFixedRate(()->{
            try {
                registry.renew(instanceInfo.getAppName(), instanceInfo.getInstanceId(), false);
            } catch (Exception e) {
                // do nothing
            }
        }, Instant.now().plusSeconds(10), Duration.ofSeconds(10));
    }

    @RequestMapping("/defaultZone")
    public String getDefaultZone () {
        return defaultZone;
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> connector.setProperty("relaxedQueryChars", "|{}[]"));
        return factory;
    }

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
