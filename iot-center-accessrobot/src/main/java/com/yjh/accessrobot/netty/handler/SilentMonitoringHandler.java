package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.enumeration.AlarmLevelEnum;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.commons.utils.file.FileUtil;
import com.yjh.accessrobot.configuration.UpFtpsConfig;
import com.yjh.accessrobot.module.command.dao.TCameraPresetMapper;
import com.yjh.accessrobot.module.command.dao.TCruisePointInstanceMapper;
import com.yjh.accessrobot.module.command.dao.TStdDeviceMapper;
import com.yjh.accessrobot.module.command.dao.TWarnInfoMapper;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * 巡视主机接收边缘节点消息
 *
 * @author quzhihui
 * @date 2022/8/19 - 15:41
 */
@Slf4j
@Service
public class SilentMonitoringHandler implements MessageHandlerStrategy, InitializingBean {

    /**
     * 调用智能分析主机图像分析接口
     */
    private static final String ANALYSE_URL = "http://iot-center-platform/picAnalyseNoDetection";

    @Resource
    private RedisTemplate redisTemplate;
    @Value("${other.webSocketUrl}")
    private String syncWebsocketUrl;
    @Resource
    private TCruisePointInstanceMapper tCruisePointInstanceMapper;

    @Autowired
    private TCameraPresetMapper tCameraPresetMapper;
    @Autowired
    private TStdDeviceMapper tStdDeviceMapper;
    @Autowired
    private TWarnInfoMapper tWarnInfoMapper;
    @Autowired
    private UpFtpsConfig upFtpsConfig;
    @Resource
    private ServiceRestTemplate serviceRestTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到静默监视的数据了+++++++++++++++++ xmlBaseModel:{}", JSON.toJSONString(xmlBaseModel));
        String sendCode = xmlBaseModel.getSendCode();
        if (!Constant.robotRegisterFlag.getOrDefault(sendCode, false)) {
            return;
        }
        String absPath = String.valueOf(xmlBaseModel.getItems().get(0).get("file_path"));
        String presetId = String.valueOf(xmlBaseModel.getItems().get(0).get("device_id"));
        String monitorType = String.valueOf(xmlBaseModel.getItems().get(0).get("monitor_type"));
        TCameraPreset tCameraPreset = tCameraPresetMapper.selectByEdgeCodeAndOriginId(sendCode, presetId);
        if (tCameraPreset == null) {
            log.error("tCameraPreset is null edgeCode:{} originId:{}", sendCode, presetId);
            return;
        }

