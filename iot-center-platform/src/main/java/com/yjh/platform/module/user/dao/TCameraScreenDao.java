package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.user.entity.AreaInfoDetail;
import com.yjh.platform.module.user.entity.TCameraScreen;
import com.yjh.platform.module.user.entity.TCameraScreenDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

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
    List<AreaInfoDetail> selectCameraTreeDevice();
    List<Long> selectRecordId();
}
