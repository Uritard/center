package com.yjh.platform.module.device.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.device.controller.TRobotInspectionController;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCruiseResult;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-08-08
 */
@Service
public class TRobotInspectionService{

    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private TCruiseResultDao tCruiseResultDao;

    private Logger log = LoggerFactory.getLogger(TRobotInspectionService.class);



    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.insert(tRobotInspection);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.deleteByPrimaryId(inspectionId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.update(tRobotInspection);
    }


    @Transactional(rollbackFor = Exception.class)
    public TRobotInspection selectByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.selectByPrimaryId(inspectionId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> select(Long inspectionId, String inspectionCode ,Long robotId, String inspectionName,
                                         String componentId,Integer meterType,Integer appearanceType,String saveTypeList,
                                         String recognitionTypeList,String phase,String deviceInfo) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.select(inspectionId, inspectionCode, robotId, inspectionName,
                componentId,meterType,appearanceType,saveTypeList,
                recognitionTypeList,phase,deviceInfo);
        return tRobotInspectionList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> selectByPage(TRobotInspection tRobotInspection) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.selectByPage(tRobotInspection);
        return tRobotInspectionList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInspection> list) {
        return this.tRobotInspectionDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<RobotTaskMessage> selectRobotTaskMessage(String taskId,Long robotId) throws Exception{
        List<RobotTaskMessage> re = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取此机器人的巡视点
        String robotCode = tRobotInspectionDao.selectRobotCode(robotId);
        Map<String,Object> mapForRobotInstance = redisTemplate.opsForHash().entries("RobotTaskStatus:"+robotCode+":"+taskId);
        Map<String,Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":41");
        //String taskId = (String)mapForRobotInstance.get("taskId");
        String robotState =(String) mapForRobotState.get("value");
        if( !"2".equals(robotState)){
            //机器人未在做任务
//            Map<String,String> jasonMap=new HashMap<>();
//            jasonMap.put("type","noTask");
//            //jasonMap.put("taskId",tCruiseTask.getTaskId());
//            String json= JSON.toJSONString(jasonMap);
//            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
//            log.info("发送给前端的消息-停止调接口：   "+json);
            return  null;
        }
        String instanceList = (String)mapForRobotInstance.get("instanceIdList");
        if(instanceList == null){
            return null;
        }
        instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
        String[] instanceIdList = instanceList.split(", ");
        //List<TCruisePointAttr> nameList =  tRobotInspectionDao.selectRobotTaskMessage(instanceIdList);
        for (String item:instanceIdList) {
            //获取任务数据
            Map<String,String> mapForRobotTaskMessage = redisTemplate.opsForHash().entries("t_cruise_task_result:"+taskId+":"+item);
            RobotTaskMessage robotTaskMessage = new RobotTaskMessage();
            robotTaskMessage.setDeviceName(mapForRobotTaskMessage.get("deviceName"));
            robotTaskMessage.setInstanceName(mapForRobotTaskMessage.get("instanceName"));
            if(mapForRobotTaskMessage.get("cruiseTime") != null && !"null".equals(mapForRobotTaskMessage.get("cruiseTime"))){
                robotTaskMessage.setCruiseTime(mapForRobotTaskMessage.get("cruiseTime"));
                robotTaskMessage.setResult(mapForRobotTaskMessage.get("resultNum"));
            }else {
                robotTaskMessage.setCruiseTime("");
                robotTaskMessage.setResult("");
            }
            re.add(robotTaskMessage);
        }
        return re;
    }

