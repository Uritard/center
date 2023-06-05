package com.yjh.accessrobot.module.device.utils;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.AlarmShield;
import com.yjh.accessrobot.module.command.entity.TRobotAlarm;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import com.yjh.accessrobot.module.command.service.RobotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 可靠性统计工具类
 *
 * @author zhoupengcheng
 */
@Component
@Slf4j
public class StatisticsUtil {

  private static final String ON_LINE = "在线";
  private static final String OFF_LINE = "离线";
  private static TRobotInfoDao staticDao;
  private static final Map<Long, Long> LAST_ONLINE_TIME_MAP = new ConcurrentHashMap<>();

  @Autowired private TRobotInfoDao tRobotInfoDao;

  @Resource
  private RedisTemplate redisTemplate;

  @Resource
  private ServiceRestTemplate serviceRestTemplate;

  @Resource
  private RobotService robotService;

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
    Date commissionDate = null;
    if (tRobotInfoList.size() > 0) {
      robotStatus = tRobotInfoList.get(0).getRobotStatus();
      lastOnlineTime = tRobotInfoList.get(0).getLastOnlineTime();
      duration = tRobotInfoList.get(0).getDuration();
      offLineCount = tRobotInfoList.get(0).getOffLineCount();
      commissionDate = tRobotInfoList.get(0).getCommissionDate();
    } else {
      log.error("查询不到robotId={}的机器人信息", robotInfo.getRobotId());
    }
    // 投运日期未到，不做处理
    if (commissionDate != null && commissionDate.after(new Date())) {
      return;
    }
    if (duration == null) {
      duration = 0L;
    }
    if (offLineCount == null) {
      offLineCount = 0L;
    }
    Long currTime = System.currentTimeMillis();
    /*
     在线->离线 只记录状态 <br/>
     离线->离线 4 小时未收到机器人监控系统心跳报文，则记录离线次数+1 <br>
     在线 距离上次时间小于4小时，则记录周期，同时刷新本次心跳时间 <br>
    */
    switch (robotInfo.getRobotStatus()) {
      case ON_LINE:
        robotInfo.setRobotStatus(ON_LINE);
        if (lastOnlineTime != null) {
          boolean lessFour = currTime - lastOnlineTime <= 4 * 60 * 60 * 1000;
          if (lessFour) {
            robotInfo.setDuration(duration + (currTime - lastOnlineTime));
          }
        }
        robotInfo.setLastOnlineTime(currTime);
        break;
      case OFF_LINE:
        if (ON_LINE.equals(robotStatus)) {
          robotInfo.setRobotStatus(OFF_LINE);
        } else {
          // 没有lastOnlineTime说明没有成功登录过，不统计
          if (lastOnlineTime != null) {
            boolean greaterFour = currTime - lastOnlineTime > 4 * 60 * 60 * 1000;
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
          //告警屏蔽处理
          AtomicReference<Boolean> isWarn = new AtomicReference<>(true);
          List<AlarmShield> alarmShieldList = robotService.selectAlarmShield(tRobotAlarm.getRobotId(), tRobotAlarm.getAlarmInfo());
          if (alarmShieldList.size() > 0){
            alarmShieldList.forEach(alarmShield -> {
              Date endTime = alarmShield.getEndTime();
              if (now.before(endTime)){
                log.info("告警屏蔽：{}",alarmShield);
                isWarn.set(false);
              }
            });
          }
          if (isWarn.get()){
            int res = tRobotInfoDao.insertRobotAlarm(tRobotAlarm);
            log.info("插告警表的结果=" + res);

            sendWebsocket(tRobotAlarm);
          } else {
            log.info("此告警被屏蔽了，不入库，不弹窗");
          }

        }
      }
    } catch (Exception e) {
      log.error("定时查询在线状态定时任务发生异常", e);
    }
  }

  private void sendWebsocket(TRobotAlarm tRobotAlarm) {
    Map<String, Object> jasonMaps = new HashMap<>(16);
    jasonMaps.put("type", "alarmPopUp");
    jasonMaps.put("warnType", "2");
    jasonMaps.put("warnLevel", tRobotAlarm.getAlarmLevel());
    jasonMaps.put("warnId", tRobotAlarm.getRobotAlarmId());
    String json = JSON.toJSONString(jasonMaps);
    log.info("发送给前端的消息：{}", json);
    try {
      String webSocketUrl = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:webSocketUrl","content"));
      String result = serviceRestTemplate.postForObject(webSocketUrl, json, String.class);
      log.info("param:{} result:{}", json, result);
    } catch (Exception e) {
      log.error("发送前端失败", e);
    }
  }

  /**
   * 启动后延迟30秒，然后每隔10分支统计一次
   * 如果最后上线时间没有变化则不增加离线次数
   * 有一个小问题，如果服务器重启，离线时间在 4~4.5 小时的机器人离线次数会再次加一
   */
  @Scheduled(initialDelay = 30000, fixedDelay=600000)
  public void offlineDuration() {
    long currTime = System.currentTimeMillis();
    Date now = new Date(currTime);
    try {
      List<TRobotInfo> robotInfoList = tRobotInfoDao.selectByCommission();
      log.info("离线次数统计定时器计数成功, size: {}", robotInfoList.size());

      // 对投运时间后+最后登录时间不为空+间隔超过4小时的
      for (TRobotInfo robotInfo : robotInfoList) {
        if (ON_LINE.equals(robotInfo.getRobotStatus())
                || robotInfo.getLastOnlineTime() == null
                || robotInfo.getCommissionDate().after(now)) {
          /*log.info("离线次数统计不符合离线条件，robotId: {}, status: {}, lastOnline: {}, commissionDate: {}",
                  robotInfo.getRobotId(), robotInfo.getRobotStatus(), robotInfo.getLastOnlineTime(), DateTimeUtil.format(robotInfo.getCommissionDate()));*/
          continue;
        }
        long offlineTime = currTime - robotInfo.getLastOnlineTime();
        boolean greaterFour = offlineTime > 4 * 60 * 60 * 1000L && offlineTime < 45 * 6 * 60 * 1000L
            && robotInfo.getLastOnlineTime() > LAST_ONLINE_TIME_MAP.getOrDefault(robotInfo.getRobotId(), 0L);
        log.info("离线次数统计，greater: {}, robotId: {}, lastOnline: {}, offlineTime: {}, preLastOnline: {}",
                greaterFour, robotInfo.getRobotId(), robotInfo.getLastOnlineTime(), offlineTime, LAST_ONLINE_TIME_MAP.getOrDefault(robotInfo.getRobotId(), 0L));
        if (greaterFour) {
          TRobotInfo tRobotInfo = new TRobotInfo()
                  .setRobotId(robotInfo.getRobotId());
          long offLine = robotInfo.getOffLineCount() == null ? 0 : robotInfo.getOffLineCount();
          tRobotInfo.setOffLineCount(offLine + 1);
          LAST_ONLINE_TIME_MAP.put(robotInfo.getRobotId(), robotInfo.getLastOnlineTime());
          tRobotInfoDao.update(tRobotInfo);
        }
      }
    } catch (Exception e) {
      log.error("定时查询在线状态定时任务发生异常", e);
    }
  }

  @PostConstruct
  public void init() {
    staticDao = tRobotInfoDao;
  }
}
