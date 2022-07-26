package com.yjh.accesstcp.module.device.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.CreateModeXMLUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.dao.SendToUpSystemDao;
import com.yjh.accesstcp.module.device.entity.MaintenanceModel;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2021/1/12
 */
@Service
@Slf4j
public class SendToUpSystemServices {

    @Value("${netty.server.port}")
    private int port;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${spring.union.stationCode}")
    private String stationCode;

    @Value("${spring.union.cruiseHost}")
    private String sendCode;
    @Value("${spring.union.upSystem}")
    private String recvCode;

    @Autowired
    private FtpsUtil ftpsUtil;

    @Autowired
    private SendToUpSystemDao sendToUpSystemDao;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Transactional(rollbackFor = Exception.class)
    public int sendResponse(long receiveSessionId, String type, String command, String code, List<Map<String,Object>> items){
        byte[] bytes = new byte[]{};
        long sendSessionId;
        TCPClientHandler tcpClientHandler = TCPClientHandler.getTCPClientHandlerHashMap().get(port);
        if(tcpClientHandler != null){
            sendSessionId = Constant.sendSessionId.incrementAndGet();
        }else {
            // 刷新sendSessionId
            log.error("服务未连接，请重试，port: {}", port);
            Constant.sendSessionId.set(0L);
            return -1;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(recvCode)
                .setType(type)
                .setCommand(command)
                .setCode(code)
                .setItems(items);
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("发送给上级系统的消息：{}", xml);
        bytes = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId,true, xml);
        tcpClientHandler.send(bytes);
        return 1;
    }

