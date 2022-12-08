package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TStdDeviceMete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_std_devicemete(标准设备测点表)】的数据库操作Mapper
* @createDate 2022-11-11 11:05:16
* @Entity generator.domain.TStdDevicemete
*/
@Repository
public interface TStdDevicemeteMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TStdDeviceMete record);

    int insertSelective(TStdDeviceMete record);

    TStdDeviceMete selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TStdDeviceMete record);

    int updateByPrimaryKey(TStdDeviceMete record);

    List<TStdDeviceMete> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int batchInsert(List<TStdDeviceMete> list);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );

    int deleteByEdgeCode(@Param("edgeCode") String edgeCode);

}
