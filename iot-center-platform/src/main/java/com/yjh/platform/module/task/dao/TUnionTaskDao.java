package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.yjh.platform.module.task.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

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

    Map<String,Integer> getHistoryByWeek(@Param(value = "firstTime1")String firstTime1,
                                         @Param(value = "firstTime2")String firstTime2,
                                         @Param(value = "firstTime3")String firstTime3,
                                         @Param(value = "firstTime4")String firstTime4,
                                         @Param(value = "firstTime5")String firstTime5,
                                         @Param(value = "firstTime6")String firstTime6,
                                         @Param(value = "firstTime7")String firstTime7,
                                         @Param(value = "firstTime8")String firstTime8);
    Map<String,Integer> getHistoryByYear(@Param(value = "firstTime1")String firstTime1,
                                         @Param(value = "firstTime2")String firstTime2,
                                         @Param(value = "firstTime3")String firstTime3,
                                         @Param(value = "firstTime4")String firstTime4,
                                         @Param(value = "firstTime5")String firstTime5,
                                         @Param(value = "firstTime6")String firstTime6,
                                         @Param(value = "firstTime7")String firstTime7,
                                         @Param(value = "firstTime8")String firstTime8,
                                         @Param(value = "firstTime9")String firstTime9,
                                         @Param(value = "firstTime10")String firstTime10,
                                         @Param(value = "firstTime11")String firstTime11,
                                         @Param(value = "firstTime12")String firstTime12,
                                         @Param(value = "firstTime13")String firstTime13);
    List<WarnStatistical> getHistoryByMonth(@Param(value = "startTime")String startTime,
                                            @Param(value = "endTime")String endTime);
}
