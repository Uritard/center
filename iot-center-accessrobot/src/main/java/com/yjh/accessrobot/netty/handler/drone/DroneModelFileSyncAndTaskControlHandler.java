package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.DroneService;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
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
public class DroneModelFileSyncAndTaskControlHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        if (xmlBaseModel.getItems().get(0).size() == 1) {
            // task control
            if (xmlBaseModel.getItems().get(0).containsKey("task_patrolled_id")) {
                String taskId = xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString();
                log.info("无人机收到任务控制指令了,这是无人机响应的巡视任务执行Id==={}", taskId);
            } else if (xmlBaseModel.getItems().get(0).containsKey("error_code")) {
                switch (xmlBaseModel.getItems().get(0).get("error_code").toString()) {
                    case "0":
                        log.info("成功");
                        break;
                    case "1":
                        log.info("无人机异常");
                        break;
                    case "2":
                        log.info("无权限（或高优先级任务存在");
                        break;
                    case "3":
                        log.info("其它异常");
                        break;
                    default:
                        break;
                }
            }
        } else if (xmlBaseModel.getItems().get(0).size() >= 2) {
            log.info("无人机收到模型指令了,这是无人机的响应");
            // model file sync
            String deviceFile = xmlBaseModel.getItems().get(0).get("device_file_path").toString();
            String droneFile = xmlBaseModel.getItems().get(0).get("drone_file_path").toString();
            String propertyFile = "";
            if (xmlBaseModel.getItems().get(0).containsKey("property_file_path")) {
                propertyFile = xmlBaseModel.getItems().get(0).get("property_file_path").toString();
            }
            log.info("模型同步的文件路径信息,deviceFile:{},droneFile:{},propertyFile:{}", deviceFile, droneFile, propertyFile);
            if (StringUtils.isNotEmpty(deviceFile) && StringUtils.isNotEmpty(droneFile)) {
                Map<String, String> map = new HashMap<>(5);
                map.put("droneFile", droneFile);
                map.put("deviceFile", deviceFile);
                map.put("propertyFile", propertyFile);
                map.put("droneCode", xmlBaseModel.getSendCode());
                droneService.addDroneFile(map);
            }

            // device model
            droneService.uploadFile(deviceFile, deviceFile);
            // drone model
            droneService.uploadFile(droneFile, droneFile);
            // 国网要求
            droneService.upToCruise(xmlBaseModel);
        } else if (xmlBaseModel.getItems().get(0).size() == 0) {
            log.info("无人机收到检修区域指令了,这是无人机的响应");
            // maintenance area was issued successfully
            droneService.receivingResponse(xmlBaseModel, receiveSessionId);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.MODEL_SYNC.getCode(), this);
    }
}
