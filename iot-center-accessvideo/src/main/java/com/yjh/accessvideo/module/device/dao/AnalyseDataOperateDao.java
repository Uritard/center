package com.yjh.accessvideo.module.device.dao;

import com.yjh.accessvideo.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyseDataOperateDao {

    int insertWarnInfo(TWarnInfo tWarnInfo);
    int batchInsertWarnInfo(List<TWarnInfo> list);

    int insertVideoAnalyseResult(TVideoAnalyseResult tVideoAnalyseResult);
    int batchInsertVideoAnalyseResult(List<TVideoAnalyseResult> list);

    int insertCruiseResult(TCruiseResult tCruiseResult);
    int batchInsertCruiseResult(List<TCruiseResult> list);

    int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult);
    int batchInsertCruiseTaskResult(List<TCruiseTaskResult> list);

    int insertCruiseTaskResultDetail(TCruiseTaskResultDetail tCruiseTaskResultDetail);
    int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list);

    int insertCruiseDataResult(TCruiseDataResult tCruiseDataResult);
    int batchInsertCruiseDataResult(List<TCruiseDataResult> list);

    TStdDevicemete selectByPrimaryIdDeviceMete(@Param(value = "deviceMeteId") Long deviceMeteId);
    TStdDevicemete selectDeviceMeteByInstanceId(@Param(value = "instanceId")Long instanceId);


}
