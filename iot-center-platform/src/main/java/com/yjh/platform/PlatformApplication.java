package com.yjh.platform;

import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.device.service.TDeviceTypeImgService;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import com.yjh.platform.module.user.service.SysKeyService;
import com.yjh.platform.module.user.service.SysUserService;
import com.yjh.platform.module.user.service.TCameraInfoService;
import com.yjh.platform.module.user.service.TSysParamService;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootApplication(scanBasePackages = {"com.yjh.platform", "com.yjh.platform.common.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@EnableAsync   //开启异步
@EnableScheduling
public class PlatformApplication  implements CommandLineRunner {

    @Autowired
    private TSysParamService tSysParamService;
    @Autowired
    private TCameraInfoService tCameraInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysKeyService sysKeyService;
    @Autowired
    private TVoiceDeviceService tVoiceDeviceService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TDeviceTypeImgService tDeviceTypeImgService;
    @Value("${spring.websocket.send.url}")
    private String url;
    @Value("${spring.interface.api}")
    private String interfaceApi;

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
//        QuartzTask quartzTask = new QuartzTask();
//        quartzTask.setJobName("PlatformScheduler");
//        quartzTask.setJobGroup("Platform");
//        jobManager.addJob(quartzTask);
        redisTemplate.delete("AllRobotCode");
        sysKeyService.loadKeysToRedis();
        tSysParamService.insertIntoRedis(true);
        tCameraInfoService.intoRedis();
        tCameraInfoService.startKeepWatch();//开启摄像头守望位置任务
        sysUserService.insertIntoRedis();
        tVoiceDeviceService.registerAudioDevice();//声纹设备注册
        Constant.WEBSOCKET_URL = url;
        Constant.redisTemplate = redisTemplate;
        Constant.apiPermissions= Boolean.valueOf(interfaceApi);
        tDeviceTypeImgService.findPic();//本地启动把此行注掉
        CruiseRedisStorage.start(redisTemplate);
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
