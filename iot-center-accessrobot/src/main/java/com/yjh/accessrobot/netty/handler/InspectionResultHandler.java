package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.configuration.UpFtpsConfig;
import com.yjh.accessrobot.module.command.entity.RobotPatrolTaskResult;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Slf4j
@Service
public class InspectionResultHandler implements MessageHandlerStrategy, InitializingBean {

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到巡视结果了+++++++++++++++++");
        // Deal with robot task result data
        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(sendCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        // 给下级响应
        String cruiseResultXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true,sendCode));
        byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, cruiseResultXmlString);
        RobotServerHandler.send(cruiseResultProtocol, sendCode);
        log.info("巡视主机给下级{}响应了", sendCode);

        // 处理数据
        List<RobotPatrolTaskResult> resultList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            RobotPatrolTaskResult taskResult = new RobotPatrolTaskResult();
            taskResult.setPatrolDeviceName(String.valueOf(item.get("patroldevice_name")));
            taskResult.setPatrolDeviceCode(String.valueOf(item.get("patroldevice_code")));
            taskResult.setSendCode(sendCode);
            taskResult.setTaskName(String.valueOf(item.get("task_name")));
            taskResult.setTaskCode(String.valueOf(item.get("task_code")));
            taskResult.setDeviceName(String.valueOf(item.get("device_name")));
            taskResult.setDeviceId(String.valueOf(item.get("device_id")));
            taskResult.setValueType(String.valueOf(item.get("value_type")));
            taskResult.setValue(String.valueOf(item.get("value")));
            taskResult.setValueUnit(String.valueOf(item.get("value_unit")));
            taskResult.setUnit(String.valueOf(item.get("unit")));
            taskResult.setTime(String.valueOf(item.get("time")));
            taskResult.setRecognitionType(String.valueOf(item.get("recognition_type")));
            taskResult.setFileType(String.valueOf(item.get("file_type")));
            taskResult.setRectangle(String.valueOf(item.get("rectangle")));
            taskResult.setFilePath(String.valueOf(item.get("file_path")));
            taskResult.setTaskPatrolledId(String.valueOf(item.get("task_patrolled_id")));
            taskResult.setValid(Objects.nonNull(item.get("valid")) ? String.valueOf(item.get("valid")) : "");
            taskResult.setOriginFileResultPath(Objects.nonNull(item.get("origin_file_path")) ? String.valueOf(item.get("origin_file_path")) : "");
            taskResult.setOriginFilePath(Objects.nonNull(item.get("origin_file_result_path")) ? String.valueOf(item.get("origin_file_result_path")) : "");
            taskResult.setAbnormalType((String)item.get("abnormal_type"));
            resultList.add(taskResult);
        }
        log.info("The resultList to platform is=={}", resultList);

        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_RESULT_PROCESS, resultList, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.INSPECTION_RESULT.getCode(), this);
    }
}
