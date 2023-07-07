package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.SynthesisTreeAreaInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.user.entity.TCameraInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tt
 * @since 2020-07-27
 */
@Repository
public interface TStdDeviceTreeDao {

    /**
     * 根据id查询设备
     *
     * @param upRegionId 查询id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectDeviceByRegionId(@Param(value = "upRegionId") Long upRegionId);

    /**
     * 根据id查询所有(巡视设备和被巡视设备)
     *
     * @param upRegionId 查询id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectAllByRegionId(@Param(value = "upRegionId") Long upRegionId);

    /**
     * 根据id查询所有巡视设备
     *
     * @param upRegionId 查询id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> getRegionMonitorDevice(@Param(value = "upRegionId") Long upRegionId);

    /**
     * 查询设备树基本信息
     *
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectSynthesisTreeRegion();

    /**
     * 根据条件查询相机、机器人
     *
     * @param robotFlag  是否机器人 为空：不是
     * @param userId     用户id
     * @param upRegionId 查询id
     * @param cameraType 相机类型
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectCameraOrRobot(@Param(value = "robotFlag") String robotFlag, @Param(value = "userId") Long userId,
                                                    @Param(value = "upRegionId") Long upRegionId, @Param(value = "cameraType") Integer cameraType);

    /**
     * 根据设备id查询部位信息
     *
     * @param deviceId 设备id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectCustomByRegionId(@Param(value = "deviceId") Long deviceId);

    /**
     * 根据测点id查询关联的相机预置位id/机器人测点id - 巡视点
     *
     * @param deviceMeteId 测点id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectCruisePointByDeviceMeteId(@Param(value = "deviceMeteId") Long deviceMeteId);

    /**
     * 根据设备id查询测点信息
     *
     * @param deviceId    设备id
     * @param meteType    识别类型
     * @param analyseType 算法分析类型
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectDeviceMeteByDeviceAndCustom(@Param(value = "deviceId") Long deviceId,
                                                                  @Param(value = "meteType") String meteType,
                                                                  @Param(value = "analyseType") String analyseType);

    /**
     * 根据识别类型和名称查询巡视点信息
     *
     * @param name     名称
     * @param meteType 识别类型
     * @return List<TCruisePointInstance>
     */
    List<TCruisePointInstance> selectAllMeteCruiseTreeByName(@Param(value = "name") String name,
                                                             @Param(value = "meteType") String meteType);

    /**
     * 根据区域id和设备id查询巡视点信息
     *
     * @param list       设备id
     * @param regionList 区域id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectAllMeteCruiseTreeByNameTree(@Param(value = "list") List<TCruisePointInstance> list,
                                                                  @Param(value = "regionList") List<Long> regionList);

    /**
     * 根据名称模糊查询设备id
     *
     * @param name 名称
     * @return List<TCruisePointInstance>
     */
    List<TCruisePointInstance> selectDevTreeDeviceByName(@Param(value = "name") String name);

    /**
     * 根据区域id和设备id查询巡视点信息
     *
     * @param list       设备id
     * @param regionList 区域id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectDevTreeDeviceByNameTree(@Param(value = "list") List<TCruisePointInstance> list,
                                                              @Param(value = "regionList") List<Long> regionList);

    /**
     * 根据名称模糊查询巡视设备id(相机、机器人、声纹)
     *
     * @param name 名称
     * @return List<TCameraInfo>
     */
    List<TCameraInfo> selectAllPatrolDeviceByName(@Param(value = "name") String name);

    /**
     * 根据区域id和设备id查询巡视点信息
     *
     * @param cameraList 巡视设备id
     * @param regionList 区域id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectAllPatrolDeviceTreeByName(@Param(value = "cameraList") List<TCameraInfo> cameraList,
                                                                @Param(value = "regionList") List<Long> regionList);

    /**
     * 根据区域id和设备id查询巡视点信息
     *
     * @param cameraList 相机id、机器人id
     * @param regionList 区域id
     * @return List<SynthesisTreeAreaInfo>
     */
    List<SynthesisTreeAreaInfo> selectCameraTreeByName(@Param(value = "cameraList") List<TCameraInfo> cameraList,
                                                       @Param(value = "regionList") List<Long> regionList);

}
