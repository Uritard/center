package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TVideoAlgoResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-24
 */
@Repository
public interface TVideoAlgoResultDao {

    int insert(TVideoAlgoResult tVideoAlgoResult);
    int deleteByPrimaryId(@Param(value = "id")Long id);
    int update(TVideoAlgoResult tVideoAlgoResult);
    TVideoAlgoResult selectByPrimaryId(@Param(value = "id")Long id);
    List<TVideoAlgoResult> select(@Param(value = "pointId") Long pointId,
                                @Param(value = "taskId") String taskId,
                                @Param(value = "planId") Long planId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "presetId") Long presetId,
                                @Param(value = "deviceMeteId") Long deviceMeteId,
                                @Param(value = "picUrl") String picUrl,
                                @Param(value = "cusId") String cusId,
                                @Param(value = "algorithmId") Long algorithmId,
                                @Param(value = "status") String status,
                                @Param(value = "analyseResult") String analyseResult,
                                @Param(value = "picOrignal") String picOrignal,
                                @Param(value = "evaluationState") Integer evaluationState,
                                @Param(value = "signpic") String signpic,
                                @Param(value = "algorithmType") String algorithmType,
                                @Param(value = "algorithmSonType") String algorithmSonType,
                                @Param(value = "executeTime") Date executeTime,
                                @Param(value = "createTime") Date createTime);
    List<TVideoAlgoResult> selectByPage(TVideoAlgoResult tVideoAlgoResult);

    int batchInsert(List<TVideoAlgoResult> list);
}
