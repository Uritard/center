package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TIotDeviceData;
import com.yjh.accessrobot.module.command.entity.TMeter;
import com.yjh.accessrobot.module.command.entity.TStdDevice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
* @author YIJIAHE
* @description 针对表【t_std_device(标准化设备表)】的数据库操作Mapper
* @createDate 2022-11-11 11:05:16
* @Entity generator.domain.TStdDevice
*/
@Repository
public interface TStdDeviceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TStdDevice record);

    int insertSelective(TStdDevice record);

    TStdDevice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TStdDevice record);

    int updateByPrimaryKey(TStdDevice record);

    List<Map<String, String>> selectDictCodeByUpDict(@Param("colName") String colName);

    List<TStdDevice> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int batchInsert(List<TStdDevice> list);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );

    int deleteByEdgeCode(@Param("edgeCode") String edgeCode);

    TStdDevice selectByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode, @Param("originId") String originId);

    Map<String,String> selectInstanceInfo(@Param(value = "originId") Long originId);

    int deleteAllMete(@Param("edgeCode") String edgeCode);
    int deleteAllMeteByAddress(@Param("edgeCode") String edgeCode,
                               @Param("list") List<TMeter> list);
    int batchInsertMeter(@Param("list") List<TMeter> list);
    int batchInsertMeterLog(@Param("list") List<TMeter> list);
    List<TMeter> selectAllMeterByEdgeCode(@Param("edgeCode") String edgeCode);
    int updateById(TMeter tMeter);

    List<TIotDeviceData> selectIotDeviceByEdgeCode(@Param("edgeCode") String edgeCode);
    int batchInsertIotDevice(@Param("list") List<TIotDeviceData> list);
    int batchInsertIotDevicePoint(@Param("list") List<TIotDeviceData> list);
    int batchInsertIotDeviceData(@Param("list") List<TIotDeviceData> list);
    int updateIotDevice(TIotDeviceData tIotDeviceData);
    int updateIotDevicePoint(TIotDeviceData tIotDeviceData);
    int deleteIotDevice(@Param("list") List<Long> list);
    int deleteIotDevicePoint(@Param("list") List<Long> list);
}
