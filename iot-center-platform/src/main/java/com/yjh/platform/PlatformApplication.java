package com.yjh.platform;

import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.device.service.TDeviceTypeImgService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.patrol.service.AnalyseDataOperateService;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import com.yjh.platform.module.user.service.*;
import com.yjh.platform.netty.client.NettyClient;
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
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
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
    @Resource
    private TStdRegionService tStdRegionService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TDeviceTypeImgService tDeviceTypeImgService;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    @Autowired
    private TRobotInfoService tRobotInfoService;

    private NettyClient nettyClient = new NettyClient();
    @Value("${spring.websocket.send.url}")
    private String url;
    @Value("${spring.interface.api}")
    private String interfaceApi;
    /**
     * 表计识别分析端口
     */
    @Value("${netty.recognize.port}")
    private int recognizePort;
    /**
     * 缺陷分析端口
     */
    @Value("${netty.ai.port}")
    private int aiPort;
    /**
     * 算法服务端IP
     */
    @Value("${netty.server.url}")
    private String serverUrl;
    @Autowired
    private TCameraPresetService tCameraPresetService;

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
        tCameraPresetService.startSilentTask();//开启摄像头静默任务
        sysUserService.insertIntoRedis();
        tVoiceDeviceService.registerAudioDevice();//声纹设备注册
        //区域信息加载到缓存
        tStdRegionService.loadRegionIntoRedis();
        tRobotInfoService.initAllRobotCode(); // RobotCode初始化
        Constant.WEBSOCKET_URL = url;
        Constant.redisTemplate = redisTemplate;
        Constant.apiPermissions= Boolean.valueOf(interfaceApi);
        tDeviceTypeImgService.findPic();//本地启动把此行注掉
        CruiseRedisStorage.start(redisTemplate);

        // 算法暂时为http方式 先注释
        /*InetSocketAddress remoteAddress1 = new InetSocketAddress(serverUrl, recognizePort);
        InetSocketAddress remoteAddress2 = new InetSocketAddress(serverUrl, aiPort);
        nettyClient.start(remoteAddress1, remoteAddress2, redisTemplate, analyseDataOperateService, url);*/
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
    ServiceRestTemplate serviceRestTemplate() {
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
