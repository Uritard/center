package com.yjh.platform.module.device.dao;

import java.math.BigDecimal;
import java.util.List;

import com.yjh.platform.module.device.entity.MeteInfo;
import com.yjh.platform.module.device.entity.TStdMete;
import com.yjh.platform.module.device.entity.TStdMeteDetail;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-07
 */
@Repository
public interface TStdMeteDao {

    int add(TStdMete tStdMete);
    int deleteByPrimaryId(@Param(value = "stdMeteId") Long stdMeteId);
    int update(TStdMete tStdMete);
    TStdMete selectByPrimaryId(@Param(value = "stdMeteId") Long stdMeteId);
    List<TStdMete> select(@Param(value = "stdMeteId") Long stdMeteId,
                                @Param(value = "deviceType") Integer deviceType,
                                @Param(value = "meteType") String meteType,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "alarmNote") String alarmNote,
                                @Param(value = "alarmExplain") String alarmExplain,
                                @Param(value = "alarmType") String alarmType,
                          @Param(value = "analyseType") Integer analyseType,
                                @Param(value = "unit") String unit,
                                @Param(value = "upEffect") Float upEffect,
                                @Param(value = "downEffect") Float lowEffect,
                                @Param(value = "alarmLevel") Integer alarmLevel,
                                @Param(value = "alarmLimit") Integer alarmLimit,
                                @Param(value = "highLimit1") Float highLimit1,
                                @Param(value = "lowLimit1") Float lowLimit1,
                                @Param(value = "highLimit2") Float highLimit2,
                                @Param(value = "lowLimit2") Float lowLimit2,
                                @Param(value = "highLimit3") Float highLimit3,
                                @Param(value = "lowLimit3") Float lowLimit3,
                                @Param(value = "highLimit4") Float highLimit4,
                                @Param(value = "lowLimit4") Float lowLimit4,
                                @Param(value = "alarmDelay") Integer alarmDelay,
                                @Param(value = "alarmCnt") Integer alarmCnt,
                                @Param(value = "thresholdAbs") BigDecimal thresholdAbs,
                                @Param(value = "thresholdPer") BigDecimal thresholdPer,
                                @Param(value = "modulus") Integer modulus,
                                @Param(value = "remark") String remark);
    List<TStdMeteDetail> selectByPage(@Param(value = "deviceType") Integer deviceType,
                                      @Param(value = "meteName") String meteName);
    int batchAdd(List<TStdMete> list);

    List<MeteInfo> selectByDeviceType(Integer deviceType);

    List<TDictBusiness>selectForDeviceTypeTree(@Param(value = "colName") String colName);
    int batchDelete(@Param(value = "list")List<String> list);


}
