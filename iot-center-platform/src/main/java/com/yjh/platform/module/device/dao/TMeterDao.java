package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TMeter;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* @author yanhao
* @description 针对表【t_meter】的数据库操作Mapper
* @createDate 2022-10-21
* @Entity com.yjh.platform.module.device.entity.TMeter
*/
@Repository
public interface TMeterDao {

    List<TMeter> selectByUpRegionId(List<Long> list);

    List<TMeter> selectAll();

    int deleteByPrimaryKey(Long id);

    int insert(TMeter record);

    TMeter selectByPrimaryKey(Long id);

    int updateByPrimaryKey(TMeter record);

    List<Map<String,Float>> countPowerTotalByRegion();
    List<Map<String,String>> countPowerTotalByEdge(@Param(value = "startTime")LocalDateTime startTime,
                                                  @Param(value = "endTime")LocalDateTime endTime);
    String countPowerTotalAll();

    List<TMeter> selectMeterByDeviceId();

}
