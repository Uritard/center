package com.yjh.platform.module.control.dao;

import com.yjh.platform.module.control.entity.CameraConInfo;
import com.yjh.platform.module.control.entity.RecorderConInfo;
import com.yjh.platform.module.control.entity.RobotConInfo;
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

}
