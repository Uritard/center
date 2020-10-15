package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.task.entity.TRobotAlarm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-10-15
 */
@Repository
public interface TRobotAlarmDao {

    int insert(TRobotAlarm tRobotAlarm);
    int deleteByPrimaryId(@Param(value = "robotAlarmId") Long robotAlarmId);
    int update(TRobotAlarm tRobotAlarm);
    TRobotAlarm selectByPrimaryId(@Param(value = "robotAlarmId") Long robotAlarmId);
    List<TRobotAlarm> select(@Param(value = "robotAlarmId") Long robotAlarmId,
                                @Param(value = "alarmName") String alarmName,
                                @Param(value = "robotId") Long robotId,
                                @Param(value = "stationId") String stationId,
                                @Param(value = "alarmType") Integer alarmType,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "alarmInfo") String alarmInfo,
                                @Param(value = "alarmTime") Date alarmTime,
                                @Param(value = "dealType") Integer dealType,
                                @Param(value = "dealInfo") String dealInfo,
                                @Param(value = "dealPersonId") String dealPersonId,
                                @Param(value = "dealTime") Date dealTime,
                                @Param(value = "positionStationNum") String positionStationNum,
                                @Param(value = "positionOffset") String positionOffset,
                                @Param(value = "alarmState") Integer alarmState,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "endTime") Date endTime);
    List<TRobotAlarm> selectByPage(TRobotAlarm tRobotAlarm);

    int batchInsert(List<TRobotAlarm> list);
}
