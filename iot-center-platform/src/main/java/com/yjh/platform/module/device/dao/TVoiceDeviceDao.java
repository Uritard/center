package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-12-01
 */
@Repository
public interface TVoiceDeviceDao {

    int add(VoiceDeviceAllInfoDetail tVoiceDevice);
    int deleteByPrimaryId(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    int update(VoiceDeviceAllInfoDetail tVoiceDevice);
    VoiceDeviceAllInfoDetail selectByPrimaryId(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    List<TVoiceDevice> select(@Param(value = "voiceDeviceId") Long voiceDeviceId,
                                @Param(value = "voiceDeviceName") String voiceDeviceName,
                                @Param(value = "stdDeviceId") Long stdDeviceId,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "configId") Long configId,
                              @Param(value = "upRegionId")Long upRegionId);
    List<VoiceDeviceAllInfoDetail> selectByPage(@Param(value = "voiceDeviceName") String voiceDeviceName,
                                    @Param(value = "deviceType") String deviceType,
                                    @Param(value = "list") List<Long> list,
                                                @Param(value = "upRegionId")Long upRegionId);

    int batchAdd(List<VoiceDeviceAllInfoDetail> list);
    int batchDelete(List<String> list);
    List<VoiceDevice> selectAll(@Param(value = "voiceDeviceName") String voiceDeviceName);
    VoiceDeviceInfoDetail selectVoiceInfo(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    List<VoiceDeviceAllInfo>selectVoiceDeviceInfo();
    VoiceDeviceAllInfo selectById(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    int addConf(VoiceDeviceAllInfoDetail voiceDeviceAllInfoDetail);
    int updateConf(VoiceDeviceAllInfoDetail voiceDeviceAllInfoDetail);
    int deleteConf(@Param(value = "configId") Long configId);
    VoiceDeviceInfoDetail selectFrequencyInfo(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    List<VoiceDevice> selectVoiceTree();
}
