package com.yjh.accessrobot.common.mqtt;

import com.yjh.accessrobot.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.accessrobot.common.mqtt.alarmMsgBody.AlarmMqttMsg;
import com.yjh.accessrobot.module.device.service.AlarmService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class pushcontroller {

    @Autowired
    AlarmService alarmService;


    @PostMapping("/pushrobotMsg")
    public Boolean postjosnMsg2(@RequestBody AlarmMqttMsg msg) {
         Alarm alarm=new Alarm();
        alarmService.PushMsg(alarm);
        return true;
    }



}