    private String getName(Long instanceId,int flag,List<TCruisePointAttr> nameList){
        for (TCruisePointAttr item:nameList) {
            if(instanceId.equals(item.getInstanceId())){
                if(flag == 0){//巡检点名称
                    return item.getInstanceName();
                }
                if(flag == 1){//设备名称
                    return item.getAttrName();
                }
            }
        }
        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<Robot> selectRobotInfo(){
        return this.tRobotInspectionDao.selectRobotInfo();
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectRobotStatus(String robotCode){
        Map<String,Object> re = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00");
        //获取状态信息
        Map<String,Object> mapForCell  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":3");
        if(mapForCell.size() != 0){
            re.put("batteryLevel",mapForCell.get("valueUnit"));//电池电量
        }else {
            re.put("batteryLevel","");//电池电量
        }

        Map<String,Object> mapForOnlineState  = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
        if(mapForOnlineState.size() != 0){
            re.put("onlineState",mapForOnlineState.get("value"));//网络状态
            //todo 通知前端停止调接口
        }else {
            re.put("onlineState","");//网络状态
        }

        Map<String,Object> mapForRobotCoordinate  = redisTemplate.opsForHash().entries("RobotCoordinate:"+robotCode);
        if(mapForRobotCoordinate.size() != 0){
            re.put("robotCoordinate",mapForRobotCoordinate.get("coordinatePixel"));//机器人坐标
        }else {
            re.put("robotCoordinate","0,0,0,0");//机器人坐标
        }
        Map<String,Object> mapForMileage  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":2");
        if(mapForMileage.size() != 0){
            re.put("mileage",mapForMileage.get("valueUnit"));//里程
        }else {
            re.put("mileage","");//里程
        }

        Map<String,String> mapForSpeed  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":1");
        if(mapForSpeed.size() != 0){
            Double speed =Double.valueOf(mapForSpeed.get("value")) ;
            re.put("speed",df.format(speed)+mapForSpeed.get("unit"));//速度
        }else {
            re.put("speed","");//速度
        }

        Map<String,Object> mapForCruiseMap  = redisTemplate.opsForHash().entries("RobotRoad:"+robotCode);
        if(mapForCruiseMap.size() != 0){
            re.put("cruiseMapPath",mapForCruiseMap.get("relativePath"));//巡视路径地图路径
        }else {
            re.put("cruiseMapPath","");//巡视路径地图路径
        }

