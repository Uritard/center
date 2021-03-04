package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @since 2020-11-19
 */
@Repository
public interface TRobotInfoDao {

    int insert(TRobotInfo tRobotInfo);
    int deleteByPrimaryId(@Param(value = "robotId") Long robotId);
    int update(TRobotInfo tRobotInfo);
    TRobotInfo selectByPrimaryId(@Param(value = "robotId") Long robotId);
    List<TRobotInfo> select(@Param(value = "robotId") Long robotId,
                            @Param(value = "robotCode") String robotCode,
                            @Param(value = "robotName") String robotName,
                            @Param(value = "robotStatus") String robotStatus,
                            @Param(value = "robotType") Integer robotType,
                            @Param(value = "robotIp") String robotIp,
                            @Param(value = "robotPort") Integer robotPort,
                            @Param(value = "lightIp") String lightIp,
                            @Param(value = "lightPort") String lightPort,
                            @Param(value = "identityManager") String identityManager,
                            @Param(value = "identityCode") String identityCode,
                            @Param(value = "lnferadIp") String lnferadIp,
                            @Param(value = "inferadPort") Integer inferadPort,
                            @Param(value = "inferadUsername") String inferadUsername,
                            @Param(value = "inferadPassword") String inferadPassword,
                            @Param(value = "photePath") String photePath,
                            @Param(value = "createBy") String createBy,
                            @Param(value = "createDate") Date createDate,
                            @Param(value = "updateBy") String updateBy,
                            @Param(value = "updateDate") Date updateDate,
                            @Param(value = "robotFactory") String robotFactory,
                            @Param(value = "isUse") String isUse,
                            @Param(value = "commissionDate") Date commissionDate,
                            @Param(value = "upRegionId") Long upRegionId,
                            @Param(value = "robotPosition") String robotPosition,
                            @Param(value = "remarks") String remarks);
    List<TRobotInfo> selectByPage(TRobotInfo tRobotInfo);
    int batchInsert(List<TRobotInfo> list);

    List<String> selectAllRobotCode();
    Long selectLastRobotId();
    String selectContent(@Param(value = "paramCode")String paramCode);
    List<String> selectOnline();
    Long selectRobotIdByCode(@Param(value = "robotCode") String robotCode);
    String selectDictCode(@Param(value = "colName")String colName,
                          @Param(value = "dictNote")String dictNote);
    String selectStatusByRobotCode(@Param(value = "robotCode") String robotCode);
    String selectInspectionCode(@Param(value = "instanceId") Long instanceId);
    TCruiseResult selectTaskResultId(@Param(value = "taskId") String taskId);
    TCruiseTask selectTCruiseTask(@Param(value = "taskId") String taskId);
    int insertRobotAlarm(TRobotAlarm tRobotAlarm);
    int batchInsertCruiseDataResult(List<TCruiseDataResult> list);
    int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list);
    int updateTCruiseResult(TCruiseResult tCruiseResult);
    int insertTCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult);

    List<AlgorithmDeviceMete> selectAlgorithm(@Param(value = "inspectionCode") String inspectionCode);

    List<TCruiseTaskResultDetail> selectRepairTCTRDList(@Param(value = "taskId")String taskId);
    List<TCruiseDataResult> selectRepairTCDRList(@Param(value = "taskId")String taskId);
}
