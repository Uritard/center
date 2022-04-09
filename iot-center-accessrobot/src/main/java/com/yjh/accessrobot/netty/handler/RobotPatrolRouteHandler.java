package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
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
    private RobotService robotService;

    private String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

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
            robotRoadMap.put("patrolDeviceName", res.get("patroldevice_name").toString());
            String filePath = res.get("file_path").toString();
            robotService.uploadFile(filePath, filePath);
            String[] splitArray = filePath.split("/");
            String fileName = splitArray[splitArray.length - 1];
            log.info("巡检路线图片名称==" + fileName);
            String taskId = splitArray[splitArray.length - 3];

            String temporaryPath = filePathMap.get("content") + "/" + filePath;
            log.info("temporaryPath是===" + temporaryPath);

            // 开发环境图片相对路径文件目录
            String developRelativeUrl = relativeImgMap.get("content") + "/" + todayTime + "/" + taskId + "/Road";
            // 开发环境图片绝对路径文件目录
            String developAbsoluteUrl = absoluteImgMap.get("content") + "/" + todayTime + "/" + taskId + "/Road";
            // 将ftp服务器上的文件复制到开发环境
            copyFileToDevelop(temporaryPath, developAbsoluteUrl);

            robotRoadMap.put("relativePath", developRelativeUrl + "/" + fileName);
            robotRoadMap.put("absolutePath", developAbsoluteUrl + "/" + fileName);
            robotRoadMap.put("patrolDeviceCode", res.get("patroldevice_code").toString());
            robotRoadMap.put("robotCode", robotCode);
            robotRoadMap.put("time", res.get("time").toString());
            robotRoadMap.put("coordinatePixel", res.get("coordinate_pixel").toString());
            robotRoadMap.put("coordinateGeography", res.get("coordinate_geography").toString());
            robotRoadList.add(robotRoadMap);
        });

        for (int i = 0; i < robotRoadList.size(); i++) {
            redisTemplate.opsForHash().putAll("RobotRoad:" + robotCode, robotRoadList.get(i));
        }

        String roadXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] roadProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, roadXmlString);
        RobotServerHandler.send( roadProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);
        // 国网要求
        robotService.upToCruise(xmlBaseModel);

    }

    /**
     * 将ftp服务器上的文件复制到开发环境
     */
    public static void copyFileToDevelop(String source,String aim){
        File ff = new File(aim);
        if (!ff.exists()){
            ff.setWritable(true, false);
            ff.mkdirs();
        }
        try {
            String url = "cp " + source + " "+aim;
            Runtime.getRuntime().exec(url);
        }catch (Exception e){
            e.getMessage();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_PATROL_ROUTE.getCode(), this);
    }
}
