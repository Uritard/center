package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.configuration.UpFtpsConfig;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Slf4j
@Service
public class InspectionResultHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UpFtpsConfig upFtpsConfig;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到巡视结果了+++++++++++++++++");
        // Deal with robot task result data
        String robotCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(robotCode)) {
            log.error("机器人编码为空");
            throw new RuntimeException("机器人编码为空");
        }

        // 给机器人响应
        String cruiseResultXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true,robotCode));
        byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, cruiseResultXmlString);
        RobotServerHandler.send(cruiseResultProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);

        // 巡视结果上报上一级系统
        resultToUpSystem(xmlBaseModel, robotCode);

        // 处理数据
        List<RobotPatrolTaskResult> resultList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            Map<String, String> cruiseResultMap = new HashMap<>(16);
            cruiseResultMap.put("patrolDeviceName", String.valueOf(item.get("patroldevice_name")));
            cruiseResultMap.put("patrolDeviceCode", String.valueOf(item.get("patroldevice_code")));
            cruiseResultMap.put("robotCode", robotCode);
            cruiseResultMap.put("taskName", String.valueOf(item.get("task_name")));
            cruiseResultMap.put("taskCode", String.valueOf(item.get("task_code")));
            cruiseResultMap.put("deviceName", String.valueOf(item.get("device_name")));
            cruiseResultMap.put("deviceId", String.valueOf(item.get("device_id")));
            cruiseResultMap.put("valueType", String.valueOf(item.get("value_type")));
            cruiseResultMap.put("value", String.valueOf(item.get("value")));
            cruiseResultMap.put("valueUnit", String.valueOf(item.get("value_unit")));
            cruiseResultMap.put("unit", String.valueOf(item.get("unit")));
            cruiseResultMap.put("time", String.valueOf(item.get("time")));
            cruiseResultMap.put("recognitionType", String.valueOf(item.get("recognition_type")));
            cruiseResultMap.put("fileType", String.valueOf(item.get("file_type")));
            cruiseResultMap.put("rectangle", String.valueOf(item.get("rectangle")));
            cruiseResultMap.put("taskPatrolledId", String.valueOf(item.get("task_patrolled_id")));
            cruiseResultMap.put("filePath", String.valueOf(item.get("file_path")));
            cruiseResultMap.put("valid", Objects.nonNull(item.get("valid")) ?
                    String.valueOf(item.get("valid")) : "");
            cruiseResultMap.put("originFilePath", Objects.nonNull(item.get("origin_file_path")) ?
                    String.valueOf(item.get("origin_file_path")) : "");
            cruiseResultMap.put("originFileResultPath", Objects.nonNull(item.get("origin_file_result_path")) ?
                    String.valueOf(item.get("origin_file_result_path")) : "");

            String toJson = JSONObject.toJSONString(cruiseResultMap);
            RobotPatrolTaskResult taskResult = JSONObject.toJavaObject(JSON.parseObject(toJson), RobotPatrolTaskResult.class);
            resultList.add(taskResult);
        }
        log.info("resultList=={}", resultList);

        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_RESULT_PROCESS, resultList, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    /**
     * 巡视结果上报上级系统
     * @param xmlBaseModel xml格式的内容
     */
    @Async
    public void resultToUpSystem(XMLBaseModel xmlBaseModel, String robotCode){
        try {
            for(Map<String, Object> item : xmlBaseModel.getItems()){
                String value = String.valueOf(item.get("file_path"));
                String tagPath = "robotTask/" + value;
                log.info("tagPath==={}", tagPath);
                String imgPath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content") + "/" + value;
                uploadFileToUpFtps(imgPath, "/" + tagPath, upFtpsConfig);
                String materialId = robotService.selectMaterialId(String.valueOf(item.get("device_id")));
                item.put("material_id", Optional.ofNullable(materialId).orElse(""));
                String dataType = robotService.selectIsDrone(robotCode) ? "0x03" : "0x02";
                item.put("data_type", dataType);
                item.put("file_path", tagPath);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        log.info("准备上报上级系统的机器人巡视结果是==={}", xmlBaseModel);
        robotService.upToCruise(xmlBaseModel);
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName, UpFtpsConfig upFtpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误:{}", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.INSPECTION_RESULT.getCode(), this);
    }
}
