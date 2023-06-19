package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.user.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
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
    List<TCameraInfo> select(@Param(value = "cameraId") Long cameraId,
        @Param(value = "cameraName") String cameraName,
        @Param(value = "cameraModel") Integer cameraModel,
        @Param(value = "pmsId") String pmsId,
        @Param(value = "aliasName") String aliasName,
        @Param(value = "recordId") String recordId,
        @Param(value = "upRegionId") Long upRegionId,
        @Param(value = "streamType") Integer streamType,
        @Param(value = "protocolType") Integer protocolType,
        @Param(value = "cameraIp") String cameraIp,
        @Param(value = "url") String url,
        @Param(value = "cameraType") Integer cameraType,
        @Param(value = "isControl") Integer isControl);
    List<TCameraInfoByDict> selectByPage(@Param(value = "cameraType") Integer cameraType,
                                        @Param(value = "aliasName") String aliasName,
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



    List<Long> selectCameraAll();
    List<Long> selectCameraByRecord(Long recordId);
    List<Long>selectHavePreset(@Param(value = "cameraId") Long cameraId);
    List<String> selectAllPMSId();
    String selectPmsIdById(@Param(value = "cameraId") Long cameraId);
    //相机ID查询视频诊断监测点ID
    String selectMonitorId(@Param(value = "cameraId")Long cameraId);
    HashMap<String,Object> selectByCameraId(@Param(value = "cameraId")Long cameraId);
    HashMap<String,Object> selectByRobotIdByLight(@Param(value = "robotId")Long robotId);
    HashMap<String,Object> selectByRobotIdByInferad(@Param(value = "robotId")Long robotId);

    int selectCount();

    List<AreaInfoDetail> selectCameraTreeRegion();
    List<AreaInfoDetail> selectCameraTreeWithRobotNew(@Param(value = "cameraName") String cameraName,
                                                      @Param(value = "robotFlag") String robotFlag,
                                                      @Param(value = "userId")Long userId,
                                                      @Param(value = "upRegionId")Long upRegionId);
    List<TCameraInfo> selectCameraByName(@Param(value = "cameraName") String cameraName,
                                  @Param(value = "robotFlag") String robotFlag,
                                  @Param(value = "userId")Long userId);

    List<Long> selectRegionByCameraList(@Param(value = "list")List<TCameraInfo> list);

    List<AreaInfoDetail> selectCameraTreeByName(@Param(value = "cameraList")List<TCameraInfo> cameraList,
                                                @Param(value = "regionList")List<Long> regionList);

    int batchInsert(List<TCameraInfo> list);
}
