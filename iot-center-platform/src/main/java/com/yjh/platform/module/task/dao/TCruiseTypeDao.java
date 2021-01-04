package com.yjh.platform.module.task.dao;

import java.util.List;

import com.yjh.platform.module.task.entity.TCruiseType;
import com.yjh.platform.module.task.entity.TCruiseTypeDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-11-17
 */
@Repository
public interface TCruiseTypeDao {

    int add(TCruiseType tCruiseType);
    int deleteByPrimaryId(@Param(value = "subType") Integer subType);
    int update(TCruiseType tCruiseType);
    TCruiseType selectByPrimaryId(@Param(value = "subType") Integer subType);
    List<TCruiseTypeDetail> select(@Param(value = "subType") Integer subType);
    List<TCruiseTypeDetail> selectByPage(TCruiseType tCruiseType);

    int batchAdd(List<TCruiseType> list);
    int batchDelete(List<String> list);
    List<Long> selectIdList(@Param(value = "subType") Integer subType);
    int deleteForInstanceId(@Param(value = "list")List<Long> list);
}
