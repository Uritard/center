package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Map;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.TSequentialConf;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2021-01-21
 */
@Repository
public interface TSequentialConfDao {

    int add(TSequentialConf tSequentialConf);
    int deleteByPrimaryId(@Param(value = "cfgDeviceId") String cfgDeviceId);
    int update(TSequentialConf tSequentialConf);
    TSequentialConf selectByPrimaryId(@Param(value = "cfgDeviceId") String cfgDeviceId);
    List<TSequentialConf> select(@Param(value = "cfgDeviceId") String cfgDeviceId,
                                @Param(value = "cfgMeteId") String cfgMeteId,
                                @Param(value = "presetId") Long presetId,
                                @Param(value = "identifyResult") String identifyResult);
    List<TSequentialConf> selectByPage(@Param(value = "cfgDeviceName") String cfgDeviceName);

    int batchAdd(List<TSequentialConf> list);
    int batchDelete(List<String> list);
    List<AreaInfo> selectForTCfgMete(@Param(value = "meteKind") Integer meteKind,
                                     @Param(value = "cfgDeviceName") String cfgDeviceName);
    List<Map<String,String>> selectForSequenceInfo(@Param(value = "cfgDeviceId") String cfgDeviceId);
    TSequentialConf selectSort(@Param(value = "cfgDeviceId") String cfgDeviceId);
    List<Long>selectCameraId();
    List<String> selectLastStep();
}
