package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.AreaInfoDetail;
import com.yjh.platform.module.user.entity.AreaInfoOfMonitorDevice;
import com.yjh.platform.module.user.entity.TCameraScreen;
import com.yjh.platform.module.user.entity.TCameraScreenDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2020-10-16
 */
@Repository
public interface TCameraScreenDao {

    int add(TCameraScreen tCameraScreen);
    int deleteByPrimaryId(@Param(value = "userId") Long userId);
    int update(TCameraScreen tCameraScreen);
    TCameraScreenDetail selectByPrimaryId(@Param(value = "userId") Long userId);
    List<TCameraScreen> select(@Param(value = "userId") Long userId,
                                @Param(value = "screenNum") String screenNum,
                                @Param(value = "cameraIds") String cameraIds,
                                @Param(value = "createTime") Date createTime);
    List<TCameraScreen> selectByPage(TCameraScreen tCameraScreen);

    int batchAdd(List<TCameraScreen> list);
    int batchDelete(List<String> list);
    List<AreaInfoDetail> selectCameraTreeDevice(@Param(value = "cameraName") String cameraName);
    List<AreaInfoDetail> infraredCameraStateTree(@Param(value = "cameraName") String cameraName);
    List<Long> selectRecordId();
    List<Long> selectRecordId2();
    List<AreaInfoDetail> selectCameraTreeWithRobot(@Param(value = "cameraName") String cameraName,
                                                   @Param(value = "robotFlag") String robotFlag,
                                                   @Param(value = "userId")Long userId);

    List<AreaInfoOfMonitorDevice> selectRegionMonitorDevice();
    List<AreaInfoOfMonitorDevice> selectAllMonitorDevice();
}
