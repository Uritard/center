package com.yjh.accessvideo.module.control.dao;

import com.yjh.accessvideo.module.control.entity.CameraConInfo;
import com.yjh.accessvideo.module.control.entity.CameraStatusInfo;
import com.yjh.accessvideo.module.control.entity.RecorderConInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-08-20
 */
@Repository
public interface CameraConDao {

    CameraConInfo selectConInfo(@Param(value = "cameraId") Long cameraId, @Param(value = "presetId") Long presetId);

    List<CameraConInfo> batchSelectConInfo(@Param("list") List<Long> list);

    List<RecorderConInfo> SelectRecords();

    List<CameraStatusInfo> cameraInfoByNVR(@Param("recordId") Long recordId);

}
