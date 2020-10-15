package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCameraAlarm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-10-15
 */
@Repository
public interface TCameraAlarmDao {

    int insert(TCameraAlarm tCameraAlarm);
    int deleteByPrimaryId(@Param(value = "cameraAlarmId") Long cameraAlarmId);
    int update(TCameraAlarm tCameraAlarm);
    TCameraAlarm selectByPrimaryId(@Param(value = "cameraAlarmId") Long cameraAlarmId);
    List<TCameraAlarm> select(@Param(value = "cameraAlarmId") Long cameraAlarmId,
                                                                @Param(value = "alarmName") String alarmName,
                                                                @Param(value = "cameraId") Long cameraId,
                                                                @Param(value = "stationId") String stationId,
                                                                @Param(value = "alarmType") Integer alarmType,
                                                                @Param(value = "alarmLevel") Integer alarmLevel,
                                                                @Param(value = "alarmInfo") String alarmInfo,
                                                                @Param(value = "alarmTime") Date alarmTime,
                                                                @Param(value = "isAlarm") Integer isAlarm,
                                                                @Param(value = "dealType") Integer dealType,
                                                                @Param(value = "dealInfo") String dealInfo,
                                                                @Param(value = "dealPersonId") String dealPersonId,
                                                                @Param(value = "dealTime") Date dealTime,
                                                                @Param(value = "alarmState") Integer alarmState,
                                                                @Param(value = "createTime") Date createTime,
                                                                @Param(value = "endTime") Date endTime);
    List<TCameraAlarm> selectByPage(TCameraAlarm tCameraAlarm);

    int batchInsert(List<TCameraAlarm> list);
}
