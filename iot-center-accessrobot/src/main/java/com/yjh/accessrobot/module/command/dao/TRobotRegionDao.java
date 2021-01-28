package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TRobotRegion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author YC
 * @since 2021-01-28
 */
@Repository
public interface TRobotRegionDao {

    int insert(TRobotRegion tRobotRegion);
    int deleteByPrimaryId(@Param(value = "regionId") String regionId);
    int update(TRobotRegion tRobotRegion);
    TRobotRegion selectByPrimaryId(@Param(value = "regionId") String regionId);
    List<TRobotRegion> select(@Param(value = "regionId") String regionId,
                              @Param(value = "regionName") String regionName,
                              @Param(value = "sort") Integer sort,
                              @Param(value = "deviceType") String deviceType,
                              @Param(value = "upRegionId") String upRegionId,
                              @Param(value = "upRegionIds") String upRegionIds,
                              @Param(value = "regionType") Integer regionType,
                              @Param(value = "stationId") String stationId,
                              @Param(value = "state") Integer state,
                              @Param(value = "createTime") Date createTime);
    List<TRobotRegion> selectByPage(TRobotRegion tRobotRegion);

    int batchInsert(List<TRobotRegion> list);
}
