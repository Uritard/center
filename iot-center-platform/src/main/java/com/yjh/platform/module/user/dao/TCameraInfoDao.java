package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Map;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraPreset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface TCameraInfoDao {

    int insert(TCameraInfo tCameraInfo);
    int deleteByPrimaryId(@Param(value = "cameraId") Long cameraId);
    int deleteSelectedCamera(@Param(value = "cameraIds") String[] cameraIds);
    int update(TCameraInfo tCameraInfo);
    TCameraInfo selectByPrimaryId(@Param(value = "cameraId") Long cameraId);
    List<TCameraInfo> selectByRegionId(@Param(value = "regionId") Long regionId);
    List<TCameraInfo> selectByCameraName(@Param(value = "cameraName") String cameraName);
    List<TCameraInfo> select(@Param(value = "cameraId") Long cameraId,
                             @Param(value = "cameraName") String cameraName,
                             @Param(value = "aliasName") String aliasName,
                             @Param(value = "recordId") String recordId,
                             @Param(value = "upRegionId") Long upRegionId,
                             @Param(value = "channelNum") Integer channelNum,
                             @Param(value = "smsId") Integer smsId,
                             @Param(value = "rmsId") Integer rmsId,
                             @Param(value = "factoryName") String factoryName,
                             @Param(value = "streamType") Integer streamType,
                             @Param(value = "protocolType") Integer protocolType,
                             @Param(value = "url") String url,
                             @Param(value = "port") Integer port,
                             @Param(value = "cameraType") Integer cameraType,
                             @Param(value = "isControl") Integer isControl);
    List<TCameraInfo> selectByPage(TCameraInfo tCameraInfo);

    List<AreaInfo> selectCameraTreeDevice();
}
