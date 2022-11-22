package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.task.entity.BrokenLineInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeInfo;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.task.entity.TStdDeviceMeteUpdate;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Repository
public interface UPatrolDataResultDao {

    int add(UPatrolDataResult uPatrolDataResult);
    int deleteByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);
    int update(UPatrolDataResult uPatrolDataResult);
    UPatrolDataResult selectByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);
    List<UPatrolDataResult> select(@Param(value = "cruiseDataId") Long cruiseDataId,
                                @Param(value = "taskId") String taskId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "deviceName") String deviceName,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "instanceName") String instanceName,
                                @Param(value = "cruiseId") Long cruiseId,
                                @Param(value = "cruiseName") String cruiseName,
                                @Param(value = "cruiseTime") Date cruiseTime,
                                @Param(value = "cruiseStatus") Integer cruiseStatus,
                                @Param(value = "cruiseType") Integer cruiseType,
                                @Param(value = "resultDesc") String resultDesc,
                                @Param(value = "resultNum") String resultNum,
                                @Param(value = "modifyNum") String modifyNum,
                                @Param(value = "picpath") String picpath,
                                @Param(value = "confirmPicPath") String confirmPicPath,
                                @Param(value = "picPathAnl") String picPathAnl,
                                @Param(value = "personCheck") String personCheck,
                                @Param(value = "origpic") String origpic,
                                @Param(value = "origConfirmPicPath") String origConfirmPicPath,
                                @Param(value = "origPicAnl") String origPicAnl,
                                @Param(value = "cruiseAbnormal") Integer cruiseAbnormal,
                                @Param(value = "evaluationState") Integer evaluationState,
                                @Param(value = "identifyState") Integer identifyState,
                                @Param(value = "identifyResult") Integer identifyResult,
                                @Param(value = "createtime") Date createtime,
                                @Param(value = "remark") String remark,
                                @Param(value = "checkUser") String checkUser,
                                @Param(value = "checkDate") Date checkDate,
                                @Param(value = "isWarn") Integer isWarn,
                                @Param(value = "cruiseResult") Integer cruiseResult,
                                @Param(value = "firName") String firName,
                                @Param(value = "firDate") Date firDate,
                                @Param(value = "resultPic") String resultPic,
                                @Param(value = "points") String points,
                                @Param(value = "voicePath") String voicePath);
    List<UPatrolDataResult> selectByPage(UPatrolDataResult uPatrolDataResult);

    int batchAdd(List<UPatrolDataResult> list);
    int batchDelete(List<String> list);

    List<CruiseResultAnalyzeInfo> selectCruiseDataResultByList2(@Param(value = "cruiseType") Integer cruiseType,
        @Param(value = "cType") Integer cType,
        @Param(value = "deviceMeteId") Long deviceMeteId,
        @Param(value = "meteType") String meteType,
        @Param(value = "meterType") Integer meterType,
        @Param(value = "endTime") String endTime,
        @Param(value = "startTime") String startTime);

    List<CruiseResultAnalyzeInfo> selectCruiseDataReport(@Param(value = "cType") Integer cType,
        @Param(value = "meteType") String meteType,
        @Param(value = "meterType") Integer meterType,
        @Param(value = "endTime") String endTime,
        @Param(value = "startTime") String startTime,
        @Param(value = "list") List<Long> list,
        @Param(value = "instanceName") String instanceName);

    List<BrokenLineInfo> selectBrokenLine(@Param(value = "cruiseType") Integer cruiseType,
        @Param(value = "cType") Integer cType,
        @Param(value = "deviceMeteId") Long deviceMeteId,
        @Param(value = "startTime") String startTime,
        @Param(value = "endTime") String endTime,
        @Param(value = "meteType") String meteType,
        @Param(value = "meterType") Integer meterType);

    List<TCruiseDataResult> selectByCameraId(@Param(value = "cameraId")Long cameraId,
        @Param(value = "startDate")String startDate,
        @Param(value = "endDate")String endDate,
        @Param(value = "firName")String firName);
    /**
     * 插入巡视任务结果数据
     * @param list 巡视点结果集
     * @return int
     */
    int batchInsertUPatrolDataResult(List<UPatrolDataResult> list);

    List<Long> selectAllDeviceMeteId();

    List<TStdDeviceMeteUpdate> selectDeviceMeteList(String taskId);

    int updateDeviceMeteUpdate(TStdDeviceMeteUpdate tStdDeviceMeteUpdate);

    int insertDeviceMeteUpdate(TStdDeviceMeteUpdate tStdDeviceMeteUpdate);
}
