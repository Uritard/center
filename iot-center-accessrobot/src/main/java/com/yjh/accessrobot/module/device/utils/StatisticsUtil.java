package com.yjh.accessrobot.module.device.utils;

import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.TRobotAlarm;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * 可靠性统计工具类
 *
 * @author zhoupengcheng
 */
@Component
@Slf4j
public class StatisticsUtil {

  private static final String ON_LINE = "在线";

  private static TRobotInfoDao staticDao;

  @Autowired private TRobotInfoDao tRobotInfoDao;


  /**
   * 定时任务检查机器人状态：4小时一次<br>
   * 如不在线，则生成一条离线告警，统计在线时长根据告警进行过滤
   */
  @Scheduled(cron = "0 0 */4 * * ?")
  public void checkRobotStatus() {
    long currTime = System.currentTimeMillis();
    Date now = new Date(currTime);
    try {
      List<TRobotInfo> robotInfoList = tRobotInfoDao.selectByCommission();

      // 对投运时间后+最后登录时间不为空+间隔超过4小时的
      for (TRobotInfo robotInfo : robotInfoList) {
        if (ON_LINE.equals(robotInfo.getRobotStatus())
            || robotInfo.getLastOnlineTime() == null
            || robotInfo.getCommissionDate().after(now)) {
          continue;
        }
        boolean greaterFour = currTime - robotInfo.getLastOnlineTime() > 4 * 60 * 60 * 1000;
        if (greaterFour) {
          TRobotAlarm tRobotAlarm =
              new TRobotAlarm()
                  .setRobotId(robotInfo.getRobotId())
                  .setRobotName(robotInfo.getRobotName())
                  .setAlarmName("离线告警")
                  .setAlarmLevel(132)
                  .setAlarmInfo("已离线超过4小时")
                  .setAlarmTime(now)
                  .setAlarmState(0);
          log.info("tRobotAlarm的内容==={}", tRobotAlarm);
          int res = tRobotInfoDao.insertRobotAlarm(tRobotAlarm);
          log.info("插告警表的结果=" + res);
        }
      }
    } catch (Exception e) {
      log.error("定时查询在线状态定时任务发生异常{}", e.getMessage());
    }
  }

  @PostConstruct
  public void init() {
    staticDao = tRobotInfoDao;
  }
}
