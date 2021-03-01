package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.TRobotAlarm;
import com.yjh.accessrobot.module.command.service.RobotService;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;

/**
 * @author YC
 * @date 2020/12/12 15:08
 */
@lombok.extern.slf4j.Slf4j
public class AlarmResultDealThread implements Runnable{

    private static final String DATETIMEFORMATTPL = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEFORMATTPL);

    private Map<String,String> robotAlarmMap;
    private boolean isThreadStart;
    private RobotServerHandler robotServerHandler;


    public AlarmResultDealThread(Map<String,String> robotAlarmMap,boolean isThreadStart,RobotServerHandler robotServerHandler){
        this.robotAlarmMap = robotAlarmMap;
        this.isThreadStart = isThreadStart;
        this.robotServerHandler = robotServerHandler;
    }

    @Override
    public void run() {
        try {

            log.info("处理告警数据的的线程进来了！！！！！！！！！！！！！！");
            log.info("传进来的robotAlarmMap是==="+robotAlarmMap);

            //根据code获取id
            Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(robotAlarmMap.get("robotCode"));
            log.info("该robotCode的robotId是==="+robotId);
            if (Objects.nonNull(robotId)){
                TRobotAlarm tRobotAlarm = new TRobotAlarm()
                        .setRobotId(robotId)
                        .setAlarmName(robotAlarmMap.get("content"))
                        .setAlarmLevel(133)
                        .setAlarmInfo(robotAlarmMap.get("content"))
                        .setAlarmTime(sdf.parse(robotAlarmMap.get("time")))
                        .setAlarmState(276);
                log.info("tRobotAlarm的内容==="+tRobotAlarm);
                //插表入库
                int res = StaticContextAccessor.getBean(RobotService.class).insertRobotAlarm(tRobotAlarm);
                log.info("插告警表的结果="+res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
