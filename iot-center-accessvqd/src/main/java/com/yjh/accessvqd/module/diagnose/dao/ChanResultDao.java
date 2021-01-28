package com.yjh.accessvqd.module.diagnose.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.accessvqd.module.diagnose.entity.DiagnoseResultDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2021-01-21
 */
@Repository
public interface ChanResultDao {

    int insert(ChanResult chanResult);
    int deleteByPrimaryId(@Param(value = "diagnoseResultId") Long diagnoseResultId);
    int update(ChanResult chanResult);
    ChanResult selectByPrimaryId(@Param(value = "diagnoseResultId") Long diagnoseResultId);
    List<ChanResult> select(@Param(value = "diagnoseResultId") Long diagnoseResultId,
                                @Param(value = "channelId") String channelId,
                                @Param(value = "ip") String ip,
                                @Param(value = "chanIndex") String chanIndex,
                                @Param(value = "checkTime") Date checkTime,
                                @Param(value = "channelResult") Integer channelResult,
                                @Param(value = "signalResult") Integer signalResult,
                                @Param(value = "blurResult") Integer blurResult,
                                @Param(value = "contrastResult") Integer contrastResult,
                                @Param(value = "brightResult") Integer brightResult,
                                @Param(value = "darkResult") Integer darkResult,
                                @Param(value = "chromaResult") Integer chromaResult,
                                @Param(value = "monoResult") Integer monoResult,
                                @Param(value = "noiseResult") Integer noiseResult,
                                @Param(value = "streakResult") Integer streakResult,
                                @Param(value = "freezeResult") Integer freezeResult,
                                @Param(value = "shakeResult") Integer shakeResult,
                                @Param(value = "flashResult") Integer flashResult,
                                @Param(value = "sceneResult") Integer sceneResult,
                                @Param(value = "coverResult") Integer coverResult,
                                @Param(value = "ptzResult") Integer ptzResult,
                                @Param(value = "snapshotUlt") String snapshotUlt,
                                @Param(value = "resultContent") String resultContent);
    List<ChanResult> selectByPage(ChanResult chanResult);

    int batchInsert(List<ChanResult> list);

    //分页条件查询诊断结果
    List<DiagnoseResultDetail> selectDiagnoseResultByPage(@Param(value = "planName")String planName,
                                                          @Param(value = "channelName")String channelName,
                                                          @Param(value = "startTime")Date startTime,
                                                          @Param(value = "endTime")Date endTime,
                                                          @Param(value = "diagnosePlanId")String diagnosePlanId,
                                                          @Param(value = "status")String status);
    //不同故障类型点数量统计
    Map<String,Long>faultTypeSta(@Param(value = "planName")String planName,
                                 @Param(value = "channelName")String channelName);
    //不同监测点状态点数量统计
    Map<String,Long>statusTypeChannel(@Param(value = "planName")String planName,
                                      @Param(value = "channelName")String channelName);

    List<Map<String,Object>> staticalAnalysis(@Param(value = "startTime")Date startTime,
                                              @Param(value = "endTime")Date endTime);

    //查询任务信息 或 监测点信息
    List<Map<String,String>> selectQueryItems(@Param(value = "queryType")Integer queryType);
}
