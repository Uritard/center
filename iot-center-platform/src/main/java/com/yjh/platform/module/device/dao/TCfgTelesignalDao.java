package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.device.entity.TCfgTelesignal;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgTelesignalDao {

    int insert(TCfgTelesignal tCfgTelesignal);
    int deleteByPrimaryId(@Param(value = "deviceId") String deviceId);
    int update(TCfgTelesignal tCfgTelesignal);
    TCfgTelesignal selectByPrimaryId(@Param(value = "deviceId") String deviceId);
    List<TCfgTelesignal> select(@Param(value = "deviceId") String deviceId,
                                @Param(value = "meteId") String meteId,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "upEffect") Integer upEffect,
                                @Param(value = "downEffect") Integer downEffect,
                                @Param(value = "meteIndex") Integer meteIndex,
                                @Param(value = "meteCid") Integer meteCid,
                                @Param(value = "signalKind") Integer signalKind,
                                @Param(value = "lastValue") Integer lastValue,
                                @Param(value = "lastTime") Date lastTime,
                                @Param(value = "explainType") Integer explainType,
                                @Param(value = "reportType") Integer reportType,
                                @Param(value = "reportLevel") Integer reportLevel,
                                @Param(value = "maskType") Integer maskType,
                                @Param(value = "maskMid") String maskMid,
                                @Param(value = "maskString") String maskString,
                                @Param(value = "maskValue") Integer maskValue,
                                @Param(value = "delayTime") Integer delayTime,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "description") String description,
                                @Param(value = "isshield") Integer isshield,
                                @Param(value = "storageperiod") Long storageperiod,
                                @Param(value = "describer") String describer,
                                @Param(value = "alarmthresbhold") Integer alarmthresbhold,
                                @Param(value = "alarmlevel") Integer alarmlevel,
                                @Param(value = "linkMeteId") String linkMeteId);
    List<TCfgTelesignal> selectByPage(TCfgTelesignal tCfgTelesignal);

    int batchInsert(List<TCfgTelesignal> list);
}
