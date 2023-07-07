package com.yjh.accessrobot.netty.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.AlarmShield;
import com.yjh.accessrobot.module.command.entity.TRobotAlarm;
import com.yjh.accessrobot.module.command.service.RobotService;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author YC
 * @date 2020/12/12 15:08
 * 机器人告警处理线程
 */
@lombok.extern.slf4j.Slf4j
public class RobotWarnThread implements Runnable{

    private Map<String,String> robotAlarmMap;
    private RobotService robotService;
    private String syncWebsocketUrl;

    public RobotWarnThread(Map<String, String> robotAlarmMap, RobotService robotService,String syncWebsocketUrl) {
        this.robotAlarmMap = robotAlarmMap;
        this.robotService = robotService;
        this.syncWebsocketUrl = syncWebsocketUrl;
    }

    @Override
    public void run() {
        try {
            log.info("开始处理告警数据 >>>>>>> robotAlarmMap==={}", robotAlarmMap);

            String robotCode = robotAlarmMap.get("robotCode");
            Long robotId = robotService.selectRobotIdByCode(robotCode);
            String robotName = robotService.selectRobotNameByCode(robotCode);

            log.info("该robotCode的robotId是==={}", robotId);
            if (Objects.nonNull(robotId)){
                TRobotAlarm tRobotAlarm = new TRobotAlarm()
                        .setRobotId(robotId)
                        .setRobotName(robotName)
                        .setAlarmName(robotAlarmMap.get("content"))
                        .setAlarmLevel(133)
                        .setAlarmInfo(robotAlarmMap.get("content"))
                        .setAlarmTime(DateTimeUtil.parse(robotAlarmMap.get("time")))
                        .setAlarmState(276);
                log.info("tRobotAlarm的内容==={}", tRobotAlarm);
                //告警屏蔽处理
                AtomicReference<Boolean> isWarn = new AtomicReference<>(true);
                List<AlarmShield> alarmShieldList = robotService.selectAlarmShield(tRobotAlarm.getAlarmInfo());
                if (alarmShieldList.size() > 0){
                    alarmShieldList.forEach(alarmShield -> {
                        Date now  = new Date();
                        Date endTime = alarmShield.getEndTime();
                        if (now.before(endTime)){
                            log.info("告警屏蔽：{}",alarmShield);
                            isWarn.set(false);
                        }
                    });
                }
                if (isWarn.get()){
                    int res = robotService.insertRobotAlarm(tRobotAlarm);
                    log.info("插告警表的结果="+res);

                    Map<String, Object> jasonMaps = new HashMap<>(16);
                    jasonMaps.put("type", "alarmPopUp");
                    jasonMaps.put("warnId", tRobotAlarm.getRobotAlarmId());
                    jasonMaps.put("warnType", "2");
                    jasonMaps.put("warnLevel", tRobotAlarm.getAlarmLevel());
                    String json = JSON.toJSONString(jasonMaps);
                    log.info("发送给前端的消息：{}", json);
                    Constant.postUrl(syncWebsocketUrl, json);
                } else {
                    log.info("此告警被屏蔽了，不入库，不弹窗");
                }


            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
