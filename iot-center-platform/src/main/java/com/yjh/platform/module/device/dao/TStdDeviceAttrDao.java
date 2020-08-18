package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TStdDeviceAttrDao {

    //根据主键查询
    TStdDeviceAttr selectByPrimaryId(@Param(value = "deviceId") Long deviceId);
    //插入
    int insert(TStdDeviceAttr tStdDeviceAttr);
    //删除
    int deleteByPrimaryId(@Param(value = "deviceId") Long deviceId);
    //修改
    int update(TStdDeviceAttr tStdDeviceAttr);
    //分页查询
    List<TStdDeviceAttr> selectByPage(TStdDeviceAttr tStdDeviceAttr);

    List<TStdDeviceAttr> select(@Param(value = "deviceId") Long deviceId,
                            @Param(value = "deviceSubtype") Integer deviceSubtype,
                            @Param(value = "serial") String serial,
                            @Param(value = "manufacturer") String manufacturer,
                            @Param(value = "supplier") String supplier,
                            @Param(value = "productionDate") Date productionDate,
                            @Param(value = "openingDate") Date openingDate,
                            @Param(value = "disableDate") Date disableDate,
                            @Param(value = "lastMaintenance") Date lastMaintenance,
                            @Param(value = "maintenanceCycle") String maintenanceCycle,
                            @Param(value = "organization") String organization,
                            @Param(value = "department") String department,
                            @Param(value = "responsiblePerson") String responsiblePerson,
                            @Param(value = "latitude") String latitude,
                            @Param(value = "longitude") String longitude,
                            @Param(value = "remark") String remark,
                            @Param(value = "para1") String para1,
                            @Param(value = "para2") String para2,
                            @Param(value = "para3") String para3);
}
