package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCfgAlarmCurrent;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-24
 */
@Repository
public interface TCfgAlarmCurrentDao {

    int insert(TCfgAlarmCurrent tCfgAlarmCurrent);
    int deleteByPrimaryId(@Param(value = "alarmNo") Long alarmNo);
    int update(TCfgAlarmCurrent tCfgAlarmCurrent);
    TCfgAlarmCurrent selectByPrimaryId(@Param(value = "alarmNo") Long alarmNo);
    List<TCfgAlarmCurrent> select(@Param(value = "alarmNo") Long alarmNo,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "cunstomId") String cunstomId,
                                @Param(value = "meteId") Long meteId,
                                @Param(value = "alarmTime") Date alarmTime,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "alarmValue") String alarmValue,
                                @Param(value = "alarmDesc") String alarmDesc,
                                @Param(value = "confirmState") Integer confirmState,
                                @Param(value = "confirmPeople") String confirmPeople,
                                @Param(value = "confirmTime") Date confirmTime,
                                @Param(value = "confirmRemark") String confirmRemark,
                                @Param(value = "defect") Integer defect,
                                @Param(value = "defectLevel") Integer defectLevel,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "isClear") String isClear,
                                @Param(value = "showType") String showType,
                                @Param(value = "updateTime") Date updateTime);
    List<TCfgAlarmCurrent> selectByPage(TCfgAlarmCurrent tCfgAlarmCurrent);

    int batchInsert(List<TCfgAlarmCurrent> list);
}
