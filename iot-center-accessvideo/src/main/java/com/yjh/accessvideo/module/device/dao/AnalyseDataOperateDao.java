package com.yjh.accessvideo.module.device.dao;

import com.yjh.accessvideo.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AnalyseDataOperateDao {

    int insertWarnInfo(TWarnInfo tWarnInfo);
    int batchInsertWarnInfo(List<TWarnInfo> list);

    int insertVideoAnalyseResult(TVideoAnalyseResult tVideoAnalyseResult);
    int batchInsertVideoAnalyseResult(List<TVideoAnalyseResult> list);

    TCruiseResult selectByPrimaryIdCruiseResult(@Param(value = "taskResultId")String taskResultId);
    int updateCruiseResult(TCruiseResult tCruiseResult);

    TCruiseTaskResult selectByPrimaryIdCruiseTaskResult(@Param(value = "taskResultId")String taskResultId);
    int updateCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult);
    int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult);

    int insertCruiseTaskResultDetail(TCruiseTaskResultDetail tCruiseTaskResultDetail);
    int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list);

    int insertCruiseDataResult(TCruiseDataResult tCruiseDataResult);
    int batchInsertCruiseDataResult(List<TCruiseDataResult> list);

    TStdDevicemete selectByPrimaryIdDeviceMete(@Param(value = "deviceMeteId") Long deviceMeteId);
    TStdDevicemete selectDeviceMeteByInstanceId(@Param(value = "instanceId")Long instanceId);

    TCruisePointInstance selectPointInstance(@Param(value = "instanceId")Long instanceId);
    String selectDictCode(@Param(value = "colName")String colName,@Param(value = "dictNote")String dictNote);
    String selectDictNote(@Param(value = "dictCode")String dictCode,@Param(value = "colName")String colName);
    List<TCruisePointInstance> selectCruiseByTaskId(@Param(value = "taskId")String taskId);

    int insertDefectInfo(TDefectInfo tDefectInfo);
    int batchInsertDefectInfo(List<TDefectInfo> list);
    Map<String,Object> selectWarnInfo(@Param(value = "deviceMeteId")Long deviceMeteId);

    //根据缺陷类型查询缺陷等级
    int selectAlgorithmDefectInfo(@Param(value = "defectType")Integer defectType);

}
