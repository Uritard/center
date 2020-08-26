package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.device.entity.TCfgTelemeter;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgTelemeterDao {

    int insert(TCfgTelemeter tCfgTelemeter);
    int deleteByPrimaryId(@Param(value = "deviceId") String deviceId);
    int update(TCfgTelemeter tCfgTelemeter);
    TCfgTelemeter selectByPrimaryId(@Param(value = "deviceId") String deviceId);
    List<TCfgTelemeter> select(@Param(value = "deviceId") String deviceId,
                                @Param(value = "meteId") String meteId,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "upEffect") Float upEffect,
                                @Param(value = "lowEffect") Float lowEffect,
                                @Param(value = "metePrecision") Integer metePrecision,
                                @Param(value = "unit") String unit,
                                @Param(value = "meteIndex") Integer meteIndex,
                                @Param(value = "meteCid") Integer meteCid,
                                @Param(value = "limitBand") Float limitBand,
                                @Param(value = "changeLimit") Float changeLimit,
                                @Param(value = "validMid") String validMid,
                                @Param(value = "validString") String validString,
                                @Param(value = "invalidValue") Float invalidValue,
                                @Param(value = "lastValue") Float lastValue,
                                @Param(value = "lastTime") Date lastTime,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "description") String description,
                                @Param(value = "hilimit1") Float hilimit1,
                                @Param(value = "lolimit1") Float lolimit1,
                                @Param(value = "hilimit2") Float hilimit2,
                                @Param(value = "lolimit2") Float lolimit2,
                                @Param(value = "hilimit3") Float hilimit3,
                                @Param(value = "lolimit3") Float lolimit3,
                                @Param(value = "hilimit4") Float hilimit4,
                                @Param(value = "lolimit4") Float lolimit4,
                                @Param(value = "stander") Float stander,
                                @Param(value = "isshield") Integer isshield,
                                @Param(value = "storageperiod") Long storageperiod,
                                @Param(value = "linkMeteId") String linkMeteId);
    List<TCfgTelemeter> selectByPage(TCfgTelemeter tCfgTelemeter);

    int batchInsert(List<TCfgTelemeter> list);
}
