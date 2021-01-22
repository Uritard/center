package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.CameraInfo;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraInfoByDict;
import com.yjh.platform.module.user.entity.TCamreaPresetTree;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface TCameraInfoDao {

    int insert(TCameraInfo tCameraInfo);
    int deleteByPrimaryId(@Param(value = "cameraId") Long cameraId);
    int update(TCameraInfo tCameraInfo);
    int deleteSelectedCamera(@Param(value = "list") List<String> list);
    TCameraInfoByDict selectByPrimaryId(@Param(value = "cameraId") Long cameraId);
    List<TCameraInfoByDict> selectByRegionId(@Param(value = "regionId") Long regionId);
    List<TCameraInfoByDict> selectByCameraName(@Param(value = "cameraName") String cameraName);
    List<TCameraInfoByDict> select(@Param(value = "cameraId") Long cameraId,
                             @Param(value = "cameraName") String cameraName,
                             @Param(value = "cameraModel") Integer cameraModel,
                            @Param(value = "pmsId") String pmsId,
                             @Param(value = "aliasName") String aliasName,
                             @Param(value = "recordId") String recordId,
                             @Param(value = "upRegionId") Long upRegionId,
                             @Param(value = "channelNum") Integer channelNum,
                             @Param(value = "smsId") Integer smsId,
                             @Param(value = "rmsId") Integer rmsId,
                             @Param(value = "monitorId")String monitorId,
                             @Param(value = "vendorId") Integer vendorId,
                             @Param(value = "streamType") Integer streamType,
                             @Param(value = "protocolType") Integer protocolType,
                             @Param(value = "cameraIp") String cameraIp,
                             @Param(value = "url") String url,
                             @Param(value = "port") Integer port,
                             @Param(value = "cameraType") Integer cameraType,
                            @Param(value = "isControl") Integer isControl,
                             @Param(value = "latitude") String latitude,
                             @Param(value = "longitude") String longitude,
                             @Param(value = "address") String address,
                             @Param(value = "unit") String unit);
    List<TCameraInfoByDict> selectByPage(@Param(value = "aliasName") String aliasName,
                                         @Param(value = "unit") String unit,
                                         @Param(value = "address") String address,
                                         @Param(value = "cameraVendor") String cameraVendor,
                                         @Param(value = "cameraModel") Integer cameraModel,
                                         @Param(value = "cameraName") String cameraName,
                                         @Param(value = "regionIdList") List<Long> regionIdList);

    List<AreaInfo> selectCameraTreeDevice();
    List<AreaInfo> selectCameraPresetTree();

    List<CameraInfo> selectCameraByTaskId(@Param(value = "taskId")Long taskId);
    List<TCamreaPresetTree> selectCameraId();
    List<TCamreaPresetTree> batchSelectPreset();

    TCameraInfo selectCamera(@Param(value = "cameraId") Long cameraId);
    Long selectCameraIdByPmsId(@Param(value = "pmsId")String pmsId);
}
