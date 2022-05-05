package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class ModelFileSyncAndTaskControlHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        if (xmlBaseModel.getItems().get(0).size() == 1) {
            // task control
            if (xmlBaseModel.getItems().get(0).containsKey("task_patrolled_id")) {
                String taskId = xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString();
                log.info("机器人收到任务控制指令了,这是机器人响应的巡视任务执行Id==={}", taskId);
            } else if (xmlBaseModel.getItems().get(0).containsKey("error_code")) {
                switch (xmlBaseModel.getItems().get(0).get("error_code").toString()) {
                    case "0": log.info("成功");break;
                    case "1": log.info("机器人异常");break;
                    case "2": log.info("无权限（或高优先级任务存在");break;
                    case "3": log.info("其它异常");break;
                    default: break;
                }
            }
        } else if (xmlBaseModel.getItems().get(0).size() >= 2) {
            log.info("机器人收到模型指令了,这是机器人的响应");
            // model file sync
            String deviceFile = xmlBaseModel.getItems().get(0).get("device_file_path").toString();
            String robotFile = xmlBaseModel.getItems().get(0).get("robot_file_path").toString();
            String propertyFile = "";
            if(xmlBaseModel.getItems().get(0).containsKey("property_file_path")){
                propertyFile = xmlBaseModel.getItems().get(0).get("property_file_path").toString();
            }
            log.info("模型同步的文件路径信息,deviceFile:{},robotFile:{},propertyFile:{}", deviceFile, robotFile, propertyFile);
            if (StringUtils.isNotEmpty(deviceFile) && StringUtils.isNotEmpty(robotFile)) {
                Map<String, String> map = new HashMap<>(5);
                map.put("robotFile", robotFile);
                map.put("deviceFile", deviceFile);
                map.put("propertyFile", propertyFile);
                map.put("robotCode", xmlBaseModel.getSendCode());
                robotService.addRobotFile(map);
            }

            // device model
//            robotService.uploadFile(deviceFile, deviceFile);
            // robot model
//            robotService.uploadFile(robotFile, robotFile);
            // 国网要求
//            robotService.upToCruise(xmlBaseModel);
        } else if (xmlBaseModel.getItems().get(0).size() == 0) {
            log.info("机器人收到检修区域指令了,这是机器人的响应");
            // maintenance area was issued successfully
            robotService.receivingResponse(xmlBaseModel, receiveSessionId);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.MODEL_SYNC.getCode(), this);
    }
}
