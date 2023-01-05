package com.yjh.accessrobot.module.command.dao;


import com.yjh.accessrobot.module.command.entity.TDeviceMaintenance;

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

}
