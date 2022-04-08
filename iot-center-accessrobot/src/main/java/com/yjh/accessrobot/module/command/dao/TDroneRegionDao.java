package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TDroneRegion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @since 2021-01-28
 */
@Repository
public interface TDroneRegionDao {

    int insert(TDroneRegion tDroneRegion);
    int deleteByPrimaryId(@Param(value = "regionId") String regionId);
    int update(TDroneRegion tDroneRegion);
    TDroneRegion selectByPrimaryId(@Param(value = "regionId") String regionId);
    List<TDroneRegion> select(@Param(value = "regionId") String regionId,
                              @Param(value = "regionName") String regionName,
                              @Param(value = "sort") Integer sort,
                              @Param(value = "deviceType") Integer deviceType,
                              @Param(value = "upRegionId") String upRegionId,
                              @Param(value = "upRegionIds") String upRegionIds,
                              @Param(value = "regionType") Integer regionType,
                              @Param(value = "stationId") String stationId,
                              @Param(value = "state") Integer state,
                              @Param(value = "droneId") Long droneId,
                              @Param(value = "createTime") Date createTime);
    List<TDroneRegion> selectByPage(TDroneRegion tDroneRegion);

    int batchInsert(List<TDroneRegion> list);
    List<String> selectAllByDroneId(@Param(value = "droneId") Long droneId);
    int batchDelete(List<String> list);
    Map<String,Object> selectInspectionId(@Param(value = "inspectionId")Long inspectionId);
}
