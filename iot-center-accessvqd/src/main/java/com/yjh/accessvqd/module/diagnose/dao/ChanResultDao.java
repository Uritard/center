package com.yjh.accessvqd.module.diagnose.dao;

import java.util.List;
import java.util.Date;

import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
}
