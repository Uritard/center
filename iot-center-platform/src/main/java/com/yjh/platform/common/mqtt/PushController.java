package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.mqtt.msg.HeartMessageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class PushController {

    @Autowired
    MqttUtilsServer mqttUtilsServer;

    @Value("${mqtt.heart.topic}")
    private String topic;

    @PostMapping("/postMsg")
    public Boolean postjosnMsg2(@RequestBody HeartMessageInfo msg) {
        Boolean pushflag = mqttUtilsServer.pushMsg(topic, msg,1);
        return pushflag;
    }

    /**
     * 订阅主题
     */
    @GetMapping("/subscribe")
    public void subscribe2(@RequestParam(value = "theme", required = false) String theme) {
        mqttUtilsServer.subscribe(theme);
    }

    @GetMapping("/shutdown")
    public void shutdown() {
        mqttUtilsServer.shutdown();
    }

    @GetMapping("/reconnect")
    public void reconnect() {
        mqttUtilsServer.reconnection();
    }
}
