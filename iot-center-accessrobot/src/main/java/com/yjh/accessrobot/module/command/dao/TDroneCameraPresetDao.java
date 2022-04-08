package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TDroneCameraPreset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2021-03-15
 */
@Repository
public interface TDroneCameraPresetDao {

    int add(TDroneCameraPreset tDroneCameraPreset);
    int deleteByPrimaryId(@Param(value = "presetId") Long presetId);
    int update(TDroneCameraPreset tDroneCameraPreset);
    TDroneCameraPreset selectByPrimaryId(@Param(value = "presetId") Long presetId);
    List<TDroneCameraPreset> select(@Param(value = "presetId") Long presetId,
                                @Param(value = "presetNum") Integer presetNum,
                                @Param(value = "droneId") Long droneId,
                                @Param(value = "droneCode") String droneCode,
                                @Param(value = "presetName") String presetName,
                                @Param(value = "creatorTime") Date creatorTime,
                                @Param(value = "cameraType") Integer cameraType);
    List<TDroneCameraPreset> selectByPage(@Param(value = "presetNum") Integer presetNum,
                                    @Param(value = "presetName") String presetName,
                                    @Param(value = "droneId") Long droneId);

    int batchAdd(List<TDroneCameraPreset> list);
    int batchDelete(List<String> list);
    TDroneCameraPreset selectByPresetNum(@Param(value = "presetNum") Integer presetNum);
}
