package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TMeter;
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
}
