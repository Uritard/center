package com.yjh.accessvideo.common.mqtt;

import com.yjh.accessvideo.common.logs.SpringBeanUtils;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.AlarmMqttMsg;
import com.yjh.accessvideo.common.mqtt.testjsonmsg.Jsonmsg;
import com.yjh.accessvideo.common.mqtt.testjsonmsg.PushMsgbody;
import com.yjh.accessvideo.common.mqtt.testjsonmsg.diffent;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class pushcontroller {

    @Autowired
    MqttUtilsServer mqttUtilsServer;

    @Autowired
    testdetedealthreadserverice tt;

    @PostMapping("/postMsg")
    public Boolean postjosnMsg2(@RequestBody AlarmMqttMsg msg) {
        Boolean pushflag = mqttUtilsServer.pushMsg("test", msg,1);
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

    @PostMapping("/posttt")
    public void postjosnMsg3(@RequestBody Jsonmsg msg) {
        tt.senddatadeal(msg);
    }

    @PostMapping("/postdefi")
    public void postjosnMsg3(@RequestBody diffent msg) {
        tt.senddatadeal2(msg);
    }

   @PostMapping("/postplatfrom")
    public Result postplatfrom(@RequestBody diffent msg){

        final String ROBOT_TASK_URL = "http://iot-center-platform/mqtt/postMqttMsg";
           ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
           if (null != serviceRestTemplate) {
               PushMsgbody pushMsgbody= new PushMsgbody();
               String name="yoja;ldjfl;ajd";
               pushMsgbody.setTopic("youbc");
               pushMsgbody.setObject(name);
             return serviceRestTemplate.postForObject(ROBOT_TASK_URL, pushMsgbody, Result.class);


           }
       return null;
   }

}
