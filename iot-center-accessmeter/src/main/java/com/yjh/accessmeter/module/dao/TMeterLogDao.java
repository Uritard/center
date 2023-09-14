package com.yjh.accessmeter.module.dao;

import com.yjh.accessmeter.module.device.entity.TMeter;
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
