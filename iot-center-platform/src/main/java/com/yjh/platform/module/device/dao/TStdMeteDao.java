package com.yjh.platform.module.device.dao;

import java.math.BigDecimal;
import java.util.List;

import com.yjh.platform.module.device.entity.TStdMete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-07
 */
@Repository
public interface TStdMeteDao {

    int insert(TStdMete tStdMete);
    int deleteByPrimaryId(@Param(value = "stdMeteId") Long stdMeteId);
    int update(TStdMete tStdMete);
    TStdMete selectByPrimaryId(@Param(value = "stdMeteId") Long stdMeteId);
    List<TStdMete> select(@Param(value = "stdMeteId") Long stdMeteId,
                                @Param(value = "deviceType") Integer deviceType,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "meteType") String meteType,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "alarmNote") String alarmNote,
                                @Param(value = "alarmExplain") String alarmExplain,
                                @Param(value = "alarmType") String alarmType,
                                @Param(value = "unit") String unit,
                                @Param(value = "upEffect") Float upEffect,
                                @Param(value = "lowEffect") Float lowEffect,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "alarmLimit") Integer alarmLimit,
                                @Param(value = "alarmDelay") Integer alarmDelay,
                                @Param(value = "alarmCnt") Integer alarmCnt,
                                @Param(value = "thresholdAbs") BigDecimal thresholdAbs,
                                @Param(value = "thresholdPer") BigDecimal thresholdPer,
                                @Param(value = "modulus") Integer modulus,
                                @Param(value = "remark") String remark);
    List<TStdMete> selectByPage(TStdMete tStdMete);

    int batchInsert(List<TStdMete> list);
}
