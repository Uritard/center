package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-24
 */
@Repository
public interface TStdDeviceAttrDao {

    int insert(TStdDeviceAttr tStdDeviceAttr);
    int deleteByPrimaryId(@Param(value = "deviceId") Long deviceId);
    int update(TStdDeviceAttr tStdDeviceAttr);
    TStdDeviceAttr selectByPrimaryId(@Param(value = "deviceId") Long deviceId);
    List<TStdDeviceAttr> select(@Param(value = "deviceId") Long deviceId,
                                @Param(value = "deviceModel") Integer deviceModel,
                                @Param(value = "pmsType") String pmsType,
                                @Param(value = "pmsId") String pmsId,
                                @Param(value = "manufacturer") String manufacturer,
                                @Param(value = "productionDate") Date productionDate,
                                @Param(value = "openingDate") Date openingDate,
                                @Param(value = "disableDate") Date disableDate,
                                @Param(value = "lastMaintenance") Date lastMaintenance,
                                @Param(value = "maintenanceCount") String maintenanceCount,
                                @Param(value = "organization") String organization,
                                @Param(value = "department") String department,
                                @Param(value = "responsiblePerson") String responsiblePerson,
                                @Param(value = "latitude") String latitude,
                                @Param(value = "longitude") String longitude,
                                @Param(value = "ip") String ip,
                                @Param(value = "port") Integer port,
                                @Param(value = "para1") String para1,
                                @Param(value = "para2") String para2,
                                @Param(value = "para3") String para3);
    List<TStdDeviceAttr> selectByPage(TStdDeviceAttr tStdDeviceAttr);

    int batchInsert(List<TStdDeviceAttr> list);
}
