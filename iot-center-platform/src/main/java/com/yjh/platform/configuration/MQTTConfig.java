package com.yjh.platform.configuration;

import com.yjh.platform.common.mqtt.MqttUtilsServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <功能描述>
 *  mqtt配置
 * @author xmchen
 * @date 2022/4/18
 * @since [产品/模块版本] （可选）
 */
@Configuration
public class MQTTConfig {

    @Value("${mqtt.host}")
    private String host;
    @Value("${mqtt.serverClientId}")
    private String serverClientId;
    @Value("${mqtt.user}")
    private String user;
    @Value("${mqtt.pwd}")
    private String pwd;

    @Bean
    public MqttUtilsServer getMQTTServer() {
        return new MqttUtilsServer(host, serverClientId, user, pwd);
    }
}
