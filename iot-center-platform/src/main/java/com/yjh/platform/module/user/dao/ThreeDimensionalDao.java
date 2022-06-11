package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.task.entity.StatisticalTools;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.entity.TCruisePlanCountByPage;
import com.yjh.platform.module.user.entity.AlarmAndMeteInfo;
import com.yjh.platform.module.user.entity.CameraUnionDevice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/8/10
 */
@Repository
public interface ThreeDimensionalDao {
    /**
     *通过设备名称查找对应的模型名称
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @return String
     */
    String searchAndLocationFocus(@Param("deviceName")String deviceName,
                                  @Param("deviceType")Integer deviceType);
    /**
     * 通过模型名称查找相关的点位信息以及告警信息
     * @param modelName 模型名称
     * @return AlarmAndMeteInfo
     */
    List<AlarmAndMeteInfo> viewPointInformation(@Param("modelName") String modelName);
    /**
     * 通过模型名称查找对应的设备id
     * @param modelName 模型名称
     * @return Long
     */
    Long selectDeviceIdByModelName(@Param("modelName") String modelName);
    /**
     * 通过模型名称查找相关信息
     * @param modelName 模型名称
     * @return Long
     */
    Map<String,Object> selectDeviceInfoByModelName(@Param("modelName") String modelName);
    /**
     * 通过设备id查找设备下对应的摄像机信息
     * @param deviceId 设备id
     * @return CameraUnionDevice
     */
    List<CameraUnionDevice> selectCameraByDeviceId(@Param("deviceId")Long deviceId);
    /**
     * 通过相机id查找设备下对应的摄像机信息
     * @param cameraId 相机id
     * @return CameraUnionDevice
     */
    List<CameraUnionDevice> selectCameraByCameraId(@Param("cameraId")Long cameraId);
    /**
     * 告警信息统计
     * @return StatisticalTools
     */
    List<StatisticalTools> statisticsAlarmInfo();
    /**
     * 查询所有模型名称
     * @return List<String>
     */
    List<String> selectAllModelName();
    /**
     * 查询所有名称
     * @return List<String>
     */
    List<String> selectAllName();

    /**
     * 根据 3D 模版查询巡检预案
     * @param cruisePlanMap 查询条件
     * @return List<TCruisePlanCountByPage>
     */
    List<TCruisePlanCountByPage> selectByPlanPage(Map<String, Object> cruisePlanMap);
}
