package com.yjh.accesstcp.module.device.dao;



import com.yjh.accesstcp.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface SendToUpSystemDao {
    List<Map<String,Object>> selectDeviceModel(@Param(value = "standardPoints") Boolean standardPoints,
                                               @Param(value = "edgeCode") String edgeCode,
                                               @Param(value = "presetRealImgPath") String presetRealImgPath,
                                               @Param(value = "presetImgPath") String presetImgPath);
    List<Map<String,Object>> selectRobotInfo();
    List<Map<String,Object>> selectDroneInfo();
    List<Map<String,Object>> selectVoiceInfo();
    List<Map<String,Object>> selectCameraInfo();
    List<Map<String,Object>> selectTaskInfo();
    List<MaintenanceModel> selectMaintenanceInfo();
    List<Long> selectInstanceId(@Param(value = "taskId") String taskId);
    HashMap<String,Object> countTask(@Param(value = "startTime")String startTime,
                                      @Param(value = "endTime")String endTime );
    HashMap<String,Object> countWarnCheck(@Param(value = "startTime")String startTime,
                                      @Param(value = "endTime")String endTime );
    HashMap<String,Object> countWarnAccuracy(@Param(value = "startTime")String startTime,
                                      @Param(value = "endTime")String endTime );
    HashMap<String,Object> countInstanceLoss(@Param(value = "startTime")String startTime,
                                      @Param(value = "endTime")String endTime );
    HashMap<String,Object> countResultCheck(@Param(value = "startTime")String startTime,
                                      @Param(value = "endTime")String endTime );

    List<DeviceStatisticsInfo>selectRobot();
    List<DeviceStatisticsInfo>selectCamera();
    List<DeviceStatisticsInfo>selectDrone();
    String selectMapPath();

    List<String> selectMapPathAll();

    List<Map<String, Object>> selectOnlinePatrolDevice();

    List<TCruisePointInstanceNameDetail> selectForTask(@Param(value = "list")List<Long> list);
    List<String> selectRobotCodeForUpperTask(@Param(value = "list") List<Long> list);
    List<Long> selectRobotTaskInstanceId(@Param(value = "list") List<Long> list,
                                         @Param(value = "robotCode") String robotCode);
    String selectEdgeCodeOfRobotOrDrone(String robotCode);

    List<String> selectForTaskInstanceId(@Param(value = "devicePointId") String devicePointId);

    String selectIsRobotDevice(@Param(value = "list") List<String> list);
    String selectIsDownSystem(@Param(value = "list") List<String> list);

    List<String> selectInstanceIdsByRegionOrDevice(Map<String, String> idMap);

    List<String> selectInstanceIdsByComponent(@Param(value = "list") List<DeviceModel> deviceModels);

    HashMap<String, Object> countLabelAccuracy(@Param(value = "startTime") String startTime,
                                               @Param(value = "endTime") String endTime);

    List<Map<String, Object>> selectMaintenanceModel(@Param(value = "stationName") String stationName,
                                                     @Param(value = "stationCode") String stationCode);

    List<Map<String, Object>> selectLinkageModel();

    List<TCruisePointInstanceMeteDetail> selectAlarmThresholdModel();

}
