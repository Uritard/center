package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.mqtt.msg.HeartMessageInfo;
import com.yjh.platform.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class HeartBeatJob {

    public static final String HEART = "heart";
    @Qualifier("algorithmMqtt")
    @Autowired
    private MqttUtilsServer mqttUtilsServer;

    private HeartMessageInfo heartMsg;
    @Autowired
    private ApplicationProperties applicationProperties;

    private DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = " */30 * * * * ?")
    public void heartTest() {
        initMssageInfo();
        mqttUtilsServer.pushMsg(applicationProperties.getManagerMqttConfig().getMqttHeartTopic(), heartMsg,1);
    }

    private void initMssageInfo() {
        heartMsg = new HeartMessageInfo();
        heartMsg.setMsgType(HEART);
        heartMsg.setProvinceName(encode(applicationProperties.getManagerMqttConfig().getMqttProvinceName()));
        heartMsg.setCityName(encode(applicationProperties.getManagerMqttConfig().getMqttCityName()));
        heartMsg.setStationName(encode(applicationProperties.getManagerMqttConfig().getMqttStationName()));
        heartMsg.setSectionName(applicationProperties.getManagerMqttConfig().getMqttSectionName());
        heartMsg.setTime(LocalDateTime.now().format(pattern));
        heartMsg.setSectionIp(applicationProperties.getManagerMqttConfig().getMqttSectionIp());
        heartMsg.setNodeId(applicationProperties.getManagerMqttConfig().getMqttNodeId());
        heartMsg.setVoltLevel(applicationProperties.getManagerMqttConfig().getVoltLevel());
    }

    private String encode(String str){
        // return new String(str.getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        return str;
    }
}
