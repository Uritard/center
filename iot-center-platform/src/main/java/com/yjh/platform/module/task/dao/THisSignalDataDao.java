package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.THisSignalData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface THisSignalDataDao {

    int insert(THisSignalData tHisSignalData);
    int deleteByPrimaryId(@Param(value = "Id")Long Id);
    int update(THisSignalData tHisSignalData);
    THisSignalData selectByPrimaryId(@Param(value = "Id")Long Id);
    List<THisSignalData> select(@Param(value = "Id") Long Id,
                                @Param(value = "meteId") Long meteId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "recordTime") Date recordTime,
                                @Param(value = "meteKind") Integer meteKind,
                                @Param(value = "meteValue") String meteValue,
                                @Param(value = "lastMeteValue") String lastMeteValue);
    List<THisSignalData> selectByPage(THisSignalData tHisSignalData);

    int batchInsert(List<THisSignalData> list);
}
