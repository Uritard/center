package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.device.entity.SYAllInfo;
import com.yjh.platform.module.device.entity.TCfgTeleadjust;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgTeleadjustDao {

    int insert(TCfgTeleadjust tCfgTeleadjust);
    int deleteByPrimaryId(@Param(value = "deviceId") String deviceId);
    int update(TCfgTeleadjust tCfgTeleadjust);
    TCfgTeleadjust selectByPrimaryId(@Param(value = "deviceId") String deviceId);
    List<TCfgTeleadjust> select(@Param(value = "deviceId") String deviceId,
                                @Param(value = "meteId") String meteId,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "upEffect") Float upEffect,
                                @Param(value = "downEffect") Float downEffect,
                                @Param(value = "metePrecision") Integer metePrecision,
                                @Param(value = "unit") String unit,
                                @Param(value = "meteIndex") Integer meteIndex,
                                @Param(value = "meteCid") Integer meteCid,
                                @Param(value = "adjustKind") Integer adjustKind,
                                @Param(value = "lastValue") Float lastValue,
                                @Param(value = "lastTime") Date lastTime,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "description") String description,
                                @Param(value = "stander") Float stander,
                                @Param(value = "controlenable") Integer controlenable);
    List<TCfgTeleadjust> selectByPage(TCfgTeleadjust tCfgTeleadjust);

    int batchInsert(List<TCfgTeleadjust> list);
    int insertForAll(SYAllInfo syAllInfo);
    int updateForAll(SYAllInfo syAllInfo);
}
