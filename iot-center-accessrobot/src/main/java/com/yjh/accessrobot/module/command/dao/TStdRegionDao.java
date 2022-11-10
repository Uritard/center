package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TStdRegion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    List<TStdRegion> selectByPage(TStdRegion tStdRegion);

    List<Long> selectDownId(@Param(value = "regionId") Long regionId);

    List<Long> selectDevice(@Param(value = "list") List<Long> list);

    List<TStdRegion> selectIsIn(TStdRegion tStdRegion);

    int updateByPrimaryKey(TStdRegion record);

    List<TStdRegion> selectAll();

    int deleteByOriginRegionIdAnRegionCode(@Param(value = "state") int state, @Param(value = "regionCode") String regionCode, @Param(value = "originIdList") Collection<Long> originIdList);

    void updateUpRegionId(TStdRegion stdRegion);

    List<TStdRegion> selectByRegionCodeAndState(@Param(value = "regionCode") String regionCode, @Param(value = "state") Integer state);

}
