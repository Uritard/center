package com.yjh.accessvideo.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.accessvideo.module.device.entity.TVideoAnalyseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-10-19
 */
@Repository
public interface TVideoAnalyseResultDao {

    int insert(TVideoAnalyseResult tVideoAnalyseResult);
    int deleteByPrimaryId(@Param(value = "algorithmResultId") Long algorithmResultId);
    int update(TVideoAnalyseResult tVideoAnalyseResult);
    TVideoAnalyseResult selectByPrimaryId(@Param(value = "algorithmResultId") Long algorithmResultId);
    List<TVideoAnalyseResult> select(@Param(value = "algorithmResultId") Long algorithmResultId,
                                @Param(value = "analyseTime") Date analyseTime,
                                @Param(value = "analyseConfId") String analyseConfId,
                                @Param(value = "algorithmResult") String algorithmResult,
                                @Param(value = "algorithmPicture") String algorithmPicture,
                                @Param(value = "algorithmStatus") Integer algorithmStatus,
                                @Param(value = "resultRate") String resultRate,
                                @Param(value = "resultDescribe") String resultDescribe,
                                @Param(value = "reserver") Integer reserver);
    List<TVideoAnalyseResult> selectByPage(TVideoAnalyseResult tVideoAnalyseResult);

    int batchInsert(List<TVideoAnalyseResult> list);
}
