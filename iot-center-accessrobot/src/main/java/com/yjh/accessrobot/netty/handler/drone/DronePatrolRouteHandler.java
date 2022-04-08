package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.DroneService;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
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
public class DronePatrolRouteHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DroneService droneService;

    private String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机巡视路线数据了+++++++++++++++++");
        //Deal with drone operation data
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");

        String droneCode = xmlBaseModel.getSendCode();
        List<Map<String, String>> droneRoadList = new ArrayList<>();
        xmlBaseModel.getItems().forEach(res -> {
            Map<String, String> droneRoadMap = new HashMap<>(16);
            droneRoadMap.put("droneName", res.get("drone_name").toString());
            String filePath = res.get("file_path").toString();
            droneService.uploadFile(filePath, filePath);
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

            droneRoadMap.put("relativePath", developRelativeUrl + "/" + fileName);
            droneRoadMap.put("absolutePath", developAbsoluteUrl + "/" + fileName);
            droneRoadMap.put("droneCode", droneCode);
            droneRoadMap.put("time", res.get("time").toString());
            droneRoadMap.put("coordinatePixel", res.get("coordinate_pixel").toString());
            droneRoadMap.put("coordinateGeography", res.get("coordinate_geography").toString());
            droneRoadList.add(droneRoadMap);
        });

        for (int i = 0; i < droneRoadList.size(); i++) {
            redisTemplate.opsForHash().putAll("DroneRoad:" + droneCode, droneRoadList.get(i));
        }

        String roadXmlString = PlatformXMLUtil.generateXml2(RobotServerHandler.sendMessageForCommandThree(true, droneCode), PlatformXMLUtil.DRONEROOTNAME);
        byte[] roadProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, roadXmlString);
        RobotServerHandler.send(roadProtocol, droneCode);
        log.info("巡视主机给无人机{}响应了", droneCode);
        // 国网要求
        droneService.upToCruise(xmlBaseModel);

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
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_PATROL_ROUTE.getCode(), this);
    }
}
