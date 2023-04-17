package com.yjh.platform.configuration;

import com.yjh.platform.common.mqtt.MqttUtilsServer;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ApplicationProperties applicationProperties;
    @Value("${spring.application.name}")
    private String appName;
    @Value("${server.port}")
    private String appPort;

    @Bean("algorithmMqtt")
    public MqttUtilsServer getAlgorithmMQTTServer() {
        return new MqttUtilsServer(applicationProperties.getManagerMqttConfig().getMqttHost(), getIp() + "#" + appName + "#" + appPort, applicationProperties.getManagerMqttConfig().getMqttUser(), applicationProperties.getManagerMqttConfig().getMqttPwd());
    }

    @Bean("voiceMqtt")
    public MqttUtilsServer getVoiceMQTTServer() {
        return new MqttUtilsServer(applicationProperties.getAudioConfig().getAudioMqttHost(), getIp() + "#" + appName + "#" + appPort, applicationProperties.getAudioConfig().getAudioMqttUser(), applicationProperties.getAudioConfig().getAudioMqttPwd());
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
