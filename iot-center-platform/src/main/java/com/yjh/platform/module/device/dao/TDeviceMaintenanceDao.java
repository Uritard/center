package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.device.entity.IdAndNameDetail;
import com.yjh.platform.module.device.entity.TDeviceMaintenance;
import com.yjh.platform.module.device.entity.TDeviceMaintenanceDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2021-01-11
 */
@Repository
public interface TDeviceMaintenanceDao {

    int add(TDeviceMaintenance tDeviceMaintenance);
    int deleteByPrimaryId(@Param(value = "maintenanceId") Long maintenanceId);
    int update(TDeviceMaintenance tDeviceMaintenance);
    List<TDeviceMaintenance> selectByPrimaryId(@Param(value = "maintenanceId") Long maintenanceId);
    List<TDeviceMaintenance> select(@Param(value = "maintenanceId") Long maintenanceId,
                                @Param(value = "maintenanceName") String maintenanceName,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "isValid") Integer isValid,
                                @Param(value = "maintenanceStart") Date maintenanceStart,
                                @Param(value = "maintenanceStop") Date maintenanceStop);
    List<TDeviceMaintenanceDetail> selectByPage(@Param(value = "maintenanceName") String maintenanceName);

    int batchAdd(List<TDeviceMaintenance> list);
    int batchDelete(List<String> list);
    List<IdAndNameDetail> selectDevice(@Param(value = "upRegionId") Long upRegionId);
    List<IdAndNameDetail> selectInstance(@Param(value = "deviceId") Long deviceId);
    List<IdAndNameDetail> selectIdAndName(@Param(value = "maintenanceId") Long maintenanceId);
    int deleteByDeviceId(@Param(value = "deviceId") Long deviceId);


}
