package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.yjh.platform.module.task.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface TCruiseResultDao {

    int insert(TCruiseResult tCruiseResult);
    int deleteByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    int update(TCruiseResult tCruiseResult);
    TCruiseResult selectByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    List<TCruiseResult> select(@Param(value = "taskResultId") String taskResultId,
                               @Param(value = "taskId") String taskId,
                               @Param(value = "areaId") String areaId,
                               @Param(value = "cType") Integer cType,
                               @Param(value = "cState") Integer cState,
                               @Param(value = "modifyState") Integer modifyState,
                               @Param(value = "taskCount") Integer taskCount,
                               @Param(value = "taskWait") Integer taskWait,
                               @Param(value = "checkUser") String checkUser,
                               @Param(value = "checkDate") Date checkDate,
                               @Param(value = "weather") String weather,
                               @Param(value = "createTime") Date createTime,
                               @Param(value = "executeTime") Date executeTime,
                               @Param(value = "taskCode") String taskCode,
                               @Param(value = "remark") String remark);
    List<TCruiseResultExpand> selectTaskByPage(@Param(value = "taskName") String taskName,
                                               @Param(value = "cType") Integer cType,
                                               @Param(value = "cState") Integer cState);
    List<CruiseResultDetail> selectCruiseByPage(@Param(value = "taskId") String taskId,
                                                @Param(value = "cruiseType") Integer cruiseType,
                                                @Param(value = "state") Integer state,
                                                @Param(value = "executeTime") String executeTime,
                                                @Param(value = "deviceName") String deviceName);

    int manualReview(CruiseManualReview cruiseManualReview);
    int batchInsert(List<TCruiseResult> list);
    List<StatisticalTools> taskStatistical(@Param(value = "colName1")String colName1,
                                           @Param(value = "Start")String Start,
                                           @Param(value = "End")String End);
    List<StatisticalTools> cruiseStatistical(@Param(value = "colName1")String colName1);
    List<TaskSimpleInfo> selectTaskIsRunning();

    String selectByIdentifyResult(@Param(value = "identifyResult")Integer identifyResult);
    String selectByIdentifyState(@Param(value = "identifyState")Integer identifyState);

    String selectUserName(@Param(value = "userID")Integer  userID);

    List<CruiseManualReview> selectManualDetail(@Param(value = "taskId")String taskId,
                                                @Param(value = "executeTime")String executeTime);
}
