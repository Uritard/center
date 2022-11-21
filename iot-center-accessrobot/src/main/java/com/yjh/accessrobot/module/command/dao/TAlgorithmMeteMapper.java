package com.yjh.accessrobot.module.command.dao;


import com.yjh.accessrobot.module.command.entity.TAlgorithmMete;
import org.apache.commons.collections4.SetUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_algorithm_mete(算法测点配置表)】的数据库操作Mapper
* @createDate 2022-11-18 14:40:07
* @Entity generator.domain.TAlgorithmMete
*/
public interface TAlgorithmMeteMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TAlgorithmMete record);

    int insertSelective(TAlgorithmMete record);

    TAlgorithmMete selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TAlgorithmMete record);

    int updateByPrimaryKey(TAlgorithmMete record);

    List<TAlgorithmMete> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int deleteByEdgeCodeAndOriginId(String edgeCode, SetUtils.SetView<String> deleteIdSet);

    int batchInsert(List<TAlgorithmMete> list);

}
