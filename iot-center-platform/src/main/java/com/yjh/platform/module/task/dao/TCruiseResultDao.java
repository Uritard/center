package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
                               @Param(value = "taskName") String taskName,
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
    List<CruiseResultDetail> selectCruiseByPage(@Param(value = "taskResultId") String taskResultId,
                                                @Param(value = "cruiseType") Integer cruiseType,
                                                @Param(value = "cruiseResult") Integer cruiseResult,
                                                @Param(value = "deviceType") Integer deviceType,
                                                @Param(value = "startTime") String startTime,
                                                @Param(value = "endTime") String endTime,
                                                @Param(value = "list") List<Long> list);

    int manualReview(CruiseManualReview cruiseManualReview);
    Long selectDeviceMeteId(@Param(value = "cruiseDataId")Long cruiseDataId);
    int updateDeviceMeteUpdate(TStdDeviceMeteUpdate tStdDeviceMeteUpdate);
    AfterManualReviewInfo selectJudgeCondition(@Param(value = "cruiseDataId")Long cruiseDataId);
    TStdDevicemete selectDeviceMeteInfo(@Param(value = "instanceId")Long instanceId);

    int updateWarnInfo(@Param(value = "taskId")String taskId,
                       @Param(value = "instanceId")Long instanceId,
                       @Param(value = "warnName")String warnName,
                       @Param(value = "warnLevel")Integer warnLevel,
                       @Param(value = "warnContent")String warnContent,
                       @Param(value = "outRange")String outRange,
                       @Param(value = "dealPersonId")String dealPersonId,
                       @Param(value = "dealTime")Date dealTime);
    int updateWarnInfo2(@Param(value = "taskId")String taskId,
                       @Param(value = "instanceId")Long instanceId,
                        @Param(value = "dealPersonId")String dealPersonId,
                        @Param(value = "dealTime")Date dealTime);
    int updateIsWarn(@Param(value = "cruiseDataId")Long cruiseDataId);
    Long selectWarnId(@Param(value = "taskId")String taskId,
                      @Param(value = "instanceId")Long instanceId);
    int batchInsert(List<TCruiseResult> list);

    String selectOnMonday();
    String selectOnSunday();
    String selectLastMonday();
    String selectLastSunday();
    List<StatisticalTools> taskStatistical(@Param(value = "colName1")String colName1,
                                           @Param(value = "start")String start,
                                           @Param(value = "end")String end);
    List<CruiseStatistical> cruiseStatistical();
    CruiseStatistical cruiseStatistical2();

    List<StatisticalTools> cruiseStatisticalByStatus();
    List<CruiseStatistical> cruiseStatisticalByAbnormal();

    //查询正在执行的任务
    List<TaskSimpleInfo> selectTaskIsRunning();
    //查询巡视监控统计点(listIndex:0-任务下测点 1-任务下摄像头个数 2-任务下机器人点位 3-任务下巡视点个数)
    List<Long> cruiseInspectCount(@Param(value = "taskId")String taskId);

    String selectByIdentifyResult(@Param(value = "identifyResult")Integer identifyResult);
    String selectByIdentifyState(@Param(value = "identifyState")Integer identifyState);

    String selectUserName(@Param(value = "userID")Integer  userID);

    List<CruiseManualReview> selectManualDetail(@Param(value = "taskResultId")String taskResultId);
    int updateCheck(@Param(value = "taskResultId")String taskResultId,
                    @Param(value = "checkUserName")String checkUserName,
                    @Param(value = "checkDate")Date checkDate);

    TCruiseResult selectForTaskId(@Param(value = "taskId") String taskId);

    String selectAlgorithmType(@Param(value = "deviceMeteId") Long deviceMeteId);
}
