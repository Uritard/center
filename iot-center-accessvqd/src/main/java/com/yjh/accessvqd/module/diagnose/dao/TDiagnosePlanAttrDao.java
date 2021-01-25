package com.yjh.accessvqd.module.diagnose.dao;

import java.util.List;

import com.yjh.accessvqd.module.diagnose.entity.TDiagnosePlanAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2021-01-22
 */
@Repository
public interface TDiagnosePlanAttrDao {

    int insert(TDiagnosePlanAttr tDiagnosePlanAttr);
    int deleteByPrimaryId(@Param(value = "diagnosePlanId") String diagnosePlanId);
    int update(TDiagnosePlanAttr tDiagnosePlanAttr);
    List<TDiagnosePlanAttr> selectByPrimaryId(@Param(value = "diagnosePlanId") String diagnosePlanId);
    List<TDiagnosePlanAttr> select(@Param(value = "diagnosePlanId") String diagnosePlanId,
                                @Param(value = "channelId") String channelId);
    List<TDiagnosePlanAttr> selectByPage(TDiagnosePlanAttr tDiagnosePlanAttr);

    int batchInsert(List<TDiagnosePlanAttr> list);
}
