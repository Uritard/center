package com.yjh.accessvqd.module.diagnose.dao;

import java.util.List;

import com.yjh.accessvqd.module.diagnose.entity.TDiagnosePlanDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2021-01-22
 */
@Repository
public interface TDiagnosePlanDetailDao {

    int insert(TDiagnosePlanDetail tDiagnosePlanDetail);
    int deleteByPrimaryId(@Param(value = "diagnosePlanId") String diagnosePlanId);
    int update(TDiagnosePlanDetail tDiagnosePlanDetail);
    TDiagnosePlanDetail selectByPrimaryId(@Param(value = "diagnosePlanId") String diagnosePlanId);
    List<TDiagnosePlanDetail> select(@Param(value = "diagnosePlanId") String diagnosePlanId,
                                @Param(value = "signalOpt") String signalOpt,
                                @Param(value = "blurOpt") String blurOpt,
                                @Param(value = "contrastOpt") String contrastOpt,
                                @Param(value = "brightOpt") String brightOpt,
                                @Param(value = "darkOpt") String darkOpt,
                                @Param(value = "chromaOpt") String chromaOpt,
                                @Param(value = "monoOpt") String monoOpt,
                                @Param(value = "noiseOpt") String noiseOpt,
                                @Param(value = "streakOpt") String streakOpt,
                                @Param(value = "freezeOpt") String freezeOpt,
                                @Param(value = "shakeOpt") String shakeOpt,
                                @Param(value = "flashOpt") String flashOpt,
                                @Param(value = "sceneOpt") String sceneOpt,
                                @Param(value = "coverOpt") String coverOpt,
                                @Param(value = "ptzOpt") String ptzOpt);
    List<TDiagnosePlanDetail> selectByPage(TDiagnosePlanDetail tDiagnosePlanDetail);

    int batchInsert(List<TDiagnosePlanDetail> list);
}
