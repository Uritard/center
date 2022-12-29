package com.yjh.accessrobot.netty.thread;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.service.NonhomologousWarnService;

import java.util.Map;

/**
 * @author sunjinyan
 * @since 2022-04-08
 * 机器人侧非同源告警处理线程
 */
@lombok.extern.slf4j.Slf4j
public class NonhomologousWarnThread implements Runnable{

    private Map<String,String> cruiseResultMap;
    private int isResult;

    public NonhomologousWarnThread(Map<String,String> cruiseResultMap, int isResult){
        this.cruiseResultMap = cruiseResultMap;
        this.isResult = isResult;
    }

    @Override
    public void run(){
        try {
            log.info("开始处理巡检结果并生成相应的非同源告警 >>>>>>> cruiseResultMap==={}, isResult ==={}", cruiseResultMap, isResult);
            StaticContextAccessor.getBean(NonhomologousWarnService.class).insertNonhomologousWarn(cruiseResultMap,isResult);
        } catch (Exception e) {
            log.error("巡检结果处理失败", e);
            throw new RuntimeException("巡检结果处理失败");
        }
    }
}
