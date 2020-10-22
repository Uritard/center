package com.yjh.platform.module.task.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCfgAlarmHistory;
import com.yjh.platform.module.task.entity.TCfgAlarmHistoryResultInfo;
import com.yjh.platform.module.task.entity.TUnionTaskAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-24
 */
@Repository
public interface TCfgAlarmHistoryDao {

    int insert(TCfgAlarmHistory tCfgAlarmHistory);
    int deleteByPrimaryId(@Param(value = "alarmNo") Long alarmNo);
    int update(TCfgAlarmHistory tCfgAlarmHistory);
    TCfgAlarmHistory selectByPrimaryId(@Param(value = "alarmNo") Long alarmNo);
    List<TCfgAlarmHistory> select(@Param(value = "alarmNo") Long alarmNo,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "cunstomId") String cunstomId,
                                @Param(value = "meteId") Long meteId,
                                @Param(value = "alarmTime") Date alarmTime,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "alarmValue") String alarmValue,
                                @Param(value = "alarmDesc") String alarmDesc,
                                @Param(value = "clearTime") Date clearTime,
                                @Param(value = "clearValue") BigDecimal clearValue,
                                @Param(value = "confirmState") Integer confirmState,
                                @Param(value = "confirmPeople") String confirmPeople,
                                @Param(value = "confirmTime") Date confirmTime,
                                @Param(value = "confirmRemark") String confirmRemark,
                                @Param(value = "defect") Integer defect,
                                @Param(value = "defectLevel") Integer defectLevel,
                                @Param(value = "forceClearReason") String forceClearReason,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "isClear") String isClear,
                                @Param(value = "showType") String showType,
                                @Param(value = "updateTime") Date updateTime);
    List<TCfgAlarmHistory> selectByPage(@Param(value = "alarmNo") Long alarmNo,
                                        @Param(value = "deviceId") Long deviceId,
                                        @Param(value = "cunstomId") String cunstomId,
                                        @Param(value = "meteId") Long meteId,
                                        @Param(value = "alarmTime") Date alarmTime,
                                        @Param(value = "alarmLevel") Integer alarmLevel,
                                        @Param(value = "alarmValue") String alarmValue,
                                        @Param(value = "alarmDesc") String alarmDesc,
                                        @Param(value = "clearTime") Date clearTime,
                                        @Param(value = "clearValue") BigDecimal clearValue,
                                        @Param(value = "confirmState") Integer confirmState,
                                        @Param(value = "confirmPeople") String confirmPeople,
                                        @Param(value = "confirmTime") Date confirmTime,
                                        @Param(value = "confirmRemark") String confirmRemark,
                                        @Param(value = "defect") Integer defect,
                                        @Param(value = "defectLevel") Integer defectLevel,
                                        @Param(value = "forceClearReason") String forceClearReason,
                                        @Param(value = "meteCode") String meteCode,
                                        @Param(value = "isClear") String isClear,
                                        @Param(value = "showType") String showType,
                                        @Param(value = "updateTime") Date updateTime);

    int batchInsert(List<TCfgAlarmHistory> list);

    List<TCfgAlarmHistoryResultInfo> selectUnionCruiseResult(@Param(value = "taskId") String taskId);
}
