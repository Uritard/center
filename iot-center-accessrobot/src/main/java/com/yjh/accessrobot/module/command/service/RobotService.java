package com.yjh.accessrobot.module.command.service;


import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.commons.logs.Logs;
import com.yjh.accessrobot.module.command.entity.Analysis;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author tt
* @since 2020-08-20
*/
@Service
public class RobotService {

    private Logger log = LoggerFactory.getLogger(RobotService.class);

    private static long algorithmMsgId = 100000001;

    @Logs(title = "机器人控制调用", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String feignRobotControl(List<Analysis> analysisList, int recognizePort) throws InterruptedException {

        RobotServerHandler.getRobotServerHandlerMap().get(recognizePort).SendHeartBeat();
        log.info("analysisList.get(0).getInstanceId():"+analysisList);
        return "success";
    }

}

