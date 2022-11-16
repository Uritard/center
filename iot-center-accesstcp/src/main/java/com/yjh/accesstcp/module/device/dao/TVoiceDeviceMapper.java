package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TVoiceDevice;
import com.yjh.accesstcp.module.device.entity.VoiceDeviceModel;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yanhao
 * @description 针对表【t_voice_device(声纹设备表)】的数据库操作Mapper
 * @createDate 2022-11-11 13:59:46
 * @Entity com.yjh.accesstcp.module.device.entity.TVoiceDevice
 */
@Repository
public interface TVoiceDeviceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TVoiceDevice record);

    int insertSelective(TVoiceDevice record);

    TVoiceDevice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TVoiceDevice record);

    int updateByPrimaryKey(TVoiceDevice record);

    List<VoiceDeviceModel> selectAll();

}
