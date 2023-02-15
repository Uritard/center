package com.yjh.accessrobot.module.command.dao;


import com.yjh.accessrobot.module.command.entity.TDeviceMaintenance;
import org.apache.commons.collections4.SetUtils;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_device_maintenance(设备区域检修表)】的数据库操作Mapper
* @createDate 2023-01-05 19:59:40
* @Entity generator.domain.TDeviceMaintenance
*/
public interface TDeviceMaintenanceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TDeviceMaintenance record);

    int insertSelective(TDeviceMaintenance record);

    TDeviceMaintenance selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TDeviceMaintenance record);

    int updateByPrimaryKey(TDeviceMaintenance record);

    int deleteByEdgeCode(String edgeNode);

    List<TDeviceMaintenance> selectByEdge(@Param(value = "edgeCode") String edgeCode);

    int deleteByEdgeCodeAndOriginId(@Param(value = "edgeCode")String edgeNode, @Param("originIdList") Collection<String> originIdList);

    int batchInsert(List<TDeviceMaintenance> list);

    List<String> selectDeviceIdsList(@Param(value = "originIds") String originIds);

    List<String> selectInstanceIdsList(@Param(value = "originIds") String originIds);

    List<String> selectDeviceIdsByInstanceList(List<String> list);

    List<String> selectInstanceIdsByDeviceIdList(List<String> list);
}
