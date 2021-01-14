package com.yjh.accesstcp.module.device.service;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.CreateModeXMLUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.dao.SendToUpSystemDao;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.ws.Action;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2021/1/12
 */
@Service
public class SendToUpSystemServices {

    @Value("${netty.server.port}")
    private int port;

    @Autowired
    private SendToUpSystemDao sendToUpSystemDao;

    @Transactional(rollbackFor = Exception.class)
    public int send(List<Map<String,Object>> list,String msgType){
        byte[] bytes = new byte[]{};
        long sendSessionId = Constant.sendSessionId;
        TCPClientHandler tcpClientHandler = TCPClientHandler.getTCPClientHandlerHashMap().get(port);
        if(tcpClientHandler != null){
            sendSessionId = sendSessionId + 1L;//请求报文每次累加1
            Constant.sendSessionId = sendSessionId;//刷新sendSessionId
        }else {
            Constant.sendSessionId = 0L;//刷新sendSessionId
            return -1;
        }
        String type = "";
        if("abnormalWarn".equals(msgType)){//异常告警
            type = "5";
        }
        if("device".equals(msgType)){//设备运行状态
            type = "2";
        }
        if("weather".equals(msgType)){//微气象
            type = "21";
        }
        if("cruiseResult".equals(msgType)){//巡视结果
            type = "61";
        }
        if("warn".equals(msgType)){//告警
            type = "62";
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("Client02")
                .setReceiveCode("Server02")
                .setType(type)
                .setItems(list);
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
        bytes = PlatformPacketUtil.createPacket(sendSessionId,Constant.receiveSessionId,true,xml);
        tcpClientHandler.send(bytes);
        return 1;
    }

    public int sendResponse(String type,String command,String code,List<Map<String,Object>> items){
        byte[] bytes = new byte[]{};
        long sendSessionId = Constant.sendSessionId;
        TCPClientHandler tcpClientHandler = TCPClientHandler.getTCPClientHandlerHashMap().get(port);
        if(tcpClientHandler != null){
            sendSessionId = sendSessionId + 1L;//请求报文每次累加1
            Constant.sendSessionId = sendSessionId;//刷新sendSessionId
        }else {
            Constant.sendSessionId = 0L;//刷新sendSessionId
            return -1;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("Client02")
                .setReceiveCode("Server02")
                .setType(type)
                .setCommand(command)
                .setCode(code)
                .setItems(items);
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
        bytes = PlatformPacketUtil.createPacket(sendSessionId,Constant.receiveSessionId,true,xml);
        tcpClientHandler.send(bytes);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public String creatFile(String what){
        try{
            Map<String,Object> map = new HashMap<>();
            List<Map<String,Object>> list = sendToUpSystemDao.selectDeviceModel();
            map.put("device_file_path",CreateModeXMLUtil.createXmlFile(list,"D:/code/qhTest/66666","device.xml"));
            list = sendToUpSystemDao.selectRobotInfo();
            map.put("robot_file_path",CreateModeXMLUtil.createXmlFile(list,"D:/code/qhTest/66666","robot.xml"));
            return CreateModeXMLUtil.createXmlFile(list,"D:/code/qhTest/66666","device.xml");
        }catch (Exception e) {
        e.printStackTrace();
        }
        return "fail";
    }
}
