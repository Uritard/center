package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TVoiceDevice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
* @author yanhao
* @description 针对表【t_voice_device(声纹设备表)】的数据库操作Mapper
* @createDate 2022-11-14
* @Entity com.yjh.accessrobot.module.command.entity.TVoiceDevice

*/
@Repository
public interface TVoiceDeviceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TVoiceDevice record);

    int insertSelective(TVoiceDevice record);

    TVoiceDevice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TVoiceDevice record);

    int updateByPrimaryKey(TVoiceDevice record);

    List<TVoiceDevice> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int insertBatch(List<TVoiceDevice> list);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeNode, @Param("originIdList") Collection<String> deleteIdCollection);

    int deleteByEdgeCode(@Param("edgeCode") String edgeNode);
}
