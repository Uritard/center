package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.entity.TStdRegion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-27
 */
@Repository
public interface TStdRegionDao {

    int insert(TStdRegion tStdRegion);
    int deleteByPrimaryId(@Param(value = "regionId") Long regionId);
    int update(Map<String,Object> map);
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

    int updateByMap(@Param("map") Map<String, Object> map);
    List<TStdRegion> selectByPage(TStdRegion tStdRegion);
    List<AreaInfoRegionCode> selectRegTreeByRegName(@Param(value = "regionName") String regionName);
    List<Long> selectRegionIds(Long upRegionId);
    List<Long> selectDownId(@Param(value = "regionId") Long regionId);
    int batchDelete(@Param(value = "list") List<Long> list);

    //根据上层ID查询子层区域ID
    List<Long> selectRegionByUpId(List<Long> upRegionIds);
    List<Long>selectDevice(@Param(value = "list") List<Long> list);
}
