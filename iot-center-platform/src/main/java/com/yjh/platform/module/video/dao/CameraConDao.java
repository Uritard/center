package com.yjh.platform.module.video.dao;

import com.yjh.platform.module.video.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tt
 * @since 2020-08-20
 */
@Repository
public interface CameraConDao {

    CameraConInfo selectConInfo(@Param(value = "cameraId") Long cameraId, @Param(value = "presetId") Long presetId);

    List<CameraConInfo> batchSelectConInfo(@Param("list") List<Long> list);

    RobotConInfo selectRobotConInfo(@Param("robotId") Long robotId);

    RecorderConInfo selectByRecordId(@Param(value = "recordId") Long recordId);

    RobotConInfo selectDroneConInfo(@Param("robotId") Long robotId);

    List<CameraStatusInfo> cameraInfoByNVR(@Param("recordId") Long recordId);

    List<CameraAreaInfo> selectCameraTree(@Param("cameraName") String cameraName);

}
