package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author tt
 * @since 2020-09-04
 */
@Repository
public interface TUnionTaskDao {

    int insert(TUnionTask tUnionTask);
    int deleteByPrimaryId(@Param(value = "unionId") String unionId);
    int update(TUnionTask tUnionTask);
    TUnionTask selectByPrimaryId(@Param(value = "unionId") String unionId);
    List<TUnionTask> select(@Param(value = "unionId") String unionId,
                            @Param(value = "ruleId") Long ruleId,
                            @Param(value = "unionName") String unionName,
                            @Param(value = "ruleDelay") Integer ruleDelay,
                            @Param(value = "isFinish") Integer isFinish,
                            @Param(value = "robotId") Long robotId,
                            @Param(value = "remark1") Integer remark1,
                            @Param(value = "remark2") Integer remark2,
                            @Param(value = "remark3") String remark3,
                            @Param(value = "paramValues") String paramValues,
                            @Param(value = "startTime") Date startTime,
                            @Param(value = "createTime") Date createTime);
    List<TUnionTask> selectByPage(TUnionTask tUnionTask);
    int batchInsert(List<TUnionTask> list);

    List<TUnionTaskExpand> selectHistory(@Param(value = "ruleName")String  ruleName,
                                         @Param(value = "endDateTemp")Date endDateTemp,
                                         @Param(value = "startDateTemp")Date startDateTemp);
    //    int insertRecord(TUnionTask tUnionTask);
    List<TUnionTaskDetail> selectUnionDetail(@Param(value = "ruleId")Long  ruleId);
    int insertRecordDetail(List<TUnionTaskAttr> tUnionTaskAttrList);

    List<WarnStatistical> getHistoryByWeek();
    List<WarnStatistical> getHistoryByYear();
    List<WarnStatistical> getHistoryByMonth();
}
