package com.yjh.accesstcp.module.device.dao;



import com.yjh.accesstcp.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface SendToUpSystemDao {
    List<Map<String,Object>> selectDeviceModel();
    List<Map<String,Object>> selectRobotInfo();
    List<Map<String,Object>> selectCameraInfo();
    List<Map<String,Object>> selectTaskInfo();
    List<MaintenanceModel> selectMaintenanceInfo();
    List<Long> selectInstanceId(@Param(value = "taskId") String taskId);
}
