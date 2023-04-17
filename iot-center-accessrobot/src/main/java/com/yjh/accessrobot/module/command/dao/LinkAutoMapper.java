package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/4/3
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface LinkAutoMapper {
    /**
     * 查询根节点
     * @return
     */
    Long selectRoot();

    /**
     * 查询站所信息
     * @return TStdRegion
     */
    TStdRegion selectTSRegionForStation();

    /**
     * 批量插入巡视点关联关系
     * @param list
     * @return
     */
    int batchInsertCruisePointInstance(List<TCruisePointInstance> list);
    /**
     * 查询未连接测点的巡视点
     * @param robotId
     * @return
     */
    List<TRobotInspection> selectInspectionNonLink(@Param(value = "robotId") Long robotId);

    /**
     *
     * @param Id 父Id
     * @param nameList 区域名称List
     * @return
     */
    List<TStdRegion> listTStdRegionByParentIdAndNameList(@Param("rootId")Long Id,@Param("nameList")Set<String> nameList);

    /**
     * 批量插入区域信息
     * @param tStdRegionList
     * @return
     */
    int batchInsertRegion(@Param("list") List<TStdRegion> tStdRegionList);

    /**
     * 通过所属区域和设备名称查询设备
     * @param upRegionId
     * @param deviceNameList
     * @return
     */
    List<TStdDevice> selectByDeviceNameEqual(@Param("upRegionId")Long upRegionId, @Param("deviceNameList") Set<String> deviceNameList);

    /**
     * 通过所属区域和区域名称查询区域
     * @param upRegionId
     * @param regionNameList
     * @return
     */
    List<TStdRegion> selectByRegionNameEqual(@Param("upRegionId")Long upRegionId, @Param("regionNameList") Set<String> regionNameList);

    /**
     * 通过所属设备和测点名称查询测点
     * @param upRegionId
     * @param regionNameList
     * @return
     */
    List<TStdDeviceMete> selectByMeteNameEqual(@Param("deviceId")Long deviceId, @Param("meteNameList") Set<String> meteNameList);

    /**
     * 批量插入设备
     * @param list
     * @return
     */
    int batchInsertDevice(@Param("list") List<TStdDevice> list);

    /**
     * 批量插入设备属性
     * @param list
     */
    void batchInsertDeviceAttr(@Param("list") List<TStdDeviceAttr> list);

    /**
     * 批量插入测点
     * @param record
     * @return
     */
    int insertDeviceMete(@Param("list")List<TStdDeviceMete> record);
}