        Map<String,Object> mapForRobotState  = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":41");
        if(mapForRobotState.size() != 0){
            re.put("robotState",mapForRobotState.get("value"));//机器人状态
            if(! "2".equals(mapForRobotState.get("value"))){
                re.put("cruiseMapPath","");//巡视路径地图路径
            }
//            if("2".equals(mapForRobotState.get("value"))){
//                try{
//                    Map<String,String> jasonMap=new HashMap<>();
//                    jasonMap.put("type","newTask");
//                    //jasonMap.put("taskId",tCruiseTask.getTaskId());
//                    String json= JSON.toJSONString(jasonMap);
//                    log.info("发送给前端的消息-停止调接口：   "+json);
//                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
//                }catch (Exception e){
//                    log.error("发送websocket错误"+e);
//                }
//            }
        }else {
            re.put("robotState","");//机器人状态
        }

        Map<String,String> mapForRobotDefeatState  = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":61");
        if(mapForRobotDefeatState.size() != 0){
            String value = mapForRobotDefeatState.get("value");
            if("1".equals(value)){
                re.put("modelType","任务模式");
            }
            if("2".equals(value)){
                re.put("modelType","紧急定位模式");
            }
            if("3".equals(value)){
                re.put("modelType","后台遥控模式");
            }
            if("4".equals(value)){
                re.put("modelType","手持遥控模式");
            }
        }else {
            re.put("modelType","");//机器人状态
        }

        return re;
    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectRobotTaskProgress(Long robotId) throws Exception{
        Map<String,Object> reMap = new HashMap<>();
        String robotCode = tRobotInspectionDao.selectRobotCode(robotId);
        List<String> taskList = tRobotInspectionDao.selectRobotTaskOnStart(robotId);
        if(taskList != null && taskList.size()>0){
           String taskId = taskList.get(0);
           reMap.put("taskId",taskId);
               Map<String,String> mapForRobotInstance = redisTemplate.opsForHash().entries("RobotTaskStatus:"+robotCode+":"+taskId);
               if(mapForRobotInstance == null || mapForRobotInstance.size() == 0){
                   reMap.put("taskProgress",0);
                   reMap.put("taskName","");
                   reMap.put("startTime","");
                   reMap.put("taskState","");

//                   Map<String,String> jasonMap=new HashMap<>();
//                   jasonMap.put("type","noTask");
//                   //jasonMap.put("taskId",tCruiseTask.getTaskId());
//                   String json= JSON.toJSONString(jasonMap);
//                   Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
//                   log.info("发送给前端的消息-停止调接口：   "+json);
                   return reMap;
               }
               if(mapForRobotInstance != null && mapForRobotInstance.size() > 0){
                   if("1".equals(mapForRobotInstance.get("taskState"))){
                       reMap.put("taskProgress",0);
                       reMap.put("taskName","");
                       reMap.put("startTime","");
                       reMap.put("taskState","");
                       return reMap;
                   }
               }
               //String taskId = (String)mapForRobotInstance.get("taskId");
               String instanceList = (String)mapForRobotInstance.get("instanceIdList");
               instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
               String[] instanceIdList = instanceList.split(", ");
               int i = 0;
               for (String item:instanceIdList) {
                   Map<String,Object> mapForRobotTaskMessage = redisTemplate.opsForHash().entries("t_cruise_task_result:"+taskId+":"+item);
                   if(mapForRobotTaskMessage.size() != 0){
                       if("null".equals(mapForRobotTaskMessage.get("cruiseResult"))){
                           continue;
                       }
                       i = i + 1;
                   }
               }
               Integer re = (int) ((new BigDecimal((float) i / instanceIdList.length).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())*100);
               TCruiseResult tc = tCruiseResultDao.selectForTaskId(taskId);
               if(tc == null){
                   reMap.put("taskProgress",0);
                   reMap.put("taskName","");
                   reMap.put("startTime","");
                   reMap.put("taskState","");
               }else{
                   if(tc.getCState() == 239 || tc.getCState() == 241){
                       reMap.put("taskProgress",re);
                       reMap.put("taskName",tc.getTaskName());
                       reMap.put("startTime",mapForRobotInstance.get("startTime"));
                       String state = mapForRobotInstance.get("taskState");
                       if(state != null){
                           //1=已执行 2=正在执行 3=暂停 4=终止 5=未执行 6=超期
                           if("1".equals(state)){
                               state = "已执行";
                           }
                           if("2".equals(state)){
                               state = "正在执行";
                           }
                           if("3".equals(state)){
                               state = "暂停";
                           }
                           if("4".equals(state)){
                               state = "终止";
                           }
                           if("5".equals(state)){
                               state = "未执行";
                           }
                           if("6".equals(state)){
                               state = "超期";
                           }
                       }else {
                           state="";
                       }
                       reMap.put("taskState",state);
                   }else {
                       reMap.put("taskProgress",0);
                       reMap.put("taskName","");
                       reMap.put("startTime","");
                       reMap.put("taskState","");
                   }
               }
        }else {
            reMap.put("taskProgress",0);
            reMap.put("taskName","");
            reMap.put("startTime","");
            reMap.put("taskState","");
            reMap.put("taskId","");

//            Map<String,String> jasonMap=new HashMap<>();
//            jasonMap.put("type","noTask");
//            //jasonMap.put("taskId",tCruiseTask.getTaskId());
//            String json= JSON.toJSONString(jasonMap);
//            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
//            log.info("发送给前端的消息-停止调接口：   "+json);
        }

        return reMap;
    }

    //机器人树
    @Transactional(rollbackFor = Exception.class)
    public List<Robot> robotTree(){
        List<Robot> reList = new ArrayList<>();
        Robot node = new Robot();
        node.setId(1L);
        node.setLabel("机器人树");
        node.setInfoType("tree");
        List<Robot> robotList = this.selectRobotInfo();
        for(Robot robot:robotList){
            robot.setId(robot.getRobotId());
            robot.setLabel(robot.getRobotName());
            robot.setUpId(1L);
            robot.setUpName("机器人树");
            robot.setInfoType("robot");
        }
        node.setChildren(robotList);
        reList.add(node);
        return reList;
    }

}

