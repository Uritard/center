package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.task.entity.DeviceBaseReport;
import com.yjh.platform.module.task.entity.DeviceMeteBaseReport;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tt
 * @since 2020-07-27
 */
@Repository
public interface TStdDeviceDao {

    int add(TStdDevice tStdDevice);
    int deleteByPrimaryId(@Param(value = "deviceId") Long deviceId);
    int deleteByUnionKeys(@Param(value = "deviceId")Long deviceId);
    int update(TStdDevice tStdDevice);
    int updateByTask(@Param(value = "taskId")String taskId);
    List<TStdDevice> selectByPrimaryId(@Param(value = "deviceId") Long deviceId);
    List<TStdDevice> selectListByPrimaryId(@Param(value = "deviceId") Long deviceId);
    List<TStdDevice> select(@Param(value = "deviceId") Long deviceId,
                                @Param(value = "deviceCode") String deviceCode,
                                @Param(value = "deviceName") String deviceName,
                                @Param(value = "aliasName") String aliasName,
                                @Param(value = "deviceType") Integer deviceType,
                                @Param(value = "positionType") String positionType,
                                @Param(value = "modelId") Long modelId,
                                @Param(value = "regionPath") String regionPath,
                                @Param(value = "upRegionId") Long upRegionId,
                                @Param(value = "upRegionName") String upRegionName,
                                @Param(value = "customType") Integer customType,
                                @Param(value = "status") Integer status,
                                @Param(value = "updateTime") Date updateTime,
                                @Param(value = "createTime") Date createTime);
    List<TStdDevice> selectByPage(TStdDevice tStdDevice);
    TStdDevice selectByUnionKeys(@Param(value = "deviceId")Long deviceId);
    List<TStdDeviceDetail> selectAll(@Param(value = "deviceId") Long deviceId,
                            @Param(value = "deviceCode") String deviceCode,
                            @Param(value = "deviceName") String deviceName,
                            @Param(value = "aliasName") String aliasName,
                            @Param(value = "deviceType") Integer deviceType,
                            @Param(value = "positionType") String positionType,
                            @Param(value = "modelId") Long modelId,
                            @Param(value = "regionPath") String regionPath,
                            @Param(value = "upRegionId") Long upRegionId,
                            @Param(value = "upRegionName") String upRegionName,
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
    List<TStdDeviceDetail> selectByPageAll(@Param(value = "deviceName") String deviceName,
                                           @Param(value = "deviceType") Integer deviceType,
                                           @Param(value = "realCode") String realCode,
                                           @Param(value = "upRegionId") Long upRegionId,
                                           @Param(value = "upRegionIds") List<Long> upRegionIds,
                                           @Param(value = "list") List<Long> list,
                                           @Param(value = "pageSize") Integer pageSize);
    List<Long> selectForPage(TStdDeviceDetail tStdDeviceDetail);
    List<Long> findPresetByCameraIdAndMeteId(@Param("cameraId")String cameraId,@Param("meteId")Long Id);

    TStdRegion selectRegionById(@Param(value = "deviceId") Long deviceId);
    List<AreaInfo> selectDevTreeCustom();
    List<AreaInfo> selectDevTreeDevice(@Param(value = "regionId") Long regionId);
    List<AreaInfo> selectDevTreeRegion();
    List<AreaInfo> selectAllTreeCustom();
    List<AreaInfo> selectAllTreeDevice();
    List<AreaInfo> selectRobotTree();
    List<AreaInfo> selectRobotInspectionTree();
    List<AreaInfo> selectAllMeteTree();
    List<AreaInfo> selectAllMeteCruiseTree(@Param(value = "deviceType") String deviceType,
                                           @Param(value = "analyseType") String analyseType,
                                           @Param(value = "edgeCode") String edgeCode);
    List<AreaInfo> selectRobotMeteCruiseTree(@Param(value = "deviceType") String deviceType,
                                           @Param(value = "analyseType") String analyseType);
    List<AreaInfo> selectCameraMeteCruiseTree(@Param(value = "deviceType") String deviceType,
                                           @Param(value = "analyseType") String analyseType);
    List<AreaInfo> selectDevTaskTree(@Param(value = "taskId") String taskId);
    List<String> selectByModelId(@Param(value = "modelId") Long modelId);

    int updateModelIdByDevCus(@Param(value = "deviceId")Long deviceId,
                              @Param(value = "modelId")Long modelId);

    int batchDelete(@Param(value = "list")List<String> list);
    String selectCustomTypeByDict();
    List<CustomInfo> selectCustomInfoByDict();

    List<AreaInfo> selectAllRegion();

    //查询巡视结果分析报表所需基本设备信息
    DeviceBaseReport selectDeviceBase(@Param(value = "deviceMeteId")Long deviceMete);
    //查询巡视结果分析表所需基本测点信息
    DeviceMeteBaseReport selectDeviceMeteBase(@Param(value = "deviceMeteId")Long deviceMeteId);

    //根据UpRegion查询设备ID
    List<Long> selectDeviceIdsByRegion(List<Long> regionIds);

    List<Long> selectDeviceIdListByRegion(List<Long> list);

    String defaultPart();

    List<AreaInfo> selectDeviceByRegionId(@Param(value = "upRegionId")Long upRegionId);
    List<AreaInfo> selectAllByRegionId(@Param(value = "upRegionId")Long upRegionId);
    List<AreaInfo> selectCameraByRegionId(@Param(value = "upRegionId")Long upRegionId);
    List<AreaInfo> selectRobotByRegionId(@Param(value = "upRegionId")Long upRegionId);
    List<AreaInfo> selectCustomByRegionId(@Param(value = "deviceId")Long deviceId);
    List<AreaInfo> selectDeviceMeteByDeviceAndCustom(@Param(value = "deviceId")Long deviceId,
                                                     @Param(value = "deviceType")String deviceType,
                                                     @Param(value = "analyseType")String analyseType);
    List<AreaInfo> selectCruisePointByDeviceMeteId(@Param(value = "deviceMeteId")Long deviceMeteId);
    List<TCruisePointInstance> selectAllMeteCruiseTreeByName(@Param(value = "name")String name,
                                                             @Param(value = "deviceType")String deviceType);
    List<TCruisePointInstance> selectDevTreeDeviceByName(@Param(value = "name")String name);
    List<TCruisePointInstance> selectCameraTreeDeviceByName(@Param(value = "name")String name);
    List<Long> selectRegionByDeviceList(@Param(value = "list")List<TCruisePointInstance> list);
    List<Long> selectUpIdByRegionList(@Param(value = "list")List<Long> list);
    List<AreaInfo> selectAllMeteCruiseTreeByNameTree(@Param(value = "list")List<TCruisePointInstance> list,
                                                                 @Param(value = "regionList")List<Long> regionList);
    List<AreaInfo> selectDevTreeDeviceByNameTree(@Param(value = "list")List<TCruisePointInstance> list,
                                                                 @Param(value = "regionList")List<Long> regionList);
    TCruisePointInstanceDetail selectDevTreeDeviceByInstacneId(@Param(value = "instanceId")Long instanceId);

    List<Map<String,Long>> selectFirstPresetIdByCameraIdList(@Param("list")List<String> cameraIdList);

    List<AreaInfo> selectRegTreeByRegionList(@Param(value = "list") Set<Long> list);

}
