package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.InspectionResultThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author hyh
 * @since 2022/2/8
 **/
@Slf4j
@Service
public class OperationResultHandler implements MessageHandlerStrategy, InitializingBean {

    @Value("${other.webSocketUrl}")
    private String websocketUrl;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到机器人操作结果了");
        String robotCode = xmlBaseModel.getSendCode();
        Map<String, String> operationResultMap = new HashMap<>();
        operationResultMap.put("robotCode", robotCode);
        operationResultMap.put("taskName", xmlBaseModel.getItems().get(0).get("task_name").toString());
        operationResultMap.put("taskCode", xmlBaseModel.getItems().get(0).get("task_code").toString());
        operationResultMap.put("deviceName", xmlBaseModel.getItems().get(0).get("device_name").toString());
        operationResultMap.put("deviceId", xmlBaseModel.getItems().get(0).get("device_id").toString());
        operationResultMap.put("value", xmlBaseModel.getItems().get(0).get("value").toString());
        operationResultMap.put("valueUnit", xmlBaseModel.getItems().get(0).get("value_unit").toString());
        operationResultMap.put("unit", xmlBaseModel.getItems().get(0).get("unit").toString());
        operationResultMap.put("time", xmlBaseModel.getItems().get(0).get("time").toString());
        operationResultMap.put("operationType", xmlBaseModel.getItems().get(0).get("operation_type").toString());
        operationResultMap.put("fileType", xmlBaseModel.getItems().get(0).get("file_type").toString());
        operationResultMap.put("rectangle", xmlBaseModel.getItems().get(0).get("rectangle").toString());
        operationResultMap.put("taskPatrolledId", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());

        String ftpFilePath = xmlBaseModel.getItems().get(0).get("file_path").toString();
        robotService.uploadFile(ftpFilePath, ftpFilePath);

        String confirmFilePath = xmlBaseModel.getItems().get(0).get("confirm_file_path").toString();
        robotService.uploadFile(confirmFilePath, confirmFilePath);

        //操作结果文件处理
        operationResultFileHandler(xmlBaseModel, ftpFilePath, confirmFilePath, operationResultMap);

        log.info("机器人操作结果数据是：{}", operationResultMap);
        // Start CruiseResultDealThread
        InspectionResultThread cruiseResultDealThread = new InspectionResultThread(operationResultMap, redisTemplate, websocketUrl, false);
        TaskExecutePool.getInstance().execute(cruiseResultDealThread);

        String cruiseResultXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, cruiseResultXmlString);
        RobotServerHandler.send(cruiseResultProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);
    }

    private void operationResultFileHandler(XMLBaseModel xmlBaseModel, String ftpFilePath, String confirmFilePath, Map<String, String> operationResultMap) {
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");

        String todayTime = DateTimeUtil.format(new Date(), "yyyy/MM/dd");

        String developAbsoluteUrl = absoluteImgMap.get("content") + "/" + todayTime + "/" + xmlBaseModel.getItems().get(0).get("task_code").toString() + "/";
        String developRelativeUrl = relativeImgMap.get("content") + "/" + todayTime + "/" + xmlBaseModel.getItems().get(0).get("task_code").toString() + "/";
        // 操作结果图片  可见光结果、红外fir、音频wav
        String[] sArray = ftpFilePath.split("/");
        String ftpFileName = sArray[sArray.length - 1];
        String temporaryFilePath = filePathMap.get("content") + "/" + ftpFilePath;
        log.info("temporaryFilePath==={}", temporaryFilePath);
        // 操作前确认图片
        String[] confirmArray = confirmFilePath.split("/");
        String confirmFtpFileName = confirmArray[confirmArray.length - 1];
        String confirmTemporaryFilePath = filePathMap.get("content") + "/" + confirmFilePath;
        log.info("confirmTemporaryFilePath==={}", confirmTemporaryFilePath);

        String fileType = xmlBaseModel.getItems().get(0).get("file_type").toString();
        // 1.红外（操作结果目前没有红外） 2.可见光 3.音频 4.视频
        if (Objects.equals("2", fileType)) {
            // 操作结果图
            copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "CCD");
            operationResultMap.put("relativePath", developRelativeUrl + "CCD" + "/" + ftpFileName);
            operationResultMap.put("absolutePath", developAbsoluteUrl + "CCD" + "/" + ftpFileName);
            // 操作前确认图
            copyFileToDevelop(confirmTemporaryFilePath, developAbsoluteUrl + "CCD");
            operationResultMap.put("confirmRelativePath", developRelativeUrl + "CCD" + "/" + confirmFtpFileName);
            operationResultMap.put("confirmAbsolutePath", developAbsoluteUrl + "CCD" + "/" + confirmFtpFileName);

        }else if (Objects.equals("4", fileType)) { //操作结果为视频时  只有结果
            // 拷贝巡视结果图
            copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "Video");
            operationResultMap.put("relativePath", developRelativeUrl + "Video" + "/" + ftpFileName);
            operationResultMap.put("absolutePath", developAbsoluteUrl + "Video" + "/" + ftpFileName);
            // 操作前确认图
            operationResultMap.put("confirmRelativePath", "");
            operationResultMap.put("confirmAbsolutePath", "");
        }
    }

    /**
     * 将ftp服务器上的文件复制到开发环境
     */
    public static void copyFileToDevelop(String source, String aim) {
        File ff = new File(aim);
        if (!ff.exists()) {
            ff.setWritable(true, false);
            ff.mkdirs();
        }
        try {
            String url = "cp " + source + " " + aim;
            Runtime.getRuntime().exec(url);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // MessageHandlerStrategyFactory.register(HandlerEnum.OPERATION_RESULT.getCode(), this);
    }
}
