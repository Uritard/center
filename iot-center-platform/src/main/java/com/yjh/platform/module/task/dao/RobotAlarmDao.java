package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.RobotAlarm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface RobotAlarmDao {

    int insert(RobotAlarm robotAlarm);
    int deleteByPrimaryId(@Param(value = "robotAlarmId") Long robotAlarmId);
    int update(RobotAlarm robotAlarm);
    RobotAlarm selectByPrimaryId(@Param(value = "robotAlarmId") Long robotAlarmId);
    List<RobotAlarm> select(@Param(value = "robotAlarmId") Long robotAlarmId,
                                @Param(value = "stationId") String stationId,
                                @Param(value = "alarmType") Integer alarmType,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "alarmInfo") String alarmInfo,
                                @Param(value = "alarmTime") Date alarmTime,
                                @Param(value = "dealType") Integer dealType,
                                @Param(value = "dealInfo") String dealInfo,
                                @Param(value = "dealPersonId") String dealPersonId,
                                @Param(value = "dealPersonName") String dealPersonName,
                                @Param(value = "positionStationNum") String positionStationNum,
                                @Param(value = "positionOffset") String positionOffset,
                                @Param(value = "alarmState") Integer alarmState,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "endTime") Date endTime);
    List<RobotAlarm> selectByPage(RobotAlarm robotAlarm);

    int batchInsert(List<RobotAlarm> list);
}
