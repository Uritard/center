package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.device.entity.CruiseCountOfType;
import com.yjh.platform.module.device.entity.TaskInfoBean;
import com.yjh.platform.module.patrol.entity.NonhomologousInfo;
import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.task.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Repository
public interface UPatrolResultDao {

    int add(UPatrolResult uPatrolResult);

    int deleteByPrimaryId(@Param(value = "taskId") String taskId);

    int update(UPatrolResult uPatrolResult);

    UPatrolResult selectByPrimaryId(@Param(value = "taskId") String taskId);

    List<UPatrolResult> select(@Param(value = "taskId") String taskId,
                               @Param(value = "taskCode") String taskCode,
                               @Param(value = "taskName") String taskName,
                               @Param(value = "areaId") String areaId,
                               @Param(value = "taskType") Integer taskType,
                               @Param(value = "executeType") Integer executeType,
                               @Param(value = "robotId") Long robotId,
                               @Param(value = "taskSource") Integer taskSource,
                               @Param(value = "taskLevel") Integer taskLevel,
                               @Param(value = "taskState") Integer taskState,
                               @Param(value = "modifyState") Integer modifyState,
                               @Param(value = "taskCount") Integer taskCount,
                               @Param(value = "taskWait") Integer taskWait,
                               @Param(value = "checkUser") String checkUser,
                               @Param(value = "checkDate") Date checkDate,
                               @Param(value = "weather") String weather,
                               @Param(value = "createTime") Date createTime,
                               @Param(value = "endTime") Date endTime,
                               @Param(value = "executeTime") Date executeTime,
                               @Param(value = "isReview") String isReview,
                               @Param(value = "taskAbnormal") Integer taskAbnormal,
                               @Param(value = "cruiseResult") Integer cruiseResult,
                               @Param(value = "remark") String remark);

    List<UPatrolResult> selectByPage(UPatrolResult uPatrolResult);

    int batchAdd(List<UPatrolResult> list);

    int batchDelete(List<String> list);

    Long selectDeviceMeteId(@Param(value = "instanceId") Long instanceId);

    AfterManualReviewInfo selectJudgeCondition(@Param(value = "instanceId") Long instanceId,
                                               @Param(value = "taskId") String taskId);

    List<TCruiseResultExpand> selectTaskByPage(@Param(value = "taskName") String taskName,
        @Param(value = "cState") Integer cState,
        @Param(value = "cType") Integer cType,
        @Param(value = "deviceType") Integer deviceType,
        @Param(value = "startDate") String startDate,
        @Param(value = "endDate") String endDate,
        @Param(value = "list") List<Long> list,
        @Param(value = "meteType") Integer meteType,
        @Param(value = "customId") String customId,
        @Param(value = "isCheck") Integer isCheck);
    List<String> selectTaskByPageByPage(@Param(value = "taskName") String taskName,
                                               @Param(value = "cState") Integer cState,
                                               @Param(value = "cType") Integer cType,
                                               @Param(value = "deviceType") Integer deviceType,
                                               @Param(value = "startDate") String startDate,
                                               @Param(value = "endDate") String endDate,
                                               @Param(value = "list") List<Long> list,
                                               @Param(value = "meteType") Integer meteType,
                                               @Param(value = "customId") String customId,
                                               @Param(value = "isCheck") Integer isCheck);

    List<CruiseResultDetail> selectCruiseByPage(@Param(value = "taskId") String taskId,
                                                @Param(value = "cruiseType") Integer cruiseType,
                                                @Param(value = "cruiseResult") Integer cruiseResult,
                                                @Param(value = "deviceType") Integer deviceType,
                                                @Param(value = "startTime") String startTime,
                                                @Param(value = "endTime") String endTime,
                                                @Param(value = "list") List<Long> list,
                                                @Param(value = "customId") String customId);

    List<CruiseResultDetail> selectAbnormalResult(@Param(value = "taskResultId") String taskResultId,
                                                  @Param(value = "cruiseType") Integer cruiseType,
                                                  @Param(value = "cruiseResult") Integer cruiseResult,
                                                  @Param(value = "deviceType") Integer deviceType,
                                                  @Param(value = "instanceName") String instanceName,
                                                  @Param(value = "startTime") String startTime,
                                                  @Param(value = "endTime") String endTime,
                                                  @Param(value = "list") List<Long> list,
                                                  @Param(value = "customId") String customId);

    //查询正在执行的任务
    List<TaskSimpleInfo> selectTaskIsRunning();

    TaskSimpleInfo selectTaskStateByTaskId(@Param(value = "taskId")String taskId);

    //统计当前任务下的巡检点数量
    int selectCruiseCount(@Param(value = "taskId")String taskId);

