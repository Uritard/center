package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TCameraPresetExpand;
import org.apache.ibatis.annotations.Param;

import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author yc
 * @since 2020-08-18
 */
@Repository
public interface TCameraPresetDao {

    int insert(TCameraPreset tCameraPreset);
    int deleteByPrimaryId(@Param(value = "presetId") Long presetId);
    int deleteSelectedPreset(@Param(value = "list") List<String> list);
    int update(TCameraPreset tCameraPreset);
    TCameraPreset selectByPrimaryId(@Param(value = "presetId") Long presetId);
    List<TCameraPreset> selectByCameraId(@Param(value = "cameraId") Long cameraId);
    List<TCameraPreset> selectByPresetName(@Param(value = "presetName") String presetName);
    List<TCameraPreset> select(@Param(value = "presetId") Long presetId,
                               @Param(value = "cameraId") Long cameraId,
                               @Param(value = "presetNum") Integer presetNum,
                               @Param(value = "presetName") String presetName,
                               @Param(value = "creatorUser") String creatorUser,
                               @Param(value = "creatorTime") Date creatorTime,
                               @Param(value = "isUse") Integer isUse,
                               @Param(value = "presetImg") String presetImg,
                               @Param(value = "inspectionPostion") Integer inspectionPostion,
                               @Param(value = "collectStatus") Integer collectStatus,
                               @Param(value = "calibrationStatus") Integer calibrationStatus);
    List<TCameraPresetExpand> selectByPage(TCameraPreset tCameraPreset);

    int batchInsert(List<TCameraPreset> list);
    String selectPreImgByCruiseId(@Param(value = "instanceId")Long instanceId);

    TCameraPreset selectLastOne();

    List<TCameraPreset>selectAll();
    List<Long> selectInstanceIdList(@Param(value = "cruiseId")Long cruiseId);
    int deleteInstance(@Param(value = "list")List<Long> list);
    int deletePlanInstance(@Param(value = "list")List<Long> list);
    int deletePointInstance(@Param(value = "list")List<Long> list);
    List<Long>selectForThisPreset(@Param(value = "cameraId")Long cameraId);
    List<Long>selectCameraIdList();
    List<Long>selectCameraHavePreset(@Param(value = "list")List<Long> list);
}
