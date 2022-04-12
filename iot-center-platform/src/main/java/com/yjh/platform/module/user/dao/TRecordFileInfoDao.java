package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TRecordFileInfo;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author hyh
 * @since 2022/4/11
 **/
public interface TRecordFileInfoDao {

    TRecordFileInfo selectByPrimaryId(@Param(value = "id") Long id);

    int deleteByPrimaryId(@Param(value = "id") Long id);

    List<TRecordFileInfo> selectByPage(@Param(value = "cameraId") Long cameraId,
                                        @Param(value = "startTime") String startTime,
                                        @Param(value = "endTime") String endTime);
}
