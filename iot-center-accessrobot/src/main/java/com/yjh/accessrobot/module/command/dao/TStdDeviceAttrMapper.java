package com.yjh.accessrobot.module.command.dao;


import com.yjh.accessrobot.module.command.entity.TStdDeviceAttr;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_std_device_attr(标准化设备参数表)】的数据库操作Mapper
* @createDate 2022-11-29 20:28:06
* @Entity generator.domain.TStdDeviceAttr
*/
public interface TStdDeviceAttrMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TStdDeviceAttr record);

    int insertSelective(TStdDeviceAttr record);

    TStdDeviceAttr selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TStdDeviceAttr record);

    int updateByPrimaryKey(TStdDeviceAttr record);

    List<TStdDeviceAttr> selectByEdgeCode(@Param(value = "edgeCode") String edgeCode);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );

    int deleteByEdgeCode(@Param("edgeCode") String edgeCode);

    void batchInsert(List<TStdDeviceAttr> list);
}
