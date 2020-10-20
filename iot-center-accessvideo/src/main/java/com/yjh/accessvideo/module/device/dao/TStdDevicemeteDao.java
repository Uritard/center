package com.yjh.accessvideo.module.device.dao;

import java.math.BigDecimal;
import java.util.List;

import com.yjh.accessvideo.module.device.entity.TStdDevicemete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-10-19
 */
@Repository
public interface TStdDevicemeteDao {

    int insert(TStdDevicemete tStdDevicemete);
    int deleteByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    int update(TStdDevicemete tStdDevicemete);
    TStdDevicemete selectByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    List<TStdDevicemete> selectByPage(TStdDevicemete tStdDevicemete);

    int batchInsert(List<TStdDevicemete> list);
}
