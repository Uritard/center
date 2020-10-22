package com.yjh.platform.module.user.dao;

import java.util.List;

import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-06
 */
@Repository
public interface TAlgorithmInfoDao {

    int insert(TAlgorithmInfo tAlgorithmInfo);
    int deleteByPrimaryId(@Param(value = "algorithmId") Long algorithmId);
    int update(TAlgorithmInfo tAlgorithmInfo);
    TAlgorithmInfo selectByPrimaryId(@Param(value = "algorithmId") Long algorithmId);
    List<TAlgorithmInfo> select(@Param(value = "algorithmId") Long algorithmId,
                                @Param(value = "algorithmName") String algorithmName,
                                @Param(value = "aliasName") String aliasName,
                                @Param(value = "describel") String describel,
                                @Param(value = "algorithmCode") String algorithmCode,
                                @Param(value = "analyseType") String analyseType,
                                @Param(value = "isAi") Integer isAi);
    List<TAlgorithmInfo> selectByPage(TAlgorithmInfo tAlgorithmInfo);

    int batchInsert(List<TAlgorithmInfo> list);
}
