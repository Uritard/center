package com.yjh.accessvideo.module.device.service;

import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.module.device.entity.Analysis;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author tt
* @since 2020-08-20
*/
@Service
public class AnalysisService {

    @Logs(title = "算法调用", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int feignAlgorithm(List<Analysis> analysisList) {
        return 1;
    }

    @Logs(title = "缺陷调用", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int feignDefect(List<Analysis> analysisList) {
        return 1;
    }

}

