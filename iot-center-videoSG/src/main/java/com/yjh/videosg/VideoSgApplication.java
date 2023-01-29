package com.yjh.videosg;

import com.yjh.videosg.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Created by tt on 2019/7/31
 */
@SpringBootApplication(scanBasePackages = "com.yjh.videosg")
@EnableDiscoveryClient
@Slf4j
@EnableAsync
@EnableScheduling
public class VideoSgApplication implements CommandLineRunner {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * sdk路径
     */
    @Value("${nvr.sdk.path}")
    private String sdkPath;

    /**
     * sdk日志路径
     */
    @Value("${nvr.log.path}")
    private String sdkLogPath;

    /**
     * sdk日志等级
     */
    @Value("${nvr.log.level}")
    private int logLevel;

    /**
     * 算法服务端IP
     */
    @Value("${netty.server.url}")
    private String serverUrl;

    /**
     * WS调用接口地址
     */
    @Value("${system.webSocket.url}")
    private String syncWebsocketUrl;

    @Value("${spring.interface.api}")
    private String interfaceApi;

    public static void main(String[] args) {
        SpringApplication.run(VideoSgApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        Constant.WEBSOCKET_URL = syncWebsocketUrl;
        Constant.redisTemplate = redisTemplate;
        Constant.apiPermissions = Boolean.valueOf(interfaceApi);
        log.info("videoAccess is running...");
    }

}
