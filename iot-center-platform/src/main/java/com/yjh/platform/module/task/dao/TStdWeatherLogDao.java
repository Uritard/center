package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.dal.TStdWeatherLogDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhangyuyi
 * @create 2023-07-19
 */
@Repository
public interface TStdWeatherLogDao {

    /**
     * 查询列表*
     * @param tStdWeatherLogDO
     * @return
     */
    List<TStdWeatherLogDO> list(TStdWeatherLogDO tStdWeatherLogDO);

    /**
     * 批量新增*
     * @param weatherLogs
     */
    void batchInsert(@Param("list") List<TStdWeatherLogDO> weatherLogs);
}
