package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.entity.BrokenLineInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalMeteInfo;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface TCruiseDataResultDao {

    int insert(TCruiseDataResult tCruiseDataResult);
    int deleteByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);
    int update(TCruiseDataResult tCruiseDataResult);
    TCruiseDataResult selectByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);
    List<TCruiseDataResult> select(@Param(value = "cruiseDataId") Long cruiseDataId,
                                @Param(value = "cruiseResultId") String cruiseResultId,
                                @Param(value = "cruiseId") Long cruiseId,
                                @Param(value = "pointType") Integer pointType,
                                @Param(value = "resultDesc") String resultDesc,
                                @Param(value = "resultNum") String resultNum,
                                @Param(value = "modifyNum") String modifyNum,
                                @Param(value = "picpath") String picpath,
                                @Param(value = "personcheck") String personcheck,
                                @Param(value = "origpic") String origpic,
                                @Param(value = "state") Integer state,
                                @Param(value = "evaluationState") String evaluationState,
                                @Param(value = "identifyState") Integer identifyState,
                                @Param(value = "identifyResult") Integer identifyResult,
                                @Param(value = "createtime") Date createtime,
                                @Param(value = "remark") String remark);
    List<TCruiseDataResult> selectByPage(TCruiseDataResult tCruiseDataResult);

    int batchInsert(List<TCruiseDataResult> list);
    CruiseResultAnalMeteInfo selectMeteCruiseByDeviceId(@Param(value = "deviceId")Long deviceId,
                                                        @Param(value = "deviceMeteId")Long deviceMeteId);
    List<CruiseResultAnalInfo> selectCruiseDataResultByList(@Param(value = "cruiseType")Integer cruiseType,
                                                            @Param(value = "cType")Integer cType,
                                                            @Param(value = "deviceMeteId")Long deviceMeteId,
                                                            @Param(value = "endDate")Date endDate,
                                                            @Param(value = "startDate")Date startDate);

    List<BrokenLineInfo> selectBrokenLine(@Param(value = "cruiseType")Integer cruiseType,
                                          @Param(value = "cType")Integer cType,
                                          @Param(value = "deviceMeteId")Long deviceMeteId,
                                          @Param(value = "endDate")Date endDate,
                                          @Param(value = "startDate")Date startDate);
}
