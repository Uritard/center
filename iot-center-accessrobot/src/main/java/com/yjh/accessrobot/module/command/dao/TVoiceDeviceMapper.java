package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TVoiceDevice;

/**
* @author yanhao
* @description 针对表【t_voice_device(声纹设备表)】的数据库操作Mapper
* @createDate 2022-11-11 13:54:22
* @Entity com.yjh.accessrobot.module.command.entity.TVoiceDevice
*/
public interface TVoiceDeviceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TVoiceDevice record);

    int insertSelective(TVoiceDevice record);

    TVoiceDevice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TVoiceDevice record);

    int updateByPrimaryKey(TVoiceDevice record);

}
