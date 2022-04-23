package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.mqtt.msg.HeartMessageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class HeartBeatJob {

    public static final String HEART = "heart";
    @Autowired
    private MqttUtilsServer mqttUtilsServer;

    private HeartMessageInfo heartMsg;

    @Value("${mqtt.heart.topic}")
    private String heartTopic;

    @Value("${mqtt.province_name}")
    private String provinceName;

    @Value("${mqtt.city_name}")
    private String cityName;

    @Value("${mqtt.station_name}")
    private String stationName;

    @Value("${mqtt.section_ip}")
    private String sectionIP;

    @Value("${mqtt.node_id}")
    private String nodeId;

    @Value("${mqtt.volt_level}")
    private String voltLevel;

    private DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = " */30 * * * * ?")
    public void heartTest() {
        initMssageInfo();
        mqttUtilsServer.pushMsg(heartTopic, heartMsg,1);
    }

    private void initMssageInfo() {
        heartMsg = new HeartMessageInfo();
        heartMsg.setMsgType(HEART);
        heartMsg.setProvinceName(provinceName);
        heartMsg.setCityName(cityName);
        heartMsg.setStationName(stationName);
        heartMsg.setTime(LocalDateTime.now().format(pattern));
        heartMsg.setSectionIp(sectionIP);
        heartMsg.setNodeId(nodeId);
        heartMsg.setVoltLevel(voltLevel);
    }
}
