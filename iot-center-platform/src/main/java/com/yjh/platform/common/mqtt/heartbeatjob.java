package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.mqtt.msg.mqttmsg;
import com.yjh.platform.common.mqtt.msg.postbody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Component
public class heartbeatjob {

    @Autowired
    private MqttUtilsServer mqttUtilsServer;

    private postbody postbody;
    private mqttmsg mqttmsg;
    private String topic;
    private String province_name;
    private String city_name;
    private String station_name;
    private String section_ip;
    private String node_id;
    private String volt_level;


    heartbeatjob() {
        postbody = new postbody();
        mqttmsg = new mqttmsg();
        Properties properties = new Properties();
        InputStream inputStream = this.getClass().getResourceAsStream("/application-dev.properties");
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            properties.load(bf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        topic = properties.getProperty("mqtt.topic");
        province_name = properties.getProperty("mqtt.province_name");
        city_name = properties.getProperty("mqtt.city_name");
        station_name = properties.getProperty("mqtt.station_name");
        section_ip = properties.getProperty("mqtt.section_ip");
        node_id = properties.getProperty("mqtt.node_id");
        volt_level = properties.getProperty("mqtt.volt_level");
        mqttmsg.setMsg_type("heart");
    }

    @Scheduled(cron = " */30 * * * * ?")
    public void runpushmessag() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        mqttmsg.setProvince_name(province_name);
        mqttmsg.setCity_name(city_name);
        mqttmsg.setStation_name(station_name);
        mqttmsg.setTime(dateFormat.format(date));
        mqttmsg.setSection_ip(section_ip);
        mqttmsg.setNode_id(node_id);
        mqttmsg.setVolt_level(volt_level);

        postbody.setTopic(topic);
        postbody.setMqttmsg(mqttmsg);
        mqttUtilsServer.pushMsg(postbody.getTopic(), postbody.getMqttmsg(),1);
        // mqttUtilsServer.subscribe("jeff2");
    }
}