package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.CheckPointType;
import com.yjh.platform.module.task.entity.TCruiseDataResultDetail;
import com.yjh.platform.module.task.entity.TaskVO;
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
    List<TCruiseDataResultDetail> selectDetail(@Param(value = "list") List<String> list,
                                               @Param(value = "startTime")Date startTime,
                                               @Param(value = "endTime")Date endTime);
    List<TCruiseDataResultDetail> selectTaskResult(@Param(value = "taskId")String taskId);
    String selectStationName();
    TaskVO selectTaskNameAndTime(@Param(value = "taskId")String taskId);
    int selectMeteNum(@Param(value = "list") List<String> list);
    int selectMeteNumByTask(@Param(value = "taskId")String taskId);
    int selectAbnormalNum(@Param(value = "list") List<String> list);
    int selectAbnormalNumByTask(@Param(value = "taskId")String taskId);
    List<CheckPointType> selectMeteType(@Param(value = "list") List<String> list);
    List<CheckPointType> selectMeteType2(@Param(value = "taskId")String taskId);
}
