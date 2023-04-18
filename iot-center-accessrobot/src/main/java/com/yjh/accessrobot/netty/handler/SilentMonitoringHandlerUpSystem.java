package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.enumeration.AlarmLevelEnum;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.file.FileUtil;
import com.yjh.accessrobot.module.command.dao.TStdDeviceMapper;
import com.yjh.accessrobot.module.command.dao.TWarnInfoMapper;
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
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <功能描述> 上级系统接收巡视主机消息
 *
 * @author yanhao
 * @date 2022/11/23
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class SilentMonitoringHandlerUpSystem  implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private TStdDeviceMapper tStdDeviceMapper;
    @Autowired
    private TWarnInfoMapper tWarnInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;
    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("上级系统接收到静默告警数据 xmlBaseModel:{}", JSON.toJSONString(xmlBaseModel));
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
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        String filePath = String.valueOf(xmlBaseModel.getItems().get(0).get("file_path"));
        String originId = String.valueOf(xmlBaseModel.getItems().get(0).get("patroldevice_code"));
        String content= String.valueOf(xmlBaseModel.getItems().get(0).get("content"));
        String alaramLevel= String.valueOf(xmlBaseModel.getItems().get(0).get("alarm_level"));

        Map<String,String> map =tStdDeviceMapper.selectInstanceInfo(Long.valueOf(originId));
        if(map==null){
            log.error("tStdDevice is null,edgeCode:{}, deviceId:{} ",sendCode,originId);
            return;
        }
        // 图片在ftps上的全路径
        String resultAbsolutePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content") + "/"+filePath;
        String targetPath = redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content") + "/"+filePath;
        try {
            FileUtil.copyFileUsingStream(resultAbsolutePath, targetPath);
        } catch (IOException e) {
            log.error("复制文件失败，resultAbsolutePath:{}  targetPath:{} ", resultAbsolutePath, targetPath, e);
            return ;
        }
        String defectResultRealImg = targetPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")),
                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg", "content")));
        TWarnInfo tWarnInfo = new TWarnInfo()
                .setWarnLevel(Integer.valueOf(AlarmLevelEnum.getAlarmLevelByProtocolCode(alaramLevel).getCode()))
                .setWarnTime(new Date())
                .setWarnName("静默监视告警数据")
                .setWarnContent(content)
                .setDeviceId(Long.valueOf(String.valueOf(map.get("device_id"))))
                .setCunstomId(String.valueOf(map.get("custom_id")))
                .setInstanceId(Long.valueOf(String.valueOf(map.get("instance_id"))))
                .setStdMeteId(Long.valueOf(String.valueOf(map.get("device_mete_id"))))
                .setConfMode(276)
                .setDefectModel(450)
                .setAlarmSource(689)
                .setImagePath(defectResultRealImg);
        tWarnInfoMapper.insert(tWarnInfo);
        sendUpSystem(xmlBaseModel);

    }

    private void sendUpSystem(XMLBaseModel xmlBaseModel) {
        try{
        //图片上传
        // 图片在ftps上的全路径
        String ftpPath = String.valueOf(xmlBaseModel.getItems().get(0).get("file_path"));
        String resultAbsolutePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content") + File.separator+ ftpPath;
        uploadFileToUpFtps(resultAbsolutePath,ftpPath);
        robotService.upToCruise(xmlBaseModel);
        }catch (Exception e){
            log.error("上传静默监视告警数据出错：",e);
        }
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath     源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            Map<String, String> upSystemFtps = redisTemplate.opsForHash().entries("upSystemFtps");
            String upSystemFtpsIp = upSystemFtps.get("upSystemFtpsIp");
            String upSystemFtpsPort = upSystemFtps.get("upSystemFtpsPort");
            String upSystemFtpsUsername = upSystemFtps.get("upSystemFtpsUsername");
            String upSystemFtpsPassword = upSystemFtps.get("upSystemFtpsPassword");
            FtpsUtil.putFile(sourcePath, targetPathName, upSystemFtpsIp, Integer.parseInt(upSystemFtpsPort),
                    upSystemFtpsUsername, upSystemFtpsPassword);
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误：", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.SILENT_MONITORING_ALARM.getCode(), this);
    }
}
