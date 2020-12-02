package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.TVoiceDevice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-12-01
 */
@Repository
public interface TVoiceDeviceDao {

    int add(TVoiceDevice tVoiceDevice);
    int deleteByPrimaryId(@Param(value = "voiceDeviceId") String voiceDeviceId);
    int update(TVoiceDevice tVoiceDevice);
    TVoiceDevice selectByPrimaryId(@Param(value = "voiceDeviceId") String voiceDeviceId);
    List<TVoiceDevice> select(@Param(value = "voiceDeviceId") String voiceDeviceId,
                                @Param(value = "voiceDeviceName") String voiceDeviceName,
                                @Param(value = "stdDeviceId") Long stdDeviceId,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "configId") String configId);
    List<TVoiceDevice> selectByPage(TVoiceDevice tVoiceDevice);

    int batchAdd(List<TVoiceDevice> list);
    int batchDelete(List<String> list);
    List<TVoiceDevice> selectAll();
}
