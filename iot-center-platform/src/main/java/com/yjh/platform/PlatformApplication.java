package com.yjh.platform;

import com.rabbitmq.client.AMQP;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.tradio.RecordVoiceFileThread;
import com.yjh.platform.module.device.entity.TDeviceTypeImg;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfo;
import com.yjh.platform.module.device.service.TDeviceTypeImgService;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.service.SysUserService;
import com.yjh.platform.module.user.service.TCameraInfoService;
import com.yjh.platform.module.user.service.TSysParamService;
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
import org.springframework.session.data.redis.config.ConfigureRedisAction;



import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootApplication(scanBasePackages = {"com.yjh.platform", "com.yjh.platform.common.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
public class PlatformApplication  implements CommandLineRunner {

    @Autowired
    private TSysParamService tSysParamService;
    @Autowired
    private TCameraInfoService tCameraInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private TVoiceDeviceService tVoiceDeviceService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TDeviceTypeImgService tDeviceTypeImgService;
    @Value("${spring.websocket.send.url}")
    private String url;

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
        tCameraInfoService.intoRedis();
        sysUserService.insertIntoRedis();
        Constant.WEBSOCKET_URL = url;
        Constant.redisTemplate = redisTemplate;
        tDeviceTypeImgService.findPic();//本地启动把此行注掉
        //Start RecordVoiceFileThread
        List<VoiceDeviceAllInfo> voiceDeviceAllInfoList = tVoiceDeviceService.selectVoiceDeviceInfo();
        if (voiceDeviceAllInfoList.size()>0) {
            for (VoiceDeviceAllInfo voiceDeviceAllInfo:voiceDeviceAllInfoList) {
                RecordVoiceFileThread recordVoiceFileThread = new RecordVoiceFileThread(redisTemplate, voiceDeviceAllInfo.getPort(),
                        voiceDeviceAllInfo.getVoiceDeviceId(), voiceDeviceAllInfo.getChannelNum(), voiceDeviceAllInfo.getFtpUrl(),
                        voiceDeviceAllInfo.getOwner(), voiceDeviceAllInfo.getOwnerCode(), true);
                Thread thread = new Thread(recordVoiceFileThread);
                thread.setDaemon(true);
                thread.start();
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
}