    //根据巡检点类型查询巡检点数量
    List<CruiseCountOfType> selectCruiseCountByType(@Param(value = "taskId")String taskId);

    Set<Long> selectInstanceIdByTask(@Param(value = "taskId")String taskId);

    List<CruiseInspectResult> selectCruiseInspectByTaskIdYC(@Param(value = "taskId")String taskId);

    /**
     * 巡视任务结果统计
     */
    List<StatisticalTools> taskStatistical(@Param(value = "colName1")String colName1,
                                           @Param(value = "start")String start,
                                           @Param(value = "end")String end);

    List<CruiseStatistical> cruiseStatisticalByAbnormal();

    /**
     * 查询巡视监控统计点(listIndex:0-任务下测点 1-任务下摄像头个数 2-任务下机器人点位 3-任务下巡视点个数)
     *
     */
    List<Long> cruiseInspectCount(@Param(value = "taskId")String taskId);

    List<UPatrolDataResult> selectCruiseDataResult(@Param(value = "taskId")String taskId);

    List<CruiseManualReview> selectManualDetail(@Param(value = "taskResultId")String taskResultId);

    int manualReview(CruiseManualReview cruiseManualReview);

    int manualReviewByTask(CruiseManualReview cruiseManualReview);

    int manualReviewByTaskInstance(CruiseManualReview cruiseManualReview);

    int updateDeviceMeteUpdate(TStdDeviceMeteUpdate tStdDeviceMeteUpdate);

    TStdDevicemete selectDeviceMeteInfo(@Param(value = "instanceId")Long instanceId);

    int updateIsWarn(@Param(value = "taskId") String taskId, @Param(value = "instanceId") Long instanceId);

    int updateWarnInfo(@Param(value = "warnId")Long warnId,
                       @Param(value = "warnName")String warnName,
                       @Param(value = "warnLevel")Integer warnLevel,
                       @Param(value = "warnContent")String warnContent,
                       @Param(value = "dealInfo")String dealInfo,
                       @Param(value = "dealType")int dealType,
                       @Param(value = "outRange")String outRange,
                       @Param(value = "dealPersonId")String dealPersonId,
                       @Param(value = "dealTime")Date dealTime);

    int updateWarnInfoByTask(@Param(value = "dealPersonId") String dealPersonId,
                             @Param(value = "dealTime") Date dealTime,
                             @Param(value = "taskId") String taskId);

    List<Long> selectWarnId(@Param(value = "taskId")String taskId,
                            @Param(value = "instanceId")Long instanceId);

    int updateCheck(@Param(value = "taskId")String taskId,
                    @Param(value = "checkUserName")String checkUserName,
                    @Param(value = "checkDate")Date checkDate,
                    @Param(value = "remark")String remark);

    String selectAlgorithmType(@Param(value = "deviceMeteId") Long deviceMeteId);

    TaskVO selectTaskNameAndTime(@Param(value = "taskId")String taskId);

    String selectReviewTaskFlag(@Param(value = "taskId")String taskId);

    List<TCruiseDataResultDetail> selectDetail(@Param(value = "list") List<String> list,
                                               @Param(value = "startTime")Date startTime,
                                               @Param(value = "endTime")Date endTime);

    int selectMeteNum(@Param(value = "list") List<String> list,
                      @Param(value = "startTime")Date startTime,
                      @Param(value = "endTime")Date endTime);

    int selectAbnormalNum(@Param(value = "list") List<String> list);//未处理数

    List<CheckPointType> selectMeteType(@Param(value = "list") List<String> list,
                                        @Param(value = "startTime")Date startTime,
                                        @Param(value = "endTime")Date endTime);

    List<CheckPointType> selectMeteType2(@Param(value = "taskId")String taskId);


    List<TCruiseDataResultDetail> selectTaskResult(@Param(value = "taskId")String taskId);

    int selectMeteNumByTask(@Param(value = "taskId")String taskId);

    int selectAbnormalNumByTask(@Param(value = "taskId")String taskId);

    List<Map<String, Object>> selectUpatrolTaskInfo(String taskId);

    int selectUpatrolTaskInfoById(String taskId);

    int selectTaskWait(String taskId);

    int selectReviewTask(String taskId);

    int selectCountByTaskId(String taskId);

    List<Long> batchSelectAttr(@Param("instanceList") List<Long> instanceList);

    TaskInfoBean queryTaskInfo();

    Map<String,Object> selectMeteInfoByInstanceId(@Param("instanceId")Long instanceId);

    List<NonhomologousInfo> selectWarnByTaskId(@Param(value = "taskId")String taskId);
}
