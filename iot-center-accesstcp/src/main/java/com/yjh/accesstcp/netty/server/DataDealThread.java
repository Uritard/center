package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.Object2Map;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.commons.logs.SpringBeanUtils;
import com.yjh.accesstcp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.TCruiseTaskAdd;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.thread.TaskExecutePool;
import com.yjh.accesstcp.thread.WeatherThread;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accesstcp.common.Constant.Packet;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private byte[] data;
    private TCPClientHandler tcpClientHandler;
    private RedisTemplate redisTemplate;
    private SendToUpSystemServices sendToUpSystemServices;

    public DataDealThread(byte[] data, TCPClientHandler tcpClientHandler, RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
        this.data =data;
        this.redisTemplate = redisTemplate;
        this.tcpClientHandler = tcpClientHandler;
        this.sendToUpSystemServices = sendToUpSystemServices;
    }

    @Override
    public void run() {
        try{
        StringBuilder dataByte = new StringBuilder();
        for (byte byteitem : data) {
            dataByte.append(String.format("%02x ", byteitem));
        }
        log.info("我在处理数据-字节"+dataByte);

        String dataString = new String(data, "UTF-8");
        log.info("我在处理数据-字符串"+dataString);
        //处理毡包问题
        String zzbds ="^.*<?xml.*";
        if (!Packet.matches(zzbds)){
            Packet = "";
        }

        String temporaryBody = Packet + dataString ;//临时
        String temporaryBody2 = temporaryBody.replace("\"UTF-8\"","\'UTF-8\'");//临时
        String finalBody = temporaryBody2.replace("\"1.0\"","\'1.0\'");//最终的body
        handlingMethod(data,finalBody);
        }catch (Exception e){
            log.info("解析报文出错："+e.getMessage());
        }
    }
    //拆包 解决毡包
    private void handlingMethod(byte[] bytes,String parameter)throws Exception {

        String hua = null;

        if (parameter.contains("开始") || parameter.contains("结束")) {
            hua = parameter;
        } else {
            String bian = parameter.replace("<?xml version='1.0' encoding='UTF-8'?>", "开始<?xml version='1.0' encoding='UTF-8'?>");
            hua = bian.replace("</PatrolHost>", "</PatrolHost>结束");
        }
        Pattern pattern1 = Pattern.compile("(\\<\\?xml version='1.0' encoding='UTF-8'?[^>])([\\s\\S]*?)(</PatrolHost>)");
        Matcher matcher1 = pattern1.matcher(hua);
        String wanZheng = null;
        String shengYu = null;
        if (matcher1.find()) {
            wanZheng = matcher1.group();
            log.info("匹配到一个完整包的内容=" + wanZheng);
            stringToXml(bytes, wanZheng);
            shengYu = parameter.replace(wanZheng, "");
            handlingMethod(bytes, shengYu);
        } else {
            log.info("不完整啊，小老弟");
            Packet = parameter;
//                log.info("不足一个完整的包：" + Packet);
        }

    }
    private void stringToXml(byte[] bytes,String xmlContext)throws Exception{

        Document document = DocumentHelper.parseText(xmlContext);//String转XML
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml
        log.info("解析出来的xml是：" + xmlRes);
        byte[] sendSessionIdByte = new byte[8];//发送会话序列号Byte
        byte[] receiveSessionIdByte = new byte[8];//接收会话序列号Byte
        System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
        System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
        long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
        Constant.receiveSessionId = sendSessionId;
        long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号
        if (xmlRes.getSendCode() == null) {
            log.info("连接可能断了，等待重连.....");
        } else {
            doProcessMessage(xmlRes, sendSessionId, receiveSessionId);
        }

    }
    private void doProcessMessage(XMLBaseModel xmlBaseModel,long sendSessionId,long receiveSessionId) throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //解析的xml文件
        if ("251".equals(xmlBaseModel.getType())) {//系统消息
            if ("4".equals(xmlBaseModel.getCommand())) {//响应注册
                log.info("---注册响应---");
                if ("100".equals(xmlBaseModel.getCode())) {
                    //需要重发注册消息
                    tcpClientHandler.sendRegister();
                }        else if ("200".equals(xmlBaseModel.getCode())) {
                    //服务端响应 我方开启心跳

                    List<Map<String, Object>> items = xmlBaseModel.getItems();
                    if (items == null || items.size() == 0) {
                        return;
                    }
                    for (Map<String, Object> item : items) {
                        if(item.get("heart_beat_interval") != null){
                            Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());//心跳间隔
                        }
                        if(item.get("patroldevice_run_interval") != null){
                            Constant.paramMap.put("patroldevice_run_interval", item.get("patroldevice_run_interval").toString());//巡视设备运行数据间隔间隔
                        }
                        if(item.get("weather_interval") != null){
                            Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());//微气象数据间隔
                        }
                    }
                    //将数据放入redis 做个保存
                    redisTemplate.opsForHash().putAll("upSystemParameter",Constant.paramMap);
                    //todo 记得做 启动响应线程处理响应业务
                    //心跳线程发心跳
                    HeartBeatThead heartBeatThead = new HeartBeatThead(tcpClientHandler, true);
                    //天气线程发天气
                    WeatherThread weatherThread = new WeatherThread(tcpClientHandler,redisTemplate,true,sendToUpSystemServices);

                    TaskExecutePool.getInstance().execute(heartBeatThead);
                    TaskExecutePool.getInstance().execute(weatherThread);

                } else {
                    return;
                }

            }

            if ("3".equals(xmlBaseModel.getCommand())) {//响应心跳
                log.info("--心跳响应--");
                List<Map<String, Object>> items = xmlBaseModel.getItems();
                if (items == null || items.size() == 0) {
                    return;
                }
                for (Map<String, Object> item : items) {
                    if(item.get("heart_beat_interval") != null){
                        Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());//心跳间隔
                    }
                    if(item.get("patroldevice_run_interval") != null){
                        Constant.paramMap.put("patroldevice_run_interval", item.get("patroldevice_run_interval").toString());//巡视设备运行数据间隔间隔
                    }
                    if(item.get("weather_interval") != null){
                        Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());//微气象数据间隔
                    }
                }
                //将数据放入redis 做个保存
                redisTemplate.opsForHash().putAll("upSystemParameter",Constant.paramMap);
            }
        }
        if("1".equals(xmlBaseModel.getType()) || "2".equals(xmlBaseModel.getType()) || "3".equals(xmlBaseModel.getType())
          || "4".equals(xmlBaseModel.getType()) || "21".equals(xmlBaseModel.getType()) || "22".equals(xmlBaseModel.getType())){//控制消息
            log.info("--响应控制 控制下发--");
            Map<String,List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list",list);
            Result re = Constant.otherServer(robotMap,Constant.ROBOT_TASK_URL);
            if(re == null){
                sendToUpSystemServices.sendResponse("251","3","100",null);
            }else if("success".equals(re.getData().toString())){
                sendToUpSystemServices.sendResponse("251","3","200",null);
            }else {
                sendToUpSystemServices.sendResponse("251","3","500",null);
            }
            log.info("==控制响应==");
        }
        if ("41".equals(xmlBaseModel.getType())){
            log.info("--响应任务控制--");
            Result re = new Result();
            Map<String,List<XMLBaseModel>> map = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            map.put("list",list);
            re = Constant.otherServer(map,Constant.TASK_STATE_URL);
            List<Map<String,Object>> items = new ArrayList<>();
            Map<String,Object> item = new HashMap<>();
            item.put("task_patrolled_id",xmlBaseModel.getCode());
            items.add(item);
            if(re == null){
                sendToUpSystemServices.sendResponse("251","4","100",items);
            }else if("1".equals(re.getData().toString())){
                sendToUpSystemServices.sendResponse("251","4","200",items);
            }else {
                sendToUpSystemServices.sendResponse("251","4","500",items);
            }
            log.info("==任务控制响应==");


        }

        if ("101".equals(xmlBaseModel.getType())){
            log.info("--响应任务--");
            if("1".equals(xmlBaseModel.getCommand())){
                log.info("--响应任务 任务下发--");
                List<Map<String,Object>> list = xmlBaseModel.getItems();
                for (Map<String,Object> item:list){
                    //todo 下任务
                    TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
                    String type = item.get("type").toString();
                    String device_list = item.get("device_list").toString();
                    tCruiseTaskAdd.setDeviceList(device_list);
                    if(type != null && "1".equals(type)){
                        type = "214";
                    }
                    if(type != null && "2".equals(type)){
                        type = "216";
                    }
                    if(type != null && "3".equals(type)){
                        type = "217";
                    }
                    if(type != null && "4".equals(type)){
                        type = "218";
                    }
                    tCruiseTaskAdd.setType(Integer.valueOf(type));
                    tCruiseTaskAdd.setTaskId(item.get("task_code").toString());
                    tCruiseTaskAdd.setTaskName(item.get("task_name").toString());
                    tCruiseTaskAdd.setTaskLevel(Integer.valueOf(item.get("priority").toString()));
                    if(item.get("fixed_start_time") != null || "".equals(item.get("fixed_start_time"))){
                        tCruiseTaskAdd.setIfRun(172);
                        StringBuilder stringBuilder = new StringBuilder("0 0");
                        String interval_type = item.get("interval_type").toString();
                        if("1".equals(interval_type)){
                            String interval_number = item.get("interval_number").toString();
                            stringBuilder.append(" 0/"+interval_number+" ?");
                        }
                        if("2".equals(interval_type)){
                            String interval_number = item.get("interval_number").toString();
                            stringBuilder.append(" 0 0/"+interval_number+" ?");
                        }
                        String cycle_month = item.get("cycle_month").toString();
                        if("".equals(cycle_month) || null == cycle_month){
                            stringBuilder.append(" ?");
                        }else {
                            stringBuilder.append(" "+cycle_month);
                        }
                        String cycle_week = item.get("cycle_week").toString();
                        if("".equals(cycle_week) || null == cycle_week){
                            stringBuilder.append(" ?");
                        }else {
                            stringBuilder.append(" "+cycle_week);
                        }
                        tCruiseTaskAdd.setDateType(stringBuilder.toString());
                    }else {
                        tCruiseTaskAdd.setIfRun(174);
                    }
                    tCruiseTaskAdd.setStartTime(simpleDateFormat.parse(item.get("fixed_start_time").toString()));

                    Map<String,List<TCruiseTaskAdd>> map = new HashMap<>();
                    List<TCruiseTaskAdd> taskList = new ArrayList<>();
                    taskList.add(tCruiseTaskAdd);
                    map.put("list",taskList);
                    Result re = Constant.otherServer(map,Constant.TASK_ISSUE_URL);
                    if(re == null){
                    sendToUpSystemServices.sendResponse("251","3","500",null);
                    }else if("ok".equals(re.getData())){
                        sendToUpSystemServices.sendResponse("251","3","200",null);
                    }else {
                        sendToUpSystemServices.sendResponse("251","3","100",null);
                    }
                    log.info("--响应任务下发--");
                }

            }
        }

        if ("102".equals(xmlBaseModel.getType())) {
            log.info("--响应联动任务--");
            if("1".equals(xmlBaseModel.getCommand())){
                //联动任务
                List<Map<String,Object>> list = xmlBaseModel.getItems();
                for (Map<String,Object> item:list){
                    TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
                    String taskId = item.get("task_code").toString()+simpleDateFormat.format(new Date());
                    tCruiseTaskAdd.setTaskId(taskId);
                    String taskName = item.get("task_name").toString();
                    tCruiseTaskAdd.setTaskName(taskName);
                    String taskLevel = item.get("priority").toString();
                    tCruiseTaskAdd.setTaskLevel(Integer.valueOf(taskLevel));
                    String deviceList = item.get("device_list").toString();
                    tCruiseTaskAdd.setDeviceList(deviceList);

                    Map<String,List<TCruiseTaskAdd>> map = new HashMap<>();
                    List<TCruiseTaskAdd> listTask = new ArrayList<>();
                    listTask.add(tCruiseTaskAdd);
                    map.put("list",listTask);
                    Result re = Constant.otherServer(map,Constant.TASK_ISSUE_URL);
                    List<Map<String,Object>> xmlItems = new ArrayList<>();
                    Map<String,Object> xmlItem = new HashMap<>();
                    xmlItem.put("task_patrolled_id",taskId);
                    if(re == null){
                        xmlItem.put("error_code","3");
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse("251","4","100",xmlItems);
                    }else if("ok".equals(re.getData())){
                        xmlItem.put("error_code","0");
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse("251","4","200",xmlItems);
                    }else {
                        xmlItem.put("error_code","1");
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse("251","4","500",xmlItems);
                    }

                }
            }
        }

        if ("61".equals(xmlBaseModel.getType())){
            log.info("--模型同步--");
            if("1".equals(xmlBaseModel.getCommand())){
                //log.info("----");
            sendToUpSystemServices.creatFile();
            List<Map<String,Object>> list =new ArrayList<>();
            Map<String,String> mapForGetPath = redisTemplate.opsForHash().entries("t_sys_param:modelRelativePath");
            String path = mapForGetPath.get("content");
            Map<String,Object> mapForDevice = new HashMap<>();
            mapForDevice.put("device_file_path",path+"/"+"device_file.xml");
            list.add(mapForDevice);

            Map<String,Object> mapForRobot = new HashMap<>();
            mapForDevice.put("robot_file_path",path+"/"+"robot_file.xml");
            list.add(mapForRobot);

            Map<String,Object> mapForTask = new HashMap<>();
            mapForDevice.put("task_file_path",path+"/"+"task_file.xml");
            list.add(mapForTask);

            sendToUpSystemServices.sendResponse("251","3","200",list);

            }
        }

        if ("81".equals(xmlBaseModel.getType())){
            log.info("--检修区域--");
            Map<String,List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list",list);
            Result re = Constant.otherServer(robotMap,Constant.MAINTENANCE_URL);
            log.info("--响应检修--");
            sendToUpSystemServices.sendResponse("251","4","200",null);
        }

    }


}
