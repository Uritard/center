package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.task.entity.TWarnInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface AnalyseDataOperateDao {

    int insertWarnInfo(TWarnInfo tWarnInfo);

    int batchInsertWarnInfo(List<TWarnInfo> list);

    int insertVideoAnalyseResult(TVideoAnalyseResult tVideoAnalyseResult);

    int batchInsertVideoAnalyseResult(List<TVideoAnalyseResult> list);

    TCruiseResult selectByPrimaryIdCruiseResult(@Param(value = "taskResultId") String taskResultId,
                                                @Param(value = "taskId") String taskId);

    int updateCruiseResult(TCruiseResult tCruiseResult);

    TCruiseTaskResult selectByPrimaryIdCruiseTaskResult(@Param(value = "taskResultId") String taskResultId);

    int updateCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult);

    int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult);

    int insertCruiseTaskResultDetail(TCruiseTaskResultDetail tCruiseTaskResultDetail);

    int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list);

    int insertCruiseDataResult(TCruiseDataResult tCruiseDataResult);

    int batchInsertCruiseDataResult(List<TCruiseDataResult> list);

    TStdDeviceMete selectByPrimaryIdDeviceMete(@Param(value = "deviceMeteId") Long deviceMeteId);

    TStdDeviceMete selectDeviceMeteByInstanceId(@Param(value = "instanceId") Long instanceId);

    TCruisePointInstance selectPointInstance(@Param(value = "instanceId") Long instanceId);

    String selectDictCode(@Param(value = "colName") String colName, @Param(value = "dictNote") String dictNote);

    String selectDictCodeByUpDict(@Param(value = "colName") String colName,
                                  @Param(value = "upDict") String upDict);

    String selectDictNote(@Param(value = "dictCode") String dictCode, @Param(value = "colName") String colName);

    String selectAlarmLevel(@Param(value = "aliasName")String aliasName);

    List<TCruisePointInstance> selectCruiseByTaskId(@Param(value = "taskId") String taskId);

    int insertDefectInfo(TDefectInfo tDefectInfo);

    int batchInsertDefectInfo(List<TDefectInfo> list);

    Map<String, Object> selectWarnInfo(@Param(value = "deviceMeteId") Long deviceMeteId);

    //根据缺陷类型查询缺陷等级
    int selectAlgorithmDefectInfo(@Param(value = "defectType") Integer defectType);

    //查询任务结束后TCTR中是否已被插入数据
    String selectLaterTaskCruiseResult(@Param(value = "taskId") String taskId);

    //根据任务ID与巡视点ID查询相机ID与预置位ID
    Map<String,Long> selectTaskCruiseCameraInfo(@Param(value = "taskId")String taskId,
                                                @Param(value = "instanceId")Long instanceId);

    //根据巡视点ID查询预置位图片
    String selectPresetImgByCruise(@Param(value = "instanceId")Long instanceId);
    //查看当前最新的一条告警信息
    Long selectCurrentWarn();
    //查看当前最新的一条缺陷信息
    Long selectCurrentDefect();

    Map<String, Object> selectInstanceInfo(@Param(value = "presetId")Long presetId);
    List<TAlgorithmInfo> selectAlgorithmInfo(@Param(value = "aliasName")String aliasName);
    List<String> selectAlgorithmType();

    Map<String, Object> selectAlgorithmByInstanceId(@Param(value = "instanceId")Long instanceId);

    String selectDevicePointIdByInstanceId(@Param(value = "instanceId")Long instanceId);

    HashMap<String,String> selectDeviceNameInfo(@Param(value = "instanceId") Long instanceId);

    String selectPMSByCameraId(@Param(value = "cameraId") Long cameraId);

    String selectAnalyseType(@Param(value = "instanceId") Long instanceId);

    String selectMaterialId(@Param(value = "deviceId") Long deviceId);


    HashMap<String, String> selectPatrolDevice(@Param(value = "instanceId") String instanceId);

    Long selectPresetIdByInstanceId(@Param(value = "instanceId") String instanceId);

    int selectRobotCodeIsExist(@Param(value = "robotCode") String robotCode);
}
