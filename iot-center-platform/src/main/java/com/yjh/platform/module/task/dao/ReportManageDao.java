package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.CheckPointType;
import com.yjh.platform.module.task.entity.TCruiseDataResultDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @date 2020/10/20 - 20:21
 */
@Repository
public interface ReportManageDao {
    List<TCruiseDataResultDetail> selectDetail(@Param(value = "startTime")Date startTime,
                                                 @Param(value = "endTime")Date endTime);
    String selectStationName();
    Integer selectMeteNum();
    Integer selectAbnormalNum();
    List<CheckPointType> selectMeteType();
    List<CheckPointType> selectMeteType2();
}
