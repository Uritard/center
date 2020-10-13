package com.yjh.platform.module.device.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.CustomInfo;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceDetail;
import com.yjh.platform.module.device.entity.TStdRegion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-27
 */
@Repository
public interface TStdDeviceDao {

    int add(TStdDevice tStdDevice);
    int deleteByPrimaryId(@Param(value = "deviceId") Long deviceId);
    int deleteByUnionKeys(@Param(value = "deviceId")Long deviceId,@Param("customId")String customId);
    int update(TStdDevice tStdDevice);
    TStdDevice selectByPrimaryId(@Param(value = "deviceId") Long deviceId);
    List<TStdDevice> select(@Param(value = "deviceId") Long deviceId,
                                @Param(value = "customId") String customId,
                                @Param(value = "deviceCode") String deviceCode,
                                @Param(value = "deviceName") String deviceName,
                                @Param(value = "aliasName") String aliasName,
                                @Param(value = "deviceType") Integer deviceType,
                                @Param(value = "positionType") String positionType,
                                @Param(value = "modelId") Long modelId,
                                @Param(value = "regionPath") String regionPath,
                                @Param(value = "upRegionId") Long upRegionId,
                                @Param(value = "upRegionName") String upRegionName,
                                @Param(value = "customName") String customName,
                                @Param(value = "customType") Integer customType,
                                @Param(value = "status") Integer status,
                                @Param(value = "updateTime") Date updateTime,
                                @Param(value = "createTime") Date createTime);
    List<TStdDevice> selectByPage(TStdDevice tStdDevice);
    TStdDevice selectByUnionKeys(@Param(value = "deviceId")Long deviceId,@Param("customId")String customId);
    List<TStdDeviceDetail> selectAll(@Param(value = "deviceId") Long deviceId,
                            @Param(value = "customId") String customId,
                            @Param(value = "deviceCode") String deviceCode,
                            @Param(value = "deviceName") String deviceName,
                            @Param(value = "aliasName") String aliasName,
                            @Param(value = "deviceType") Integer deviceType,
                            @Param(value = "positionType") String positionType,
                            @Param(value = "modelId") Long modelId,
                            @Param(value = "regionPath") String regionPath,
                            @Param(value = "upRegionId") Long upRegionId,
                            @Param(value = "upRegionName") String upRegionName,
                            @Param(value = "customName") String customName,
                            @Param(value = "customType") Integer customType,
                            @Param(value = "status") Integer status,
                            @Param(value = "updateTime") Date updateTime,
                            @Param(value = "createTime") Date createTime,
                            @Param(value = "deviceModel") Integer deviceModel,
                            @Param(value = "pmsType") String pmsType,
                            @Param(value = "pmsId") String pmsId,
                            @Param(value = "deviceVendor") String deviceVendor,
                            @Param(value = "productionDate") Date productionDate,
                            @Param(value = "usedTime") Date usedTime,
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
                            @Param(value = "voltageLevel") String voltageLevel,
                            @Param(value = "sequencePoint") String sequencePoint,
                            @Param(value = "realCode") String realCode,
                            @Param(value = "address") String address);
    TStdDeviceDetail selectByPrimaryIdAll(@Param(value = "deviceId") Long deviceId);
    List<TStdDeviceDetail> selectByPageAll(TStdDeviceDetail tStdDeviceDetail);


    TStdRegion selectRegionById(@Param(value = "deviceId") Long deviceId);
    List<AreaInfo> selectDevTreeCustom();
    List<AreaInfo> selectDevTreeDevice();
    List<AreaInfo> selectDevTreeRegion();
    List<AreaInfo> selectAllTreeCustom();
    List<AreaInfo> selectAllTreeDevice();
    List<AreaInfo> selectRobotTree();
    List<AreaInfo> selectRobotInspectionTree();
    List<AreaInfo> selectAllMeteTree();
    List<String> selectByModelId(@Param(value = "modelId") Long modelId);

    int updateModelIdByDevCus(@Param(value = "deviceId")Long deviceId,
                              @Param(value = "customId")Long customId,
                              @Param(value = "modelId")Long modelId);

    int batchDelete(@Param(value = "list")List<String> list);
    String selectCustomTypeByDict();
    List<CustomInfo> selectCustomInfoByDict();

    List<AreaInfo> selectAllRegion();
}
