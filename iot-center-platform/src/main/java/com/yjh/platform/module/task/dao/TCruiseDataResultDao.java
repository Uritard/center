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
public interface TCruiseDataResultDao {

    int insert(TCruiseDataResult tCruiseDataResult);

    int deleteByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);

    int update(TCruiseDataResult tCruiseDataResult);

    TCruiseDataResult selectByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);

    List<TCruiseDataResult> select(@Param(value = "cruiseDataId") Long cruiseDataId,
                                   @Param(value = "cruiseResultId") String cruiseResultId,
                                   @Param(value = "cruiseId") Long cruiseId,
                                   @Param(value = "cruiseName") String cruiseName,
                                   @Param(value = "cruiseType") Integer cruiseType,
                                   @Param(value = "cruiseAbnormal") Integer cruiseAbnormal,
                                   @Param(value = "resultDesc") String resultDesc,
                                   @Param(value = "resultNum") String resultNum,
                                   @Param(value = "modifyNum") String modifyNum,
                                   @Param(value = "picPathAnl") String picPathAnl,
                                   @Param(value = "picpath") String picpath,
                                   @Param(value = "personCheck") String personCheck,
                                   @Param(value = "origPicAnl") String origPicAnl,
                                   @Param(value = "origpic") String origpic,
                                   @Param(value = "evaluationState") String evaluationState,
                                   @Param(value = "identifyState") Integer identifyState,
                                   @Param(value = "identifyResult") Integer identifyResult,
                                   @Param(value = "createtime") Date createtime,
                                   @Param(value = "remark") String remark,
                                   @Param(value = "checkUser") String checkUser,
                                   @Param(value = "checkDate") Date checkDate,
                                   @Param(value = "isWarn") Integer isWarn,
                                   @Param(value = "cruiseResult") Integer cruiseResult
    );

    List<TCruiseDataResult> selectByPage(TCruiseDataResult tCruiseDataResult);

    int batchInsert(List<TCruiseDataResult> list);

    CruiseResultAnalMeteInfo selectMeteCruiseByDeviceId(@Param(value = "deviceId") Long deviceId,
                                                        @Param(value = "deviceMeteId") Long deviceMeteId);
    CruiseResultAnalyzeMeteInfo selectMeteCruiseByDeviceId2(@Param(value = "instanceId") Long instanceId);
    CruiseResultAnalyzeMeteInfo selectMeteCruiseByDeviceId3(@Param(value = "deviceId") Long deviceId,
                                    @Param(value = "deviceMeteId") Long deviceMeteId);
    List<CruiseResultAnalInfo> selectCruiseDataResultByList(@Param(value = "cruiseType") Integer cruiseType,
                                                            @Param(value = "cType") Integer cType,
                                                            @Param(value = "deviceMeteId") Long deviceMeteId,
                                                            @Param(value = "endDate") Date endDate,
                                                            @Param(value = "startDate") Date startDate);
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
    List<Long> selectAllDeviceMeteId();
    TStdDeviceMeteUpdate selectCruiseAnalyze(@Param(value = "cruiseResultId") String cruiseResultId);
    int updateDeviceMeteUpdate(TStdDeviceMeteUpdate tStdDeviceMeteUpdate);
    int insertDeviceMeteUpdate(TStdDeviceMeteUpdate tStdDeviceMeteUpdate);
    List<String> test();
}
