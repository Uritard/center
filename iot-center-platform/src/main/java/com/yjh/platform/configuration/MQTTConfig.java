package com.yjh.platform.configuration;

import com.yjh.platform.common.mqtt.MqttUtilsServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <功能描述>
 * mqtt配置
 *
 * @author xmchen
 * @date 2022/4/18
 * @since [产品/模块版本] （可选）
 */
@Configuration
public class MQTTConfig {

    @Value("${mqtt.host}")
    private String host;
    @Value("${spring.application.name}")
    private String appName;
    @Value("${server.port}")
    private String appPort;
    @Value("${mqtt.user}")
    private String user;
    @Value("${mqtt.pwd}")
    private String pwd;

    /**
     * mqtt连接保证clientid唯一
     * @return
     */
    @Bean
    public MqttUtilsServer getMQTTServer() {
        return new MqttUtilsServer(host, getIp() + "#" + appName + "#" + appPort, user, pwd);
    }

    /**
     * 获取ip地址
     * @return
     */
    private String getIp() {
        String ip = "0.0.0.0";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
