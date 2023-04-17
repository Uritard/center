package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.mqtt.msg.HeartMessageInfo;
import com.yjh.platform.common.mqtt.msg.PushMsgbody;
import com.yjh.platform.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class PushController {

    @Qualifier("algorithmMqtt")
    @Autowired
    MqttUtilsServer mqttUtilsServer;

    @Autowired
    private ApplicationProperties applicationProperties;

    @PostMapping("/postMsg")
    public Boolean postjosnMsg2(@RequestBody Object msg) {
        Boolean pushflag = mqttUtilsServer.pushMsg(applicationProperties.getManagerMqttConfig().getMqttTopic(), msg,1);
        return pushflag;
    }

    @PostMapping("/postMqttMsg")
    public Boolean postMqttMsg(@RequestBody PushMsgbody pushMsgbody) {
        Boolean pushflag = mqttUtilsServer.pushMsg(pushMsgbody.getTopic(), pushMsgbody.getObject(),1);
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
