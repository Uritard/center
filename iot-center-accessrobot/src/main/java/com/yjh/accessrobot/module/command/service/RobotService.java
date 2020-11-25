package com.yjh.accessrobot.module.command.service;


import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.logs.Logs;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.dao.TRobotInspectionDao;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import com.yjh.accessrobot.module.command.entity.TRobotInspection;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
* @author tt
* @since 2020-08-20
*/
@Service
public class RobotService {

    private Logger log = LoggerFactory.getLogger(RobotService.class);

    private static long algorithmMsgId = 100000001;
    private static final String TIMEFORMATTPL = "yyyy-MM-dd HH:mm:ss";
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;

    @Logs(title = "巡视主机向机器人下发控制指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public String feignRobotControl(String type, String command){
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("巡视主机")
                .setReceiveCode("Client01")
                .setCode("省检018")
                .setTime(sdf.format(new Date()))
                .setType(type)
                .setCommand(command);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人控制xml是<start>" + xmlString + "<end>");
        RobotServerHandler sendId =  RobotServerHandler.getRobotServerHandlerMap().get("TT");
        try {
            sendId.SendHeartBeat(gongju(xmlString));
        }catch (NullPointerException e){
            log.error("机器人控制指令未能发送:", e);
        }
        return "success";
    }
    @Logs(title = "巡视主机向机器人下发模型同步指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public String feignRobotTransfer(){
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        sdf.format(new Date());
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("巡视主机")//巡视主机唯一标识
                .setReceiveCode("Client01")//机器人主机唯一标识
                .setCode("省检018")//机器人编码
                .setTime(sdf.format(new Date()))//时间
                .setType("61")//消息类型
                .setCommand("1");//命令
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人模型同步调用xml是<start>" + xmlString + "<end>");

        RobotServerHandler sendId =  RobotServerHandler.getRobotServerHandlerMap().get("TT");
        try {
            sendId.SendHeartBeat(gongju(xmlString));
        }catch (NullPointerException e){
            log.error("机器人模型同步调用指令未能发送:", e);
        }
        return "success";
    }
    //测试工具，用完就删
    public byte[] gongju(String xmlString){
        long sendSessionId = 0L;
        RobotServerHandler sendId =  RobotServerHandler.getRobotServerHandlerMap().get("TT");
        log.info("sendId是<start>"+sendId+"<end>");
        if(sendId != null){
            sendSessionId = sendSessionId + 1L;//请求报文每次累加1
        }
        log.info("发送会话序列号<start>"+sendSessionId+"<end>");
        byte[] requestProtocol = PlatformPacketUtil.createPacket(sendSessionId,0,true,xmlString);//生成发送的报文

        StringBuilder Str = new StringBuilder();
        for (byte byteitem : requestProtocol) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("发送给机器人的指令是<start>" + Str + "<end>");
        return requestProtocol;
    }
    @Logs(title = "机器人模型同步到数据库", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int robotFileIntoDB(List<Map<String,Object>> deviceMapList, List<Map<String,Object>> robotMap) {
        //机器人模型文件
        String robotPosition = null;
        if (robotMap.get(0).get("type").equals(1)){
            robotPosition = "156";
        }else if(robotMap.get(0).get("type").equals(2)) {
            robotPosition = "155";
        }
        String picPath = Constant.filePath+robotMap.get(0).get("mappath").toString();
        log.info("图片路径为："+picPath);
        /*
        * 将ftp图copy到40环境(还没搞！！！！！！！！)
        * */
        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotName(robotMap.get(0).get("robot_name").toString())
                .setRobotCode(robotMap.get(0).get("SendCode").toString())
                .setRobotFactory(robotMap.get(0).get("manufacturer").toString())
                .setRobotPosition(robotPosition)
                .setCreateDate(new Date())
                .setPhotePath(picPath)
                .setRobotStatus("on line")
                .setRemarks(robotMap.get(0).get("robot_info").toString());
        log.info("获得的tRobotInfo是："+tRobotInfo);
//        int res1 = tRobotInfoDao.insert(tRobotInfo);//机器人入库
//        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotMap.get(0).get("robot_code").toString());
        //设备模型文件
        List<TRobotInspection> list = new ArrayList<>();
        for (Map<String,Object> deviceMap : deviceMapList){
            TRobotInspection tRobotInspection = new TRobotInspection()
                    .setInspectionCode(deviceMap.get("device_id").toString())
//                    .setRobotId(robotId)
                    .setInspectionName(deviceMap.get("device_name").toString());
            list.add(tRobotInspection);
        }
        log.info("获得的list是："+list);
//        int res2 = tRobotInspectionDao.batchInsert(list);//测点入库
        return 1;
//        return res1+res2;
    }
    @Logs(title = "巡视主机向机器人下发任务指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int feignRobotTaskIssued(XMLBaseModel xmlBaseModel) {
//        if ("任务下发".equals("10")) {
            xmlBaseModel.setType("101");
            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("type", "1");//巡视类型
            map.put("task_code","12138");//任务编码
            map.put("task_name", "好牛的任务哦");//任务名称
            map.put("priority", "4");//优先级
            map.put("device_level", "3");//设备层级
            map.put("device_list", "0AD13EF0E24C43829F79F1BB51C3FB9E,1E43B10A5BAC47F984B89899B3DDF8C5");//设备列表
//            map.put("fixed_start_time", "定期开始时间");
//            map.put("cycle_month", "周期（月）");
//            map.put("cycle_week", "周期（周）");
//            map.put("cycle_execute_time", "周期（执行时间） ");
//            map.put("cycle_start_time", "周期开始时间 ");
//            map.put("cycle_end_time", "周期开始时间 ");
//            map.put("interval_number", "间隔（数量）");
//            map.put("interval_type", "间隔（类型） ");
//            map.put("interval_execute_time", "间隔（执行时间）");
//            map.put("interval_start_time", "间隔开始时间");
//            map.put("interval_end_time", "间隔结束时间 ");
//            map.put("invalid_start_time", "不可用开始时间 ");
//            map.put("invalid_end_time", "不可用结束时间");
//            map.put("isenable", "是否可用");
//            map.put("creator", "编制人");
//            map.put("create_time", "编制时间");
            mapList.add(map);
            log.info("任务下发的item是："+mapList);
            xmlBaseModel.setItems(mapList);
//        }else if ("联动任务".equals("10")){
//            xmlBaseModel.setType("102");
//            List<Map<String, Object>> mapList = new ArrayList<>();
//            Map<String, Object> map = new HashMap<>();
//            map.put("task_code", "任务编码");
//            map.put("task_name", "任务名称");
//            map.put("priority", "优先级");
//            map.put("device_level", "设备层级");
//            map.put("device_list", "设备列表");
//            mapList.add(map);
//            xmlBaseModel.setItems(mapList);
//        }
        xmlBaseModel.setSendCode("巡视主机");//巡视主机唯一标识
        xmlBaseModel.setReceiveCode("Client01");//机器人主机唯一标识
        xmlBaseModel.setCode("省检018");//机器人编码
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        sdf.format(new Date());
        xmlBaseModel.setTime(sdf.format(new Date()));//时间
        xmlBaseModel.setCommand("1");//任务配置
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人下发任务的xml是<start>" + xmlString + "<end>");
        RobotServerHandler sendId =  RobotServerHandler.getRobotServerHandlerMap().get("TT");
        try {
            sendId.SendHeartBeat(gongju(xmlString));
        }catch (NullPointerException e){
            log.error("机器人下发任务指令未能发送:", e);
        }
        return 1;
    }
    @Logs(title = "巡视主机向机器人下发任务控制指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int feignRobotTaskControl(String  commandValue,String taskId) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setType("41")
                .setSendCode("巡视主机")
                .setReceiveCode("Client01")
                .setCode(taskId)
                .setCommand(commandValue)
                .setTime(sdf.format(new Date()));
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的任务控制xml是<start>" + xmlString + "<end>");
        RobotServerHandler sendId =  RobotServerHandler.getRobotServerHandlerMap().get("TT");
        try {
            sendId.SendHeartBeat(gongju(xmlString));
        }catch (NullPointerException e){
            log.error("机器人下发任务控制指令未能发送:", e);
        }
        return 1;
    }

}

