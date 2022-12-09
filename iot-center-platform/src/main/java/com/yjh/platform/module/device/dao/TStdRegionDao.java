package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.entity.StationVoltageData;
import com.yjh.platform.module.device.entity.TStdRegion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author tt
 * @since 2020-07-27
 */
@Repository
public interface TStdRegionDao {

    int insert(TStdRegion tStdRegion);
    int deleteByPrimaryId(@Param(value = "regionId") Long regionId);
    int update(TStdRegion tStdRegion);
    TStdRegion selectByPrimaryId(@Param(value = "regionId") Long regionId);
    List<TStdRegion> select(@Param(value = "regionId") Long regionId,
                                @Param(value = "regionName") String regionName,
                                @Param(value = "sort") Integer sort,
                                @Param(value = "upRegionId") Long upRegionId,
                                @Param(value = "upRegionIds") String upRegionIds,
                                @Param(value = "regionCode") Integer regionCode,
                                @Param(value = "stationId") String stationId,
                                @Param(value = "state") Integer state,
                                @Param(value = "createTime") Date createTime);
    List<AreaInfoRegionCode> selectAreaTree();
   List<TStdRegion> selectByPage(TStdRegion tStdRegion);
    List<AreaInfoRegionCode> selectRegTreeByRegName(@Param(value = "regionName") String regionName);
    List<Long> selectDownId(@Param(value = "regionId") Long regionId);
    List<Long>selectDevice(@Param(value = "list") List<Long> list);
    List<TStdRegion> selectIsIn(TStdRegion tStdRegion);

    int updateByPrimaryKey(TStdRegion record);
    List<TStdRegion> selectAll();

    List<TStdRegion> selectByState( @Param(value = "state")Integer state);

    List<StationVoltageData> selectStationVoltageData();

    Integer countByRegionCode(@Param(value = "regionCode") String regionCode,
                              @Param(value = "upRegionId") Long upRegionId);

}
