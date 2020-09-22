package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCfgDataCurrent;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface TCfgDataCurrentDao {

    int insert(TCfgDataCurrent tCfgDataCurrent);
    int deleteByPrimaryId(@Param(value = "meteId") Long meteId);
    int update(TCfgDataCurrent tCfgDataCurrent);
    TCfgDataCurrent selectByPrimaryId(@Param(value = "meteId") Long meteId);
    List<TCfgDataCurrent> select(@Param(value = "meteId") Long meteId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "cunstomId") String cunstomId,
                                @Param(value = "recordTime") Date recordTime,
                                @Param(value = "meteKind") Integer meteKind,
                                @Param(value = "regionId") String regionId,
                                @Param(value = "meteValue") String meteValue,
                                @Param(value = "lastMeteValue") String lastMeteValue);
    List<TCfgDataCurrent> selectByPage(TCfgDataCurrent tCfgDataCurrent);

    int batchInsert(List<TCfgDataCurrent> list);
    List<Long> selectAllMeteId();
    TCfgDataCurrent selectCurrentDataByMeteId(@Param(value = "meteId")Long meteId);

}
