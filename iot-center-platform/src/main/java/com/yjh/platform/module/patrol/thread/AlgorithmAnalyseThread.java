package com.yjh.platform.module.patrol.thread;

import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.entity.AnalysePatrolTaskResult;
import com.yjh.platform.module.patrol.entity.interlanalysis.PicAnalyseResponse;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * @author hyh
 * 算法返回结果解析
 * @since 2022/8/25
 **/
@Slf4j
public class AlgorithmAnalyseThread implements Runnable {

    private PicAnalyseResponse response;
    private final String flagId;
    private PatrolResultHandler patrolResultHandler;
    private IntelAnalysisService intelAnalysisService;

    public AlgorithmAnalyseThread(PicAnalyseResponse response, String flagId) {
        this.response = response;
        this.flagId = flagId;
        this.patrolResultHandler = StaticContextAccessor.getBean(PatrolResultHandler.class);
        this.intelAnalysisService = StaticContextAccessor.getBean(IntelAnalysisService.class);
    }

    @Override
    public void run() {
        try {
            List<AnalysePatrolTaskResult> resultList = intelAnalysisService.sendAnalysePatrolTaskResult(response, flagId);
            patrolResultHandler.analysePatrolTaskResult(resultList);
        } catch (Exception e) {
            log.error("算法返回结果解析", e);
        }
    }
}
