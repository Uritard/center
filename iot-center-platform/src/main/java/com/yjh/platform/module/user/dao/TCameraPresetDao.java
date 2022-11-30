package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TCameraPresetExpand;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    String selectPreImgByCruiseId(@Param(value = "instanceId") Long instanceId);

    TCameraPreset selectLastOne();

    List<TCameraPreset> selectAll();

    List<Long> selectInstanceIdList(@Param(value = "cruiseId") Long cruiseId);

    int deleteInstance(@Param(value = "list") List<Long> list);

    int deletePlanInstance(@Param(value = "list") List<Long> list);

    int deletePointInstance(@Param(value = "list") List<Long> list);

    List<Long> selectForThisPreset(@Param(value = "cameraId") Long cameraId);

    List<Long> selectCameraIdList();

    List<Long> selectCameraHavePreset(@Param(value = "list") List<Long> list);

    TCameraPreset selectKeepWatch(@Param(value = "cameraId") Long cameraId);

    TCameraPreset selectKeepWatchTask(@Param(value = "cameraId") Long cameraId,
                                      @Param(value = "presetId") Long presetId,
                                      @Param(value = "isKeepWatch") Integer isKeepWatch,
                                      @Param(value = "isKeepWatchTask") Integer isKeepWatchTask,
                                      @Param(value = "isSecondKeepWatchTask")Integer isSecondKeepWatchTask,
                                      @Param(value = "selfPreset") Long selfPreset
    );

    List<Map<String, Object>> selectCameraBySilent();

    String selectPMSByCameraId(@Param(value = "cameraId") Long cameraId);

    Map<String, Object> selectInstanceInfo(@Param(value = "presetId") Long presetId);

    String selectAlarmLevel(@Param(value = "colName") String colName, @Param(value = "dictNote") String dictNote);

    int selectSilentByCameraIdAndPresetId(@Param(value = "cameraId") String cameraId, @Param(value = "presetId") String presetId);

    List<Map<String, Object>> selectCameraBySecondSilent();

  int selectCameraPresetInTask(@Param(value = "presetId") String presetId );

}
