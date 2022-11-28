package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TCameraInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
* @author yanhao
* @description 针对表【t_camera_info(摄像头信息表)】的数据库操作Mapper
* @createDate 2022-11-09 16:08:05
* @Entity com.yjh.accessrobot.module.command.entity.TCameraInfo
*/
@Repository
public interface TCameraInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TCameraInfo record);

    int insertSelective(TCameraInfo record);

    TCameraInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TCameraInfo record);

    int updateByPrimaryKey(TCameraInfo record);

    List<TCameraInfo> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );

    int insertBatch(@Param("tCameraInfoList")Collection<TCameraInfo> tCameraInfoList);

    int deleteByEdgeCode(@Param("edgeCode") String edgeCode);

}
