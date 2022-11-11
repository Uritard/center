package com.yjh.accessrobot.module.command.dao;


import com.yjh.accessrobot.module.command.entity.TCruisePointInstance;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_cruise_point_instance(巡检点实例表)】的数据库操作Mapper
* @createDate 2022-11-11 13:07:02
* @Entity generator.domain.TCruisePointInstance
*/
public interface TCruisePointInstanceMapper {

    int deleteByPrimaryKey(Long id);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );

    int insert(TCruisePointInstance record);

    int insertSelective(TCruisePointInstance record);

    TCruisePointInstance selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TCruisePointInstance record);

    int updateByPrimaryKey(TCruisePointInstance record);

    List<TCruisePointInstance> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int batchInsert(List<TCruisePointInstance> list);

}
