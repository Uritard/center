package com.yjh.accesstcp.module.device.dao;


import com.yjh.accesstcp.module.device.entity.TCameraPreset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_camera_preset(摄像机预位置表)】的数据库操作Mapper
* @createDate 2022-11-11 11:05:16
* @Entity generator.domain.TCameraPreset
*/
@Repository
public interface TCameraPresetMapper {

    int deleteByPrimaryKey(Long id);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );

    int deleteByEdgeCode(@Param("edgeCode") String edgeCode);

    int insert(TCameraPreset record);

    int insertSelective(TCameraPreset record);

    TCameraPreset selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TCameraPreset record);

    int updateByPrimaryKey(TCameraPreset record);

    int updatePtzByPrimaryKey(TCameraPreset record);

    List<TCameraPreset> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int batchInsert(List<TCameraPreset> list);

    TCameraPreset selectByEdgeCodeAndOriginId (@Param("edgeCode") String edgeCode ,@Param("originId") String originId );
}
