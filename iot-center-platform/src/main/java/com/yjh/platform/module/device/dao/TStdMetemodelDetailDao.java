package com.yjh.platform.module.device.dao;

import java.math.BigDecimal;
import java.util.List;

import com.yjh.platform.module.device.entity.TStdMeteModel;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-07
 */
@Repository
public interface TStdMetemodelDetailDao {

    int add(TStdMeteModelDetail tStdMeteModelDetail);
    int deleteByPrimaryId(@Param(value = "modelId") Long modelId);
    int update(TStdMeteModelDetail tStdMeteModelDetail);
    TStdMeteModelDetail selectByPrimaryId(@Param(value = "modelId") Long modelId);
    List<TStdMeteModelDetail> select(@Param(value = "modelId") Long modelId,
                                     @Param(value = "meteId") Long meteId,
                                     @Param(value = "customType") Integer customType,
                                     @Param(value = "meteCode") String meteCode,
                                     @Param(value = "meteName") String meteName,
                                     @Param(value = "meteType") String meteType,
                                     @Param(value = "unit") String unit,
                                     @Param(value = "alarmNote") String alarmNote,
                                     @Param(value = "alarmExplain") String alarmExplain,
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
                                     @Param(value = "modulus") Integer modulus);
    List<TStdMeteModelDetail> selectByPage(TStdMeteModelDetail tStdMeteModelDetail);

    int batchAdd(List<TStdMeteModelDetail> list);

}
