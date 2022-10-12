package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
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
}
