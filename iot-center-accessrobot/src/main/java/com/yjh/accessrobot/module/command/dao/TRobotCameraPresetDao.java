package com.yjh.accessrobot.module.command.dao;

import java.util.List;
import java.util.Date;
import com.yjh.accessrobot.module.command.entity.TRobotCameraPreset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2021-03-15
 */
@Repository
public interface TRobotCameraPresetDao {

    int add(TRobotCameraPreset tRobotCameraPreset);
    int deleteByPrimaryId(@Param(value = "presetId") Long presetId);
    int update(TRobotCameraPreset tRobotCameraPreset);
    TRobotCameraPreset selectByPrimaryId(@Param(value = "presetId") Long presetId);
    List<TRobotCameraPreset> select(@Param(value = "presetId") Long presetId,
                                @Param(value = "presetNum") Integer presetNum,
                                @Param(value = "robotId") Long robotId,
                                @Param(value = "robotCode") String robotCode,
                                @Param(value = "presetName") String presetName,
                                @Param(value = "creatorTime") Date creatorTime,
                                @Param(value = "cameraType") Integer cameraType);
    List<TRobotCameraPreset> selectByPage(@Param(value = "presetNum") Integer presetNum,
                                    @Param(value = "presetName") String presetName);

    int batchAdd(List<TRobotCameraPreset> list);
    int batchDelete(List<String> list);
    TRobotCameraPreset selectByPresetNum(@Param(value = "presetNum") Integer presetNum);
}
