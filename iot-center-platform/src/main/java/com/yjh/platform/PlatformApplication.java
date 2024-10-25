package com.yjh.platform;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.configuration.RedisUtil;
import com.yjh.platform.module.device.service.TDeviceTypeImgService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import com.yjh.platform.module.user.service.*;
import com.yjh.platform.netty.client.NettyClient;
import com.yjh.video.api.VideoConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

import javax.annotation.Resource;

@SpringBootApplication(scanBasePackages = {"com.yjh.platform", "com.yjh.platform.common.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@EnableAsync   //开启异步
@EnableScheduling
@Slf4j
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
    private TRobotInfoService tRobotInfoService;
    @Autowired
    private TDictBusinessService dictBusinessService;
    @Autowired
    private UPatrolTaskService uPatrolTaskService;

    private NettyClient nettyClient = new NettyClient();
    @Value("${spring.websocket.send.url}")
    private String url;
    @Value("${wvp.host}")
    private String wvpHost;
    @Autowired
    private TCameraPresetService tCameraPresetService;

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        try {
            RedisUtil.setRedisTemplate(redisTemplate);
            redisTemplate.delete("AllRobotCode");
            DictConvertUtil.DICT.loadDict(dictBusinessService);
            VideoConfig.custom(wvpHost);
            sysKeyService.loadKeysToRedis();
            tSysParamService.insertIntoRedis(true);
            tCameraInfoService.intoRedis();
            tCameraPresetService.startSilentTask();//开启摄像头静默任务
            sysUserService.insertIntoRedis();
            tVoiceDeviceService.registerAudioDevice();//声纹设备注册
            //区域信息加载到缓存
            tStdRegionService.loadRegionIntoRedis();
            tRobotInfoService.initAllRobotCode(); // RobotCode初始化
            // 初始化任务优先级
            uPatrolTaskService.taskPriorityConfigToRedis();
            Constant.WEBSOCKET_URL = url;
            Constant.redisTemplate = redisTemplate;
            tDeviceTypeImgService.findPic();//本地启动把此行注掉
            CruiseRedisStorage.start(redisTemplate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

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

}
