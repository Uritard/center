package com.yjh.accesstcp.module.device.service;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.CreateModeXMLUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.dao.SendToUpSystemDao;
import com.yjh.accesstcp.module.device.entity.MaintenanceModel;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.apache.commons.collections.iterators.ObjectGraphIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2021/1/12
 */
@Service
public class SendToUpSystemServices {

    @Value("${netty.server.port}")
    private int port;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${spring.union.stationCode}")
    private String stationCode;

    @Autowired
    private SendToUpSystemDao sendToUpSystemDao;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Transactional(rollbackFor = Exception.class)
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
                .setSendCode(stationCode)
                .setReceiveCode(Constant.paramMap.get("sendCode"))
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
    public Map<String,Object> creatFile(){
        try{
            Map<String,Object> map = new HashMap<>();
            Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
//            String path = mapForPath.get("content")+"/"+stationCode+"/Model";
            String path = "C:\\robotData\\Model";

            //点位模型
            map.put("device_file_path",createDeviceModel(path));
            String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
            FtpsUtil.putFile(map.get("device_file_path").toString(),deviceModelTargetPath);

            //机器人模型
            map.put("robot_file_path",createRobotModel(path));
            String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
            FtpsUtil.putFile(map.get("robot_file_path").toString(),robotModelTargetPath);

            //摄像机模型
            map.put("video_file_path",createCameraModel(path));
            String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
            FtpsUtil.putFile(map.get("video_file_path").toString(),videoModelTargetPath);

            //巡视主机模型
//            Map<String, Object> host = new HashMap<>();
//            host.put("patroldevcie_name","巡视主机");
//            host.put("patroldevcie_code","");
//            host.put("device_model","");
//            host.put("manufacturer","");
//            host.put("use_unit","");
//            host.put("device_source","亿嘉和");
//            host.put("production_date","");
//            host.put("production_code","001");
//            host.put("istransport","");
//            host.put("use_mode","");
//            host.put("video_mode","");
//            host.put("place","");
//            host.put("type","20");
//            host.put("patroldevcie_info","");
//            host.put("robots_code","");
//            list = new ArrayList<>();
//            list.add(host);
//            map.put("robot_file_path",CreateModeXMLUtil.createXmlFile(list,path,"robot_model.xml","Robot_Model"));

            //无人机模型
            //声纹模型

            //任务模型

            //无人机模型

            //无人机
            map.put("drone_file_path",createDroneModel(path));
            String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
            FtpsUtil.putFile(map.get("drone_file_path").toString(),droneModelTargetPath);

            //声纹模型
            map.put("voice_file_path",createVoiceModel(path));
            String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
            FtpsUtil.putFile(map.get("voice_file_path").toString(),voiceModelTargetPath);

            //任务模型
            map.put("task_file_path",createTaskModel(path));
            String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
            FtpsUtil.putFile(map.get("task_file_path").toString(),taskModelTargetPath);

            //检修区域模型
            map.put("overhaularea_file_path",createMaintenanceModel(path));
            String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
            FtpsUtil.putFile(map.get("overhaularea_file_path").toString(),overhaulareaModelTargetPath);


            return map;
        }catch (Exception e) {
        e.printStackTrace();
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public int sendXML(XMLBaseModel xmlBaseModel){
        return this.sendResponse(xmlBaseModel.getType(),xmlBaseModel.getCommand(),xmlBaseModel.getCode(),xmlBaseModel.getItems());
    }

    public String createDeviceModel(String path) throws Exception{
        List<Map<String,Object>> list = sendToUpSystemDao.selectDeviceModel();
        return CreateModeXMLUtil.createXmlFile(list,path,"device_model.xml","Device_Model");
    }
    public String createRobotModel(String path) throws Exception{
        //机器人模型
        List<Map<String,Object>> list = sendToUpSystemDao.selectRobotInfo();
        list.forEach(item->{
            item.put("station_code",stationCode);
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"robot_model.xml","PatrolDevcie _Model");

    }
    public String createCameraModel(String path) throws Exception{
        //摄像机模型
        List<Map<String,Object>> list  = sendToUpSystemDao.selectCameraInfo();
        list.forEach(item->{
            item.put("station_code",stationCode);
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"video_model.xml","PatrolDevcie _Model");

    }
    public String createDroneModel(String path) throws Exception{
        //无人机模型
        List<Map<String,Object>> list = new ArrayList<>();
        list.forEach(item->{
            item.put("station_code",stationCode);
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"drone_model.xml","PatrolDevcie _Model");

    }
    public String createVoiceModel(String path) throws Exception{
        //声纹模型
        List<Map<String,Object>> list  = new ArrayList<>();
        list.forEach(item->{
            item.put("station_code",stationCode);
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"voice_model.xml","PatrolDevcie _Model");

    }
    public String createTaskModel(String path) throws Exception{
        //任务模型
        List<Map<String,Object>> list = sendToUpSystemDao.selectTaskInfo();
        //CronExpression expression;
        for (Map<String,Object> item:list) {
            String taskId = item.get("task_code").toString();
            List<Long> instanceIdList = sendToUpSystemDao.selectInstanceId(taskId);
            item.put("device_list",instanceIdList.toString().replaceFirst("\\[","").replace("]","").replace(" ",""));
            if(!"".equals(item.get("time"))){
                //计算 todo 内容不完整
                String time = item.get("time").toString();
                //SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss");
                //String s = Pattern.compile("[^0-9]").matcher(str).replaceAll("");

//                    item.put("cycle_month", CornUtil.translateToChinese(time,1));
//                    item.put("cycle_week",CornUtil.translateToChinese(time,2));
//                    item.put("cycle_execute_time",CornUtil.translateToChinese(time,4));

                item.put("cycle_start_time",simpleDateFormat.format(new Date()));
                item.put("cycle_end_time","2025-01-01 00:00:00");
                item.put("interval_number","1");
                item.put("interval_type","2");
                item.put("interval_execute_time","00:00:00");
                item.put("interval_start_time",simpleDateFormat.format(new Date()));
                item.put("interval_end_time","2025-01-01 00:00:00");
                if("1".equals(item.get("isenable"))){
                    item.put("invalid_start_time",simpleDateFormat.format(new Date()));
                    item.put("invalid_end_time",simpleDateFormat.format(new Date()));
                }


            }
        }
        return CreateModeXMLUtil.createXmlFile(list,path,"task_model.xml","Task_Model");

    }
    public String createMaintenanceModel(String path) throws Exception{
        //检修区域模型
        List<MaintenanceModel> infoList = sendToUpSystemDao.selectMaintenanceInfo();
        List<Map<String, Object>> finalList = new ArrayList<>();
        infoList.forEach(item ->{
            Map<String,Object> maintenanceMap = new HashMap<>();
            maintenanceMap.put("config_code",item.getConfigCode());
            maintenanceMap.put("enable",item.getEnable());
            maintenanceMap.put("start_time",item.getStartTime());
            maintenanceMap.put("end_time",item.getEndTime());
            maintenanceMap.put("device_level","3 ");
            maintenanceMap.put("device_list",item.getDeviceIds().toString().replace("[","").replace("]",""));
            finalList.add(maintenanceMap);
        });
        return CreateModeXMLUtil.createXmlFile(finalList,path,"overhaularea_model.xml","Robot_Model");


    }

    public List<Map<String,Object>> resultStatistical(String cmd,String startTime,String endTime){
        // 1 - 巡视任务执行闭环率 任务执行闭环率=闭环任务数量/执行任务总数*100% 任务正常的判据（自主启停）；断点续传任务认为是闭环任务
        // 2 - 巡视告警人工审核完成率 巡视告警人工审核完成率=（已审核告警数量/告警总数）*100%
        // 3 - 巡视告警准确率 告警准确率=（已审核未人工修正的告警数量/已审核告警总数）*100%4 - 巡视结果人工审核完成率
        // 5 - 巡视点位漏检率  漏检率=（漏检点位数量/巡视点位总数量）*100%
        return null;
    }
}
