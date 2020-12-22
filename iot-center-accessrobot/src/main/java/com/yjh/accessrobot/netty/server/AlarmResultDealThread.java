package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.TRobotAlarm;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * @author YC
 * @date 2020/12/12 15:08
 */
@lombok.extern.slf4j.Slf4j
public class AlarmResultDealThread implements Runnable{

    private static final String DATETIMEFORMATTPL = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEFORMATTPL);

    private RedisTemplate redisTemplate;
    private Map<String,String> robotAlarmMap;

    public AlarmResultDealThread(Map<String,String> robotAlarmMap,RedisTemplate redisTemplate){
        this.robotAlarmMap = robotAlarmMap;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        try {
            log.info("处理告警数据的的线程进来了！！！！！！！！！！！！！！");
            log.info("传进来的robotAlarmMap是==="+robotAlarmMap);

            //根据code获取id
            String robotCode = robotAlarmMap.get("robotCode");
            Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(robotCode);
            log.info("该robotCode的robotId是==="+robotId);
            TRobotAlarm tRobotAlarm = new TRobotAlarm()
                    .setRobotId(robotId)
                    .setAlarmName(robotAlarmMap.get("content"))
                    .setAlarmLevel(133)
                    .setAlarmInfo(robotAlarmMap.get("content"))
                    .setAlarmTime(sdf.parse(robotAlarmMap.get("time")))
//                            .setPositionOffset()
//                            .setPositionStationNum()
                    .setAlarmState(276);
//                            .setCreateTime()
//                            .setEndTime()
            log.info("tRobotAlarm的内容==="+tRobotAlarm);
            //插表入库
            int res = StaticContextAccessor.getBean(RobotService.class).insertRobotAlarm(tRobotAlarm);
            log.info("插告警表的结果="+res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
