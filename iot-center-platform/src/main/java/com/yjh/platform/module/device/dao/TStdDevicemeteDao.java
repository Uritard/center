package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.entity.TStdDeviceMeteDetail;
import com.yjh.platform.module.task.entity.CruiseResultAnalMeteInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeMeteInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author tt
 * @since 2020-08-08
 */
@Repository
public interface TStdDevicemeteDao {

    int add(TStdDeviceMeteDetail tStdDeviceMeteDetail);
    int deleteByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    int deleteByDeviceId(@Param(value = "deviceId") Long deviceId);
    int update(TStdDeviceMeteDetail tStdDeviceMeteDetail);
    TStdDeviceMete selectByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    List<TStdDeviceMete> select(@Param(value = "deviceMeteId") Long deviceMeteId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "customId") String customId,
                                @Param(value = "meteId") Long meteId,
                                @Param(value = "meteKind") String meteKind,
                                @Param(value = "meteType") String meteType,

                                @Param(value = "meteName") String meteName,

                                @Param(value = "deviceType") Integer deviceType,
                                @Param(value = "positionType") String positionType,
                                @Param(value = "analyseType") Integer analyseType,
                                @Param(value = "unit") String unit,
                                @Param(value = "alarmNote") String alarmNote,
                                @Param(value = "alarmType") String alarmType,
                                @Param(value = "upEffect") Float upEffect,
                                @Param(value = "downEffect") Float downEffect,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "highLimit1") Float highLimit1,
                                @Param(value = "lowLimit1") Float lowLimit1,
                                @Param(value = "highLimit2") Float highLimit2,
                                @Param(value = "lowLimit2") Float lowLimit2,
                                @Param(value = "alarmDelay") Integer alarmDelay,
                                @Param(value = "alarmCnt") Integer alarmCnt,
                                @Param(value = "thresholdAbs") BigDecimal thresholdAbs,
                                @Param(value = "thresholdPer") BigDecimal thresholdPer,
                                @Param(value = "modulus") Integer modulus,
                                @Param(value = "remark") String remark,
                                @Param(value = "stateZero") String stateZero,
                                @Param(value = "stateOne") String stateOne,
                                @Param(value = "alarmState") Integer alarmState,
                                @Param(value = "meterType") Integer meterType,
                                @Param(value = "appearanceType") Integer appearanceType
                                );
    List<TStdDeviceMeteDetail> selectByPage(TStdDeviceMeteDetail tStdDeviceMeteDetail);

    List<Long> selectDeviceMeteByDeviceCustom(@Param("deviceId")Long deviceId,@Param("customId")String customId);
    int batchAdd(List<TStdDeviceMete> list);
    List<TStdDeviceMete> selectDevMeteByModelId(@Param(value = "modelId") Long modelId);
    int deleteByDevId(@Param(value = "deviceId") Long deviceId);
    List<TStdDeviceMete>selectByDevCus(@Param(value="deviceId")Long deviceId,@Param(value="customId")String customId);

    List<TStdDeviceMete>selectPreDeviceMete(@Param(value ="modelId")Long modelId,@Param(value = "deviceId")Long deviceId,@Param(value = "customType")Long customType);
    int batchDelete(@Param(value = "list") List<String> list);

    List<CruiseResultAnalMeteInfo> selectDeviceMeteByDeviceId(List<Long> deviceIds);
    List<CruiseResultAnalMeteInfo> selectDeviceMete();

    List<CruiseResultAnalyzeMeteInfo> selectCruiseResultAnalyze(@Param(value ="list")List<Long> list,
                                                                @Param(value ="deviceType")Integer deviceType,
                                                                @Param(value ="meteType")String meteType,
                                                                @Param(value ="meterType")Integer meterType);
   //通过巡检点Id查询标准测点
    Long getdeviceMeteByPointinstance(@Param(value = "instanceId")Long instanceId);
    List<Long> selectByDevId(@Param(value = "deviceId")Long deviceId);

    List<Long> selectHave(@Param(value = "deviceMeteId")Long deviceMeteId);



}
