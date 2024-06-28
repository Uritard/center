package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TMeter;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhangyuyi
 * @create 2023-07-25
 */
@Repository
public interface TMeterLogDao {
    int insert(TMeter record);

    int deleteExpireData();

    List<TMeter> list(TMeter tMeter);

    List<TMeter> collectList(TMeter tMeter);

    TMeter selectPowerDifferenceValue(@Param(value = "id") Long id);
}