    public Map<String,Object> creatModel(String type){
        try{
            Map<String,Object> map = new HashMap<>();
            Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
//            String path = mapForPath.get("content")+"/"+stationCode+"/Model";
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "C:\\robotData\\Model":mapForPath.get("content")+"/"+stationCode+"/Model";
            log.info("模型文件路径："+path);

            switch (type){
                case "1":
                    //巡视主机模型
                    String hostModelTargetPath = String.format(stationCode + "/Model/host_model.xml");
                    ftpsUtil.putFile(createHostModel(path),hostModelTargetPath);
                    map.put("host_file_path",hostModelTargetPath);
                    break;
                case "2":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    ftpsUtil.putFile(createRobotModel(path),robotModelTargetPath);
                    map.put("robot_file_path",robotModelTargetPath);
                    break;
                case "3":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    ftpsUtil.putFile(createCameraModel(path),videoModelTargetPath);
                    map.put("video_file_path",videoModelTargetPath);
                    break;
                case "4":
                    //点位模型
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    ftpsUtil.putFile(createDeviceModel(path),deviceModelTargetPath);
                    map.put("device_file_path",deviceModelTargetPath);
                    break;
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    ftpsUtil.putFile(createDroneModel(path),droneModelTargetPath);
                    map.put("drone_file_path",droneModelTargetPath);
                    break;
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    ftpsUtil.putFile(createVoiceModel(path),voiceModelTargetPath);
                    map.put("voice_file_path",voiceModelTargetPath);
                    break;
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    ftpsUtil.putFile(createTaskModel(path),taskModelTargetPath);
                    map.put("task_file_path",taskModelTargetPath);
                    break;
                case "8":            //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    ftpsUtil.putFile(createMaintenanceModel(path),overhaulareaModelTargetPath);
                    map.put("overhaularea_file_path",overhaulareaModelTargetPath);
                    break;
                case "9":
                    String mapRealPath = sendToUpSystemDao.selectMapPath();
                    String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute").get("content"));
                    String filePathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative").get("content"));
                    String mapAbsPath = mapRealPath.replace(filePathMap,fileFtpPathMap);
                    String mapModelTargetPath = stationCode+mapRealPath.replace(filePathMap,"");
                    ftpsUtil.putFile(mapAbsPath,mapModelTargetPath);
                    map.put("map_file_path",mapModelTargetPath);
                    break;
                default:
                    break;
            }


            return map;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Async
    public void creatFile(String type){
        try{
            List<Map<String,Object>> list = new ArrayList<>();
            Map<String,Object> map = new HashMap<>();
            Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "C:\\robotData\\Model":mapForPath.get("content")+"/"+stationCode+"/Model";

            log.info("模型文件路径："+path);
            switch (type){
                case "1":
                    //点位模型
                    map.put("device_file_path",createDeviceModel(path));
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    ftpsUtil.putFile(createDeviceModel(path),deviceModelTargetPath);
                    map.put("device_file_path",deviceModelTargetPath);
                    break;
                case "2":
                    //巡视主机模型
                    String hostModelTargetPath = String.format(stationCode + "/Model/host_model.xml");
                    ftpsUtil.putFile(createHostModel(path),hostModelTargetPath);
                    map.put("host_file_path",hostModelTargetPath);
                    break;
                case "3":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    ftpsUtil.putFile(createRobotModel(path),robotModelTargetPath);
                    map.put("robot_file_path",robotModelTargetPath);
                    break;
                case "4":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    ftpsUtil.putFile(createCameraModel(path),videoModelTargetPath);
                    map.put("video_file_path",videoModelTargetPath);
                    break;
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    ftpsUtil.putFile(createDroneModel(path),droneModelTargetPath);
                    map.put("drone_file_path",droneModelTargetPath);
                    break;
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    ftpsUtil.putFile(createVoiceModel(path),voiceModelTargetPath);
                    map.put("voice_file_path",voiceModelTargetPath);
                    break;
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    ftpsUtil.putFile(createTaskModel(path),taskModelTargetPath);
                    map.put("task_file_path",taskModelTargetPath);
                    break;
                case "8":
                    //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    ftpsUtil.putFile(createMaintenanceModel(path),overhaulareaModelTargetPath);
                    map.put("overhaularea_file_path",overhaulareaModelTargetPath);
                    break;
                case "9":
                    String mapRealPath = sendToUpSystemDao.selectMapPath();
                    String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath").get("content"));
                    String filePathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative").get("content"));
                    String mapAbsPath = mapRealPath.replace(filePathMap,fileFtpPathMap);
                    String mapModelTargetPath = stationCode+mapRealPath.replace(filePathMap,"");
                    ftpsUtil.putFile(mapAbsPath,mapModelTargetPath);
                    map.put("map_file_path",mapModelTargetPath);
                    break;
                default:
                    break;
            }
            sendResponse(0L, "11","", stationCode, list);


        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int sendXML(XMLBaseModel xmlBaseModel){
        return this.sendResponse(0L, xmlBaseModel.getType(), xmlBaseModel.getCommand(), xmlBaseModel.getCode(), xmlBaseModel.getItems());
    }

    public String createDeviceModel(String path) throws Exception{
        List<Map<String,Object>> list = sendToUpSystemDao.selectDeviceModel();
        Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:stationName");
        String robotNumber = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:robotNumber", "content"));
        String droneNumber = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:droneNumber", "content"));
        list.forEach(item->{
            item.put("station_code",stationCode);
            item.put("station_name", getStationName());
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_code","");
            jsonObject.put("device_pos","");
            jsonObject.put("robot_code","");
            jsonObject.put("robot_pos","");
            jsonObject.put("uav_code","");
            jsonObject.put("uav_pos","");
            if (item.get("cruise_type").equals(228)){// 机器人
                item.put("save_type_list","jpg");
                item.put("data_type","01");
                jsonObject.put("robot_code", robotNumber);
                jsonObject.put("robot_pos",item.get("inspection_id"));
            } else if (item.get("cruise_type").equals(229) || item.get("cruise_type").equals(230)){//视屏 红外
                item.put("save_type_list","jpg");
                item.put("data_type","1");
                jsonObject.put("device_code",item.get("camera_id"));
                jsonObject.put("device_pos",item.get("preset_id"));
            }else if (item.get("cruise_type").equals(232)){//声纹
                item.put("save_type_list","wav");
                item.put("data_type","0001");
            }else if (item.get("cruise_type").equals(524)){// 无人机
                item.put("save_type_list","jpg");
                item.put("data_type","001");
                jsonObject.put("robot_code", droneNumber);
                jsonObject.put("robot_pos",item.get("inspection_id"));
            }
            item.remove("cruise_type");
            item.remove("camera_id");
            item.remove("preset_id");
            item.remove("robot_code");
            item.remove("inspection_id");
            jsonArray.add(jsonObject);
            item.put("video_pos",jsonArray.toJSONString());
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"device_model.xml","Device_Model");
    }

    private String getStationName(){
        Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:stationName");
        return mapForPath.get("content");
    }
    public String createRobotModel(String path) throws Exception{
        //机器人模型
        List<Map<String,Object>> list = sendToUpSystemDao.selectRobotInfo();
        list.forEach(item->{
            item.put("station_code",stationCode);
            item.put("station_name", getStationName());
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"robot_model.xml","PatrolDevice_Model");

    }
    public String createCameraModel(String path) throws Exception{
        //摄像机模型
        List<Map<String,Object>> list  = sendToUpSystemDao.selectCameraInfo();
        list.forEach(item->{
            item.put("station_code",stationCode);
            item.put("station_name", getStationName());
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"video_model.xml","PatrolDevice_Model");

    }
    public String createDroneModel(String path) throws Exception{
        //无人机模型
        List<Map<String,Object>> list = sendToUpSystemDao.selectDroneInfo();
        list.forEach(item->{
            item.put("station_code",stationCode);
            item.put("station_name", getStationName());
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"drone_model.xml","PatrolDevice_Model");

    }
    public String createVoiceModel(String path) throws Exception{
        //声纹模型
        List<Map<String,Object>> list  = sendToUpSystemDao.selectVoiceInfo();
        list.forEach(item->{
            item.put("station_code",stationCode);
            item.put("station_name", getStationName());
        });
        return CreateModeXMLUtil.createXmlFile(list,path,"voice_model.xml","PatrolDevice_Model");

    }
    public String createTaskModel(String path) throws Exception{
        //任务模型
        List<Map<String,Object>> list = sendToUpSystemDao.selectTaskInfo();
        //CronExpression expression;
        for (Map<String,Object> item:list) {
            String taskId = item.get("task_code").toString();
            List<Long> instanceIdList = sendToUpSystemDao.selectInstanceId(taskId);
            item.put("station_code",stationCode);
            item.put("station_name", getStationName());
            item.put("device_list",instanceIdList.toString().replaceFirst("\\[","").replace("]","").replace(" ",""));
//            if(!"".equals(item.get("time"))){
//                //计算 todo 内容不完整
//                String time = item.get("time").toString();
//                //SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss");
//                //String s = Pattern.compile("[^0-9]").matcher(str).replaceAll("");
//
//                    item.put("cycle_month", "");
//                    item.put("cycle_week","");
//                    item.put("cycle_execute_time","");
//
//                item.put("cycle_start_time",simpleDateFormat.format(new Date()));
//                item.put("cycle_end_time","2025-01-01 00:00:00");
//                item.put("interval_number","1");
//                item.put("interval_type","2");
//                item.put("interval_execute_time","00:00:00");
//                item.put("interval_start_time",simpleDateFormat.format(new Date()));
//                item.put("interval_end_time","2025-01-01 00:00:00");
//                item.put("station_name",stationName);
//                item.put("station_code",stationCode);
//                item.put("invalid_start_time","");
//                item.put("invalid_end_time","");
//            }
        }
        return CreateModeXMLUtil.createXmlFile(list,path,"task_model.xml","Task_Model");

    }
    public String createMaintenanceModel(String path) throws Exception{
        //检修区域模型
        List<MaintenanceModel> infoList = sendToUpSystemDao.selectMaintenanceInfo();
        List<Map<String, Object>> finalList = new ArrayList<>();
        infoList.forEach(item ->{
            Map<String,Object> maintenanceMap = new HashMap<>();
            maintenanceMap.put("station_code",stationCode);
            maintenanceMap.put("station_name", getStationName());
            maintenanceMap.put("config_code",item.getConfigCode());
            maintenanceMap.put("enable","1");
            maintenanceMap.put("start_time",item.getStartTime());
            maintenanceMap.put("end_time",item.getEndTime());
            maintenanceMap.put("device_level",item.getDeviceLevel());
            maintenanceMap.put("device_list",item.getDeviceIds().toString().replace("[","")
                    .replace("]","")
                    .replace(" ",""));
            maintenanceMap.put("coordinate_pixel",item.getColocoordinatePixel());
            finalList.add(maintenanceMap);
        });
        return CreateModeXMLUtil.createXmlFile(finalList,path,"overhaularea_model.xml","Effect_Config");


    }
    public String createHostModel(String path)throws Exception{
        //巡视主机模型
        List<Map<String, Object>> finalList = new ArrayList<>();
            Map<String, Object> host = new HashMap<>();
            host.put("station_name", getStationName());
            host.put("station_code",stationCode);
            host.put("patroldevice_name","巡视主机");
            host.put("patroldevice_code","YJH-001");
            host.put("device_model","YJH");
            host.put("manufacturer","亿嘉和");
            host.put("use_unit","亿嘉和");
            host.put("device_source","亿嘉和");
            host.put("production_date","2020-08-06 12:00:00");
            host.put("production_code","001");
            host.put("istransport","");
            host.put("use_mode","");
            host.put("video_mode","");
            host.put("place","");
            host.put("type","20");
            host.put("patroldevice_info","");
            host.put("robots_code","");
        finalList.add(host);
        return CreateModeXMLUtil.createXmlFile(finalList,path,"host_model.xml","PatrolDevice_Model");
    }
    public String createMapModel(){
        String mapRealPath = sendToUpSystemDao.selectMapPath();
        String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath").get("content"));
        String filePathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative").get("content"));
        String mapAbsPath = mapRealPath.replace(filePathMap,fileFtpPathMap);
        return mapAbsPath;
    }

    public List<Map<String,Object>> resultStatistical(String cmd,String startTime,String endTime){
        // 1 - 巡视任务执行闭环率 任务执行闭环率=闭环任务数量/执行任务总数*100% 任务正常的判据（自主启停）；断点续传任务认为是闭环任务
        // 2 - 巡视告警人工审核完成率 巡视告警人工审核完成率=（已审核告警数量/告警总数）*100%
        // 3 - 巡视告警准确率 告警准确率=（已审核未人工修正的告警数量/已审核告警总数）*100%
        // 4 - 巡视结果人工审核完成率 巡视结果人工审核完成率 =  （已审核巡检结果数量/总巡检结果数量）*100%
        // 5 - 巡视点位漏检率  漏检率=（漏检点位数量/巡视点位总数量）*100%
        List<Map<String,Object>> resultStatistical = new ArrayList<>();
        switch (cmd){
            case "1":
                resultStatistical.add(countTask(startTime,endTime));
                break;
            case "2":
                resultStatistical.add(countWarnCheck(startTime,endTime));
                break;
            case "3":
                resultStatistical.add(countWarnAccuracy(startTime,endTime));
                break;
            case "4":
                resultStatistical.add(countResultCheck(startTime,endTime));
                break;
            case "5":
                resultStatistical.add(countInstanceLoss(startTime,endTime));
                break;
            default:
                break;
        }
        return resultStatistical;
    }

    private HashMap<String,Object> dealCount(HashMap<String,Object> countMap){
        double totalNum = NumberUtils.toDouble(countMap.get("totalNum").toString());
        double validNum = NumberUtils.toDouble(countMap.get("validNum").toString());
        String percent = totalNum > 0 ? String.format("%.3f",validNum * 100 / totalNum) : "0.00";
        HashMap<String,Object> reMap = new HashMap<>();
        reMap.put("total_num",totalNum);
        reMap.put("valid_num",validNum);
        reMap.put("percent",percent+"%");
        return reMap;
    }
    public HashMap<String,Object>countTask(String startTime,String endTime){
        // 巡视任务闭环率
        return dealCount(sendToUpSystemDao.countTask(startTime,endTime));
    }
    public HashMap<String,Object>countWarnCheck(String startTime,String endTime){
        // 人工审核完成率
        return dealCount(sendToUpSystemDao.countWarnCheck(startTime,endTime));
    }
    public HashMap<String,Object>countWarnAccuracy(String startTime,String endTime){
        // 巡视告警准确率
        return dealCount(sendToUpSystemDao.countWarnAccuracy(startTime,endTime));
    }
    public HashMap<String,Object>countInstanceLoss(String startTime,String endTime){
        // 巡视点位漏检率
        return dealCount(sendToUpSystemDao.countInstanceLoss(startTime,endTime));
    }
    public HashMap<String,Object>countResultCheck(String startTime,String endTime){
        // 巡视结果人工审核完成率
        return dealCount(sendToUpSystemDao.countResultCheck(startTime,endTime));
    }



}
