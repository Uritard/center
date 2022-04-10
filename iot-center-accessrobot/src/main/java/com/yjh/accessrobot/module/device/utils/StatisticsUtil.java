package com.yjh.accessrobot.module.device.utils;

import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 统计登录信息到缓存中
 *
 * @author zhoupengcheng
 */
@Component
@Slf4j
public class StatisticsUtil {

  private static final String OFF_LINE = "离线";
  private static final String ON_LINE = "在线";

  private static TRobotInfoDao staticDao;

  @Autowired private TRobotInfoDao tRobotInfoDao;

  /**
   * 刷新机器人/无人机状态的同时，刷新缓存，统计在线时长及离线次数 <br>
   * isOnline - 是否在线 <br>
   * lastOnlineTime - 上次在线时间 <br>
   * duration - 在线时长累积 毫秒 <br>
   * offLineCount - 离线次数统计
   *
   * @param robotInfo 机器人信息
   */
  public static void onlineDuration(TRobotInfo robotInfo) {

    log.info("机器人robotId=[{}]状态更新，在线时长刷新", robotInfo.getRobotId());
    List<TRobotInfo> tRobotInfoList =
        staticDao.selectByPage(new TRobotInfo().setRobotId(robotInfo.getRobotId()));
    String robotStatus = null;
    Long lastOnlineTime = null;
    Long duration = null;
    Long offLineCount = null;
    if (tRobotInfoList.size() > 0) {
      robotStatus = tRobotInfoList.get(0).getRobotStatus();
      lastOnlineTime = tRobotInfoList.get(0).getLastOnlineTime();
      duration = tRobotInfoList.get(0).getDuration();
      offLineCount = tRobotInfoList.get(0).getOffLineCount();
    } else {
      log.error("查询不到robotId={}的机器人信息", robotInfo.getRobotId());
    }
    if (duration == null) {
      duration = 0L;
    }
    if (offLineCount == null) {
      offLineCount = 0L;
    }
    /*
     在线->离线 只记录状态 <br/>
     离线->离线 4 小时未收到机器人监控系统心跳报文，则记录离线次数+1 <br>
     在线 距离上次时间小于4小时，则记录周期，同时刷新本次心跳时间 <br>
    */
    switch (robotInfo.getRobotStatus()) {
      case ON_LINE:
        robotInfo.setRobotStatus(ON_LINE);
        if (lastOnlineTime != null) {
          boolean lessFour = System.currentTimeMillis() - lastOnlineTime <= 4 * 60 * 60 * 1000;
          if (lessFour) {
            robotInfo.setDuration(duration + (System.currentTimeMillis() - lastOnlineTime));
          }
        }
        robotInfo.setLastOnlineTime(System.currentTimeMillis());
        break;
      case OFF_LINE:
        if (ON_LINE.equals(robotStatus)) {
          robotInfo.setRobotStatus(OFF_LINE);
        } else {
          // 没有lastOnlineTime说明没有成功登录过，不统计
          if (lastOnlineTime != null) {
            boolean greaterFour = System.currentTimeMillis() - lastOnlineTime > 4 * 60 * 60 * 1000;
            if (greaterFour) {
              robotInfo.setOffLineCount(offLineCount + 1);
            }
          }
        }
        break;
      default:
        break;
    }
  }

  /** 连续正常运行天数、投运期间累计自检结果正常天数、正常巡检天数 <br> */
  public static void static1() {}

  @PostConstruct
  public void init() {
    staticDao = tRobotInfoDao;
  }
}
