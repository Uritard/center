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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Slf4j
@Service
public class DroneSelfTaskHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private DroneService droneService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到无人机站端的任务了");
        // Deal with drone task data
        String droneCode = xmlBaseModel.getSendCode();
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");

        String taskFile = xmlBaseModel.getItems().get(0).get("task_file_path").toString();
        XMLBaseModel taskModel = getXmlMessage(filePathMap.get("content") + "/" + taskFile);
        List<Map<String, Object>> taskModelMapList = taskModel.getItems();
        log.info("taskModelItemsMap是：" + taskModelMapList);
        droneService.addDroneSelfTask(taskModelMapList, xmlBaseModel);

        String taskIntoDBXmlString = PlatformXMLUtil.generateXml2(RobotServerHandler.sendMessageForCommandThree(true, droneCode), PlatformXMLUtil.DRONEROOTNAME);
        byte[] taskIntoDBProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, taskIntoDBXmlString);
        RobotServerHandler.send(taskIntoDBProtocol, droneCode);
        log.info("巡视主机给无人机{}响应了", droneCode);
    }

    /**
     * 通过文件路径解析XML
     */
    public static XMLBaseModel getXmlMessage(String filePathAndName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        return PlatformXMLUtil.readStringXmlOut(document);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_SELF_TASK.getCode(), this);
    }
}
