package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.mqtt.msg.postbody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class pushcontroller {

    @Autowired
    MqttUtilsServer mqttUtilsServer;

    @PostMapping("/postMsg")
    public Boolean postjosnMsg2(@RequestBody postbody msg) {
        Boolean pushflag = mqttUtilsServer.pushMsg(msg.getTopic(), msg.getMqttmsg(),1);
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
