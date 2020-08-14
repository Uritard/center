package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.TStdMeteModel;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-07
 */
@Repository
public interface TStdMetemodelDao {

    int insert(TStdMeteModel tStdMeteModel);
    int deleteByPrimaryId(@Param(value = "modelId") Long modelId);
    int update(TStdMeteModel tStdMeteModel);
    TStdMeteModel selectByPrimaryId(@Param(value = "modelId") Long modelId);
    List<TStdMeteModel> select(@Param(value = "modelId") Long modelId,
                               @Param(value = "modelName") String modelName,
                               @Param(value = "deviceType") Integer deviceType,
                               @Param(value = "remark") String remark);
    List<TStdMeteModel> selectByPage(TStdMeteModel tStdMeteModel);

    int batchInsert(List<TStdMeteModel> list);
    List<TStdMeteModelDetail> selectMeteByDeviceType(@Param(value = "deviceType") Integer deviceType);
}
