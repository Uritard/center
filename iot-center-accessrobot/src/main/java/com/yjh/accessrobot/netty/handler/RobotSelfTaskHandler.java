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
public class RobotSelfTaskHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RobotService robotService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到机器人站端的任务了");
        // Deal with robot task data
        String robotCode = xmlBaseModel.getSendCode();
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");

        String taskFile = xmlBaseModel.getItems().get(0).get("task_file_path").toString();
        XMLBaseModel taskModel = getXmlMessage(filePathMap.get("content") + "/" + taskFile);
        List<Map<String, Object>> taskModelMapList = taskModel.getItems();
        log.info("taskModelItemsMap是：" + taskModelMapList);
        robotService.addRobotSelfTask(taskModelMapList, xmlBaseModel);

        String taskIntoDBXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] taskIntoDBProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, taskIntoDBXmlString);
        RobotServerHandler.send(taskIntoDBProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);
    }

    /**
     * 通过文件路径解析XML
     * */
    public static XMLBaseModel getXmlMessage(String filePathAndName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        return PlatformXMLUtil.readStringXmlOut(document);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_SELF_TASK.getCode(), this);
    }
}
