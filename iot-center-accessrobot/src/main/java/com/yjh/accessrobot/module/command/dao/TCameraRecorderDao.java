package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TCameraRecorder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author yanhao
 * @description 针对表【t_camera_recorder(录像服务器表)】的数据库操作Mapper
 * @createDate 2022-11-08
 * @Entity com.yjh.accessrobot.module.command.entity.TCameraRecorder
 */
@Repository
public interface TCameraRecorderDao {

    int deleteByPrimaryKey(Long id);

    int insert(TCameraRecorder record);

    int insertSelective(TCameraRecorder record);

    TCameraRecorder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TCameraRecorder record);

    int updateByPrimaryKey(TCameraRecorder record);

    List<TCameraRecorder> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );

    int batchInsert(Collection<TCameraRecorder> cameraRecorderCollection);

}
