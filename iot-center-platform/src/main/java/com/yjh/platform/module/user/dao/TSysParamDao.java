package com.yjh.platform.module.user.dao;

import java.util.List;

import com.yjh.platform.module.user.entity.TSysParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-07
 */
@Repository
public interface TSysParamDao {

    int insert(TSysParam tSysParam);
    int deleteByPrimaryId(@Param(value = "paramId") Integer paramId);
    int update(TSysParam tSysParam);
    TSysParam selectByPrimaryId(@Param(value = "paramId") Integer paramId);
    List<TSysParam> select(@Param(value = "paramId") Integer paramId,
                                @Param(value = "paramType") String paramType,
                                @Param(value = "paramName") String paramName,
                                @Param(value = "content") String content,
                                @Param(value = "remark") String remark);
    List<TSysParam> selectByPage(TSysParam tSysParam);

    int batchInsert(List<TSysParam> list);
}
