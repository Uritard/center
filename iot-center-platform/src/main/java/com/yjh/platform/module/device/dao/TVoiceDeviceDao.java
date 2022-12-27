package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    List<VoiceDeviceAllInfoDetail> selectByPrimaryIds(@Param(value = "list") List<Long> voiceDeviceId);
    List<TVoiceDevice> select(@Param(value = "voiceDeviceId") Long voiceDeviceId,
                                @Param(value = "voiceDeviceName") String voiceDeviceName,
                                @Param(value = "stdDeviceId") Long stdDeviceId,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "configId") Long configId,
                                @Param(value = "upRegionId")Long upRegionId,
                                @Param(value = "voiceType")String voiceType,
                                @Param(value = "voiceModel")String voiceModel,
                                @Param(value = "voiceFactory")String voiceFactory);
    List<VoiceDeviceAllInfoDetail> selectByPage(@Param(value = "voiceDeviceName") String voiceDeviceName,
                                    @Param(value = "deviceType") String deviceType,
                                    @Param(value = "list") List<Long> list,
                                    @Param(value = "upRegionId")Long upRegionId,
                                    @Param(value = "voiceType")String voiceType,
                                    @Param(value = "voiceModel")String voiceModel,
                                    @Param(value = "voiceFactory")String voiceFactory);

    int batchAdd(List<VoiceDeviceAllInfoDetail> list);
    int batchDelete(List<String> list);
    List<VoiceDevice> selectAll(@Param(value = "voiceDeviceName") String voiceDeviceName,
                                @Param(value = "userId")Long userId);
    VoiceDeviceInfoDetail selectVoiceInfo(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    List<VoiceDeviceAllInfo>selectVoiceDeviceInfo();
    VoiceDeviceAllInfo selectById(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    int addConf(VoiceDeviceAllInfoDetail voiceDeviceAllInfoDetail);
    int updateConf(VoiceDeviceAllInfoDetail voiceDeviceAllInfoDetail);
    int deleteConf(@Param(value = "configId") Long configId);
    VoiceDeviceInfoDetail selectFrequencyInfo(@Param(value = "voiceDeviceId") Long voiceDeviceId);
    List<VoiceDevice> selectVoiceTree();
    int  selectCount();}
