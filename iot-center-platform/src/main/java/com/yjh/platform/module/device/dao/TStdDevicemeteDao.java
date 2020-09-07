package com.yjh.platform.module.device.dao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.entity.TStdDeviceMeteDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-08
 */
@Repository
public interface TStdDevicemeteDao {

    int add(TStdDeviceMete tStdDeviceMete);
    int deleteByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    int update(TStdDeviceMete tStdDeviceMete);
    TStdDeviceMete selectByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    List<TStdDeviceMete> select(@Param(value = "deviceMeteId") Long deviceMeteId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "customId") String customId,
                                @Param(value = "meteId") Long meteId,
                                @Param(value = "meteKind") String meteKind,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "deviceType") Integer deviceType,
                                @Param(value = "customType") Integer customType,
                                @Param(value = "positionType") String positionType,
                                @Param(value = "unit") String unit,
                                @Param(value = "alarmNote") String alarmNote,
                                @Param(value = "alarmType") String alarmType,
                                @Param(value = "upEffect") Float upEffect,
                                @Param(value = "lowEffect") Float lowEffect,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "highLimit1") Float highLimit1,
                                @Param(value = "lowLimit1") Float lowLimit1,
                                @Param(value = "highLimit2") Float highLimit2,
                                @Param(value = "lowLimit2") Float lowLimit2,
                                @Param(value = "alarmDelay") Integer alarmDelay,
                                @Param(value = "alarmCnt") Integer alarmCnt,
                                @Param(value = "thresholdAbs") BigDecimal thresholdAbs,
                                @Param(value = "thresholdPer") BigDecimal thresholdPer,
                                @Param(value = "meteType") String meteType,
                                @Param(value = "modulus") Integer modulus,
                                @Param(value = "remark") String remark,
                                @Param(value = "stateZero") String stateZero,
                                @Param(value = "stateOne") String stateOne,
                                @Param(value = "alarmState") Integer alarmState);
    List<TStdDeviceMeteDetail> selectByPage(TStdDeviceMeteDetail tStdDeviceMeteDetail);

    int batchAdd(List<TStdDeviceMete> list);
    List<TStdDeviceMete> selectDevMeteByModelId(@Param(value = "modelId") Long modelId);
    int deleteByDevId(@Param(value = "deviceId") Long deviceId);
    List<TStdDeviceMete>selectByDevCus(@Param(value="deviceId")Long deviceId,@Param(value="customId")Long customId);

    List<TStdDeviceMete>selectPreDeviceMete(@Param(value ="modelId")Long modelId,@Param(value = "deviceId")Long deviceId,@Param(value = "customId")Long customId);

    int batchDelete(@Param(value = "list") List<String> list);



}
