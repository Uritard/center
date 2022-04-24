package com.yjh.accesstcp.thread;


import com.yjh.accesstcp.commons.logs.SpringBeanUtils;
import com.yjh.accesstcp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.dao.SendToUpSystemDao;
import com.yjh.accesstcp.module.device.entity.DeviceStatisticsInfo;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.list.AbstractLinkedList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class DeviceStatistics {

    @Resource
    private SendToUpSystemServices sendToUpSystemServices;

    @Resource
    private SendToUpSystemDao sendToUpSystemDao;

    @Value("${spring.union.stationCode}")
    private String stationCode;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String ROBOT_URL = "http://iot-center-platform/statistics/v1/robot?id={robotId}&type={type}";

    private static String CAMERA_URL = "http://iot-center-platform/statistics/v1/camera?id={cameraId}";

//    private static String VOICE_URL = "http://iot-center-platform/statistics/v1/voice?voice={voice}";

    @Scheduled(cron = "${spring.device.time}")
    public void deviceStatisticsUpload() {
        log.info("设备统计信息上传: "+new Date());
        //机器人
        List<DeviceStatisticsInfo> list = sendToUpSystemDao.selectRobot();
        list.forEach(robot->{
            List<Map<String,Object>> mapList = new ArrayList<>();
            HashMap<String,Object> paramMap = new HashMap<>();
            paramMap.put("robotId",robot.getId());
            paramMap.put("type","robot");
            Result re = deviceStatistics(paramMap,ROBOT_URL);
            if (re != null) {
                List<Map<String,Object>> reList = (List<Map<String,Object>>)re.getData();
                Map<String,Object> result =null;
                if(reList != null && reList.size()>0){
                    result = reList.get(0);
                }
                if(result != null && !result.isEmpty()){
                    for (int i = 1; i <= 5 ; i++) {
                        mapList.add(dealType(robot,result,i));
                        log.info("可靠性：{}",mapList);
                        sendToUpSystemServices.sendResponse("81","",stationCode,mapList);
                        mapList = new ArrayList<>();
                    }
                }
            }
        });
        //无人机
        list = sendToUpSystemDao.selectDrone();
        list.forEach(robot->{
            List<Map<String,Object>> mapList = new ArrayList<>();
            HashMap<String,Object> paramMap = new HashMap<>();
            paramMap.put("robotId",robot.getId());
            paramMap.put("type","drone");
            Result re = deviceStatistics(paramMap,ROBOT_URL);
            if (re != null) {
                List<Map<String,Object>> reList = (List<Map<String,Object>>)re.getData();
                Map<String,Object> result =null;
                if(reList != null && reList.size()>0){
                    result = reList.get(0);
                }
                if(result != null && !result.isEmpty()){
                    for (int i = 1; i <= 5 ; i++) {
                        mapList.add(dealType(robot,result,i));
                        log.info("可靠性：{}",mapList);
                        sendToUpSystemServices.sendResponse("81","",stationCode,mapList);
                        mapList = new ArrayList<>();
                    }
                }
            }
        });
        //摄像机
        list = sendToUpSystemDao.selectCamera();
        list.forEach(camera->{
            List<Map<String,Object>> mapList = new ArrayList<>();
            HashMap<String,Object> paramMap = new HashMap<>();
            paramMap.put("cameraId",camera.getId());
            Result re = deviceStatistics(paramMap,CAMERA_URL);
            if (re != null) {
                List<Map<String,Object>> reList = (List<Map<String,Object>>)re.getData();
                Map<String,Object> result =null;
                if(reList != null && reList.size()>0){
                    result = reList.get(0);
                }
                if(result != null && !result.isEmpty()){
                    for (int i = 4; i <= 6 ; i++) {
                        mapList.add(dealType(camera,result,i));
                        log.info("可靠性：{}",mapList);
                        sendToUpSystemServices.sendResponse("81","",stationCode,mapList);
                        mapList = new ArrayList<>();
                    }
                }
            }
        });
    }

    private Map<String,Object> dealType(DeviceStatisticsInfo device,Map<String,Object> result,Integer type){
        Map<String,Object> map = new HashMap<>();
        map.put("patroldevice_name",device.getPatrolDeviceName());
        map.put("patroldevice_code",device.getPatrolDeviceCode());
        map.put("commission_time",device.getCommissionTime());
        map.put("report_time",simpleDateFormat.format(new Date()));
        map.put("type",type.toString());
        switch (type){
            case 1:
                if(result.get("duration") != null && !"".equals(result.get("duration"))){
                    map.put("value",result.get("duration"));
                    map.put("unit","小时");
                }else {
                    map.put("value","0");
                    map.put("unit","小时");
                }
                break;
            case 2:
                if(result.get("offLineCount") != null && !"".equals(result.get("offLineCount"))){
                    map.put("value",result.get("offLineCount"));
                    map.put("unit","次");
                }else {
                    map.put("value","0");
                    map.put("unit","次");
                }
                break;
            case 3:
                if(result.get("runDay") != null && !"".equals(result.get("runDay"))){
                    map.put("value",result.get("runDay"));
                    map.put("unit","天");
                }else {
                    map.put("value","0");
                    map.put("unit","天");
                }
                break;
            case 4:
                if(result.get("normalDay") != null && !"".equals(result.get("normalDay"))){
                    map.put("value",result.get("normalDay"));
                    map.put("unit","天");
                }else {
                    map.put("value","0");
                    map.put("unit","天");
                }
                break;
            case 5:
                if(result.get("cruisePercent") != null && !"".equals(result.get("cruisePercent"))){
                    map.put("value",String.valueOf(result.get("cruisePercent")).replace("%",""));
                    map.put("unit","%");
                }else {
                    map.put("value","0");
                    map.put("unit","%");
                }
                break;
            case 6:
                if(result.get("intactPercent") != null && !"".equals(result.get("intactPercent"))){
                    map.put("value",String.valueOf(result.get("intactPercent")).replace("%",""));
                    map.put("unit","%");
                }else {
                    map.put("value","0");
                    map.put("unit","%");
                }
                break;
        }
        map.put("value_unit",map.get("value").toString()+map.get("unit").toString());
        return map;
    }

    private Result deviceStatistics(HashMap<String,Object> map,String url) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(url, Result.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }
}
