package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TStdDevice;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
* @author YIJIAHE
* @description 针对表【t_std_device(标准化设备表)】的数据库操作Mapper
* @createDate 2022-11-11 11:05:16
* @Entity generator.domain.TStdDevice
*/
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

}
