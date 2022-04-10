package com.yjh.platform;

import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.device.service.TDeviceTypeImgService;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
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
public class PlatformApplication  implements CommandLineRunner {

    @Autowired
    private TSysParamService tSysParamService;
    @Autowired
    private TCameraInfoService tCameraInfoService;
    @Autowired
    private SysUserService sysUserService;
//    @Autowired
//    private TVoiceDeviceService tVoiceDeviceService;
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
        tSysParamService.insertIntoRedis();
        tCameraInfoService.intoRedis();
        tCameraInfoService.startKeepWatch();//开启摄像头守望位置任务
        sysUserService.insertIntoRedis();
        Constant.WEBSOCKET_URL = url;
        Constant.redisTemplate = redisTemplate;
        Constant.apiPermissions= Boolean.valueOf(interfaceApi);
        tDeviceTypeImgService.findPic();//本地启动把此行注掉
        //开机自动将所有拾音器开启状态转为关闭
        Set voiceKeys = redisScan("is_record_open_state:*");
        List voiceList = redisTemplate.executePipelined(
                new SessionCallback<Object>() {
                    @Override
                    public <K, V> Object execute(RedisOperations<K, V> redisOperations) throws DataAccessException {
                        for (Object key : voiceKeys) {
                            redisTemplate.opsForHash().entries(key);
                        }
                        return null;
                    }});
        if (voiceList.size()>0) {
            int voiceListLen = voiceList.size();
            for (int i=0; i<voiceListLen; i++) {
                Map deviceMap = (Map) voiceList.get(i);
                deviceMap.replace("openState", "关闭");
                redisTemplate.opsForHash().putAll("is_record_open_state:"+deviceMap.get("voiceDeviceId"), deviceMap);
            }
        }
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

    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
}
