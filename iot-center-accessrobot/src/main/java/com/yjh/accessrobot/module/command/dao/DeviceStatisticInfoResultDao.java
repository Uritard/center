package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.DeviceStatisticInfoResult;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceStatisticInfoResultDao {
    public DeviceStatisticInfoResult select(String deviceCode);

    public int insert(DeviceStatisticInfoResult deviceStatisticInfoResult);
    public int insertSelective(DeviceStatisticInfoResult deviceStatisticInfoResult);
    public int changeRun(DeviceStatisticInfoResult deviceStatisticInfoResult);
}
