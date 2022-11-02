package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.configuration.UpFtpsConfig;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
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

import java.io.File;
import java.util.*;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class RobotPatrolRouteHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UpFtpsConfig upFtpsConfig;
    @Autowired
    private RobotService robotService;

    // private String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到机器人巡视路线数据了+++++++++++++++++");
        //Deal with robot operation data
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");

        String robotCode = xmlBaseModel.getSendCode();
        List<Map<String, String>> robotRoadList = new ArrayList<>();
        xmlBaseModel.getItems().forEach(res -> {
            Map<String, String> robotRoadMap = new HashMap<>(16);
            // 2022过检 robot_name -> patroldevice_name
            robotRoadMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
            robotRoadMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
            if (res.containsKey("file_path")) {
                String filePath = String.valueOf(res.get("file_path"));
                robotService.uploadFile(filePath, filePath);
                String[] splitArray = filePath.split("/");
                String fileName = splitArray[splitArray.length - 1];
                log.info("巡检路线图片名称==" + fileName);
                //机器人上报的是taskCode,要转成taskId
                String taskCode = splitArray[splitArray.length - 3];
                // 通过机器人上报的任务id查询巡视主机上的任务id
                String taskId = robotService.selectRealTaskId(taskCode);
                if (StringUtils.isEmpty(taskId)) {
                    taskId = taskCode;
                    log.info("taskId is empty, use taskCode as taskId");
                }
                log.info("taskCode==={},taskId===={}", taskCode, taskId);
                String temporaryPath = filePathMap.get("content") + "/" + filePath;
                log.info("temporaryPath是===" + temporaryPath);

                String todayTime = DateTimeUtil.format(new Date(), "yyyy/MM/dd");
                // 开发环境图片相对路径文件目录
                String developRelativeUrl = relativeImgMap.get("content") + "/" + todayTime + "/" + taskId + "/Road";
                // 开发环境图片绝对路径文件目录
                String developAbsoluteUrl = absoluteImgMap.get("content") + "/" + todayTime + "/" + taskId + "/Road";
                // 将ftp服务器上的文件复制到开发环境
                copyFileToDevelop(temporaryPath, developAbsoluteUrl);

                robotRoadMap.put("relativePath", developRelativeUrl + "/" + fileName);
                robotRoadMap.put("absolutePath", developAbsoluteUrl + "/" + fileName);
            } else {
                robotRoadMap.put("relativePath", "");
                robotRoadMap.put("absolutePath", "");
            }
            robotRoadMap.put("robotCode", robotCode);
            robotRoadMap.put("time", String.valueOf(res.get("time")));
            robotRoadMap.put("coordinatePixel", String.valueOf(res.get("coordinate_pixel")));
            robotRoadMap.put("coordinateGeography", String.valueOf(res.get("coordinate_geography")));
            robotRoadList.add(robotRoadMap);
        });

        for (int i = 0; i < robotRoadList.size(); i++) {
            redisTemplate.opsForHash().putAll("RobotRoad:" + robotCode, robotRoadList.get(i));
        }

        String roadXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] roadProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, roadXmlString);
        RobotServerHandler.send(roadProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);
        // 国网要求
//        robotService.upToCruise(xmlBaseModel);

        roadToUpSystem(xmlBaseModel);
    }

    /**
     * 巡视路线上报上级系统
     *
     * @param xmlBaseModel xml格式的内容
     */
    private void roadToUpSystem(XMLBaseModel xmlBaseModel) {
        try {
            Map<String, Object> item = xmlBaseModel.getItems().get(0);
            // 这是机器人放在巡视主机ftps服务下的路径
            String value = String.valueOf(item.get("file_path"));
            String tagPath = "robotTask/" + value;
            log.info("tagPath==={}", tagPath);
            String imgPath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content") + "/" + item.get("file_path");
            uploadFileToUpFtps(imgPath, "/" + tagPath, upFtpsConfig);
            xmlBaseModel.getItems().get(0).put("file_path", tagPath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("准备上报上级系统的机器人巡视路线是==={}", xmlBaseModel);
        robotService.upToCruise(xmlBaseModel);
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath     源文件地址
     * @param targetPathName 目标文件地址名称
     */
    @Async
    public void uploadFileToUpFtps(String sourcePath, String targetPathName, UpFtpsConfig upFtpsConfig) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误:{}", e);
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
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_PATROL_ROUTE.getCode(), this);
    }
}
