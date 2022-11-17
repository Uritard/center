package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TVoiceConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
* @author yanhao
* @description 针对表【t_voice_config(声纹ftp配置表)】的数据库操作Mapper
* @createDate 2022-11-17 10:25:11
* @Entity com.yjh.accessrobot.module.command.entity.TVoiceConfig
*/

@Repository
public interface TVoiceConfigMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TVoiceConfig record);

    int insertSelective(TVoiceConfig record);

    TVoiceConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TVoiceConfig record);

    int updateByPrimaryKey(TVoiceConfig record);

    int insertBatch(Collection<TVoiceConfig> collection);

    void deleteByDeviceEdgeCodeAndOriginId(@Param("edgeCode") String edgeNode,@Param("originIdList")  Collection<String> deleteIdCollection);
}
