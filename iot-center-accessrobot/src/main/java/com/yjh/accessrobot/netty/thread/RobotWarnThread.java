package com.yjh.accessrobot.netty.thread;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.TRobotAlarm;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author YC
 * @date 2020/12/12 15:08
 * 机器人告警处理线程
 */
@lombok.extern.slf4j.Slf4j
public class RobotWarnThread implements Runnable{

    private Map<String,String> robotAlarmMap;
    private RobotService robotService;

    public RobotWarnThread(Map<String, String> robotAlarmMap, RobotService robotService) {
        this.robotAlarmMap = robotAlarmMap;
        this.robotService = robotService;
    }

    @Override
    public void run() {
        try {
            log.info("开始处理告警数据 >>>>>>> robotAlarmMap==={}", robotAlarmMap);

            String robotCode = robotAlarmMap.getOrDefault("robotCode", "");
            if (StringUtils.isEmpty(robotCode)) {
                log.error("机器人编码为空");
                throw new RuntimeException("机器人编码为空");
            }

            Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(robotCode);
            String robotName = StaticContextAccessor.getBean(RobotService.class).selectRobotNameByCode(robotCode);

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
                int res = robotService.insertRobotAlarm(tRobotAlarm);
                log.info("插告警表的结果="+res);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