        String desc = getDesc(monitorType);
        log.info("desc :{}", desc);
        if (desc == null) {
            //调用算法
            sendAnalyse(absPath, tCameraPreset);
        } else {
            // 生成告警 ，发送上级系统
            processAlarm(sendCode, absPath, presetId, monitorType, tCameraPreset, desc);
        }
    }

    private void processAlarm(String sendCode, String absPath, String presetId, String monitorType, TCameraPreset tCameraPreset, String desc) {
        TCruisePointInstance tCruisePointInstance = tCruisePointInstanceMapper.selectByEdgeCodeAndCruiseId(sendCode, tCameraPreset.getPresetId());
        if (tCruisePointInstance == null) {
            log.error("tCruisePointInstance is null edgeCode:{} cruiseId:{}", sendCode, presetId);
            return;
        }
        // 图片在ftps上的全路径
        String resultAbsolutePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content") + absPath;
        String targetPath = redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content") + absPath;
        try {
            FileUtil.copyFileUsingStream(resultAbsolutePath, targetPath);
        } catch (IOException e) {
            log.error("复制文件失败，resultAbsolutePath:{}  targetPath:{} ", resultAbsolutePath, targetPath, e);
            return;
        }
        String defectResultRealImg = targetPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")),
                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg", "content")));
        String alarmLevel = tWarnInfoMapper.selectAlarmLevel("defect_model", desc);
        if (alarmLevel == null) {
            alarmLevel = "132";
        }
        TWarnInfo tWarnInfo = new TWarnInfo()
                .setWarnLevel(Integer.valueOf(alarmLevel))
                .setWarnTime(new Date())
                .setWarnName("静默监视告警数据")
                .setWarnContent(desc)
                .setDeviceId(tCruisePointInstance.getDeviceId())
                .setCunstomId(tCruisePointInstance.getCustomId())
                .setInstanceId(tCruisePointInstance.getInstanceId())
                .setStdMeteId(tCruisePointInstance.getDeviceMeteId())
                .setConfMode(276)
                .setDefectModel(450)
                .setAlarmSource(800)
                .setImagePath(defectResultRealImg);
        tWarnInfoMapper.insert(tWarnInfo);
        // webSocket通知前端调用查询告警弹框的接口
        sendWebsocket( tWarnInfo);
         // 上传消息到巡视主机
        sendUpSystem(monitorType, tCruisePointInstance, tWarnInfo);
    }

    private void sendUpSystem(String monitorType, TCruisePointInstance tCruisePointInstance, TWarnInfo tWarnInfo) {
        String warnTime = DateTimeUtil.format(tWarnInfo.getWarnTime());
        XMLBaseModel xmlBaseModel1 = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>(16);
        xmlBaseModel1.setType("63");
        xmlItem.put("patroldevice_code", tCruisePointInstance.getDeviceId());
        xmlItem.put("patroldevice_name", tStdDeviceMapper.selectByPrimaryKey(tCruisePointInstance.getDeviceId()).getDeviceName());
        xmlItem.put("alarm_level", AlarmLevelEnum.getAlarmLevelByCode(tWarnInfo.getWarnLevel().toString()).getProtocolCode());
        xmlItem.put("monitor_type", monitorType);
        // 目前都是识别图片 所以是5
        xmlItem.put("file_type", "5");
        String imgPath = tWarnInfo.getImagePath().replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg", "content")), String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")));
        String targetNamePath = imgPath.replace(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")), "").substring(1);
        log.info("imgPath:{},targetNamePath:{}", imgPath, targetNamePath);
        uploadFileToUpFtps(imgPath, "jm/" + targetNamePath, upFtpsConfig);
        xmlItem.put("file_path", targetNamePath);
        xmlItem.put("time", warnTime);
        xmlItem.put("content", tWarnInfo.getWarnContent());
        xmlItems.add(xmlItem);
        xmlBaseModel1.setItems(xmlItems);
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel1);
        Map<String, List<XMLBaseModel>> alarmMap = new HashMap<>(3);
        alarmMap.put("list", list);
        try {
            Result result = serviceRestTemplate.postForObject(Constant.TCP_URL, alarmMap, Result.class);
            log.info("告警上报 param :{} result:{}", JSON.toJSONString(alarmMap), result);
        } catch (RestClientException e) {
            log.error("上报异常: ", e);
        }
    }

    private void sendWebsocket(TWarnInfo tWarnInfo) {
        Map<String, Object> jasonMaps = new HashMap<>(16);
        jasonMaps.put("type", "alarmPopUp");
        jasonMaps.put("warnType", "1");
        jasonMaps.put("warnLevel", tWarnInfo.getWarnLevel());
        jasonMaps.put("warnId", tWarnInfo.getWarnId());
        jasonMaps.put("defectModel", 450);
        String json = JSON.toJSONString(jasonMaps);
        log.info("发送给前端的消息：{}", json);
        try {
            String result = serviceRestTemplate.postForObject(syncWebsocketUrl, json, String.class);
            log.info("param:{} result:{}", json, result);
        } catch (Exception e) {
            log.error("发送前端失败", e);
        }
    }

    private void sendAnalyse(String absPath, TCameraPreset tCameraPreset) {
        // 调用算法接口分析结果
        List<Analysis> analysisList = new ArrayList<>();
        Analysis analysis = new Analysis()
                // 暂定静默监视识别类型为12,没有实际意义
                .setAnalyseType("12")
                .setInstanceId(tCameraPreset.getPresetId())
                .setTaskId("jm")
                .setPicPath(absPath);
        analysisList.add(analysis);
        try {
            String result = serviceRestTemplate.postForObject(ANALYSE_URL, analysisList, String.class);
            log.info("param:{},result:{}", StringUtils.join(analysisList), result);
        } catch (Exception e) {
            log.error("请求算法失败: ", e);
        }
    }

    private static String getDesc(String monitorType) {
        String desc = null;
        if ("103".equals(monitorType)) {
            desc = "小动物入侵";
        } else if ("3".equals(monitorType)) {
            desc = "未穿工装";
        } else if ("4".equals(monitorType)) {
            desc = "人员聚集/徘徊";
        } else if ("1".equals(monitorType)) {
            desc = "未穿安全帽";
        }
        return desc;
    }



    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath     源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName, UpFtpsConfig upFtpsConfig) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误：", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.SILENT_MONITORING_DATA.getCode(), this);
    }
}