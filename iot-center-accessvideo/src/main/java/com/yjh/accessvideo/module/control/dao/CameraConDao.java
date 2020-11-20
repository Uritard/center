package com.yjh.accessvideo.module.control.dao;

import com.yjh.accessvideo.module.control.entity.CameraConInfo;
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

    CameraConInfo selectStatusInfo(@Param(value = "cameraId") Long cameraId);

}
