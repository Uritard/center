package com.yjh.platform.module.task.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.service.SystemInfoService;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.dao.TDefectInfoDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TCameraGroupDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2020/12/14
 */
@Service
public class HomePageService {

    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
    @Autowired
    private TDefectInfoDao tDefectInfoDao;
    @Autowired
    private TCruiseTaskResultService tCruiseTaskResultService;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SystemInfoService systemInfoService;
    @Autowired
    private TCameraGroupDao tCameraGroupDao;
    @Autowired
    private TCameraScreenDao tCameraScreenDao;

    @Logs(title = "巡视任务概览数据", code = "TaskForHomeService",content = "根据页面参数查询任务数据概览")
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> taskInfo(Integer date) {
        if(1 == date){
            return tCruiseTaskDao.selectOnWeek();
        }
        if(2 == date){
            return tCruiseTaskDao.selectOnMonth();
        }
        if(3 == date){
            return tCruiseTaskDao.selectOnYear();
        }
        if(4 == date){
            return tCruiseTaskDao.selectForSevenDay();
        }
        if(5 == date){
            return tCruiseTaskDao.selectForMonth();
        }
        if(6 == date){
            return tCruiseTaskDao.selectForYear();
        }
        return null;
    }


    @Logs(title = "统计缺陷", code = "TaskForHomeService",content = "统计缺陷")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Integer>> countByDefectLevel(){
        return tDefectInfoDao.countByDefectLevel();
    }

    @Logs(title = "巡视任务数据", code = "TaskForHomeService",content = "巡视任务数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TaskOnExecuteInfo> taskOnExecute()throws ParseException {
        //查出正在执行的任务
        List<TaskOnExecuteInfo> listTask=tCruiseTaskDao.selectTaskOnExecute();
        for (TaskOnExecuteInfo item: listTask) {
            CruiseResultCounter cruiseResultCounter = tCruiseTaskResultService.selectCruiseStatusCount(item.getTaskId());
            item.setAlarmCount(cruiseResultCounter.getAlarmCount());
            item.setCruisedCount(cruiseResultCounter.getCruisedCount());
            item.setCruiseNotCount(cruiseResultCounter.getCruiseNotCount());
            item.setRunningTime(cruiseResultCounter.getRunningTime());
            Map<String,Object> map = tCruiseTaskResultService.selectCruiseAdvance(item.getTaskId());
            Float i = (Float) map.get("rate");
            i = i*100F;
            item.setTaskProgress(i.intValue());
        }
        return listTask;
    }

    @Logs(title = "告警级别数据", code = "TaskForHomeService",content = "告警级别数据")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> countByAlarmLevel(){
        return tDefectInfoDao.countByAlarmLevel();
    }

    @Logs(title = "告警内容数据", code = "TaskForHomeService",content = "告警内容数据")
    @Transactional(rollbackFor = Exception.class)
    public List<WarnInfoForHomePage> selectThereWarn(){
        return tDefectInfoDao.selectThereWarn();
    }

    @Logs(title = "机器人信息", code = "TaskForHomeService",content = "机器人信息")
    @Transactional(rollbackFor = Exception.class)
    public List<RobotInfoForHomePage> robotInfoForHomePage(String robotPosition) throws Exception{
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#0");
        List<RobotInfoForHomePage> robotList = tRobotInfoDao.selectRobotInfo(robotPosition);
        for (RobotInfoForHomePage item: robotList) {
            //计算机器人投运时间
            Date startTime = simpleDateFormat.parse(item.getCommissionDate());
            Date endTime = new Date();
            long diff = endTime.getTime()-startTime.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
            long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
            String usedTime= "";
            if(days != 0){
                usedTime =usedTime+days+"天";
            }
            if(hours != 0){
                usedTime =usedTime+hours+"小时";
            }
            if(minutes != 0){
                usedTime =usedTime+minutes+"分";
            }
            item.setCommissionDate(usedTime);
            Map<String,Object> mapForCell  = redisTemplate.opsForHash().entries("RobotOperation:"+item.getRobotCode()+":3");
            if(mapForCell.size() != 0){
               Double value = Double.valueOf(mapForCell.get("value").toString());//电池电量
                String valueUnit = df.format(value)+"%";
                item.setBatteryLevel(valueUnit);
            }else {
                item.setBatteryLevel("");
            }
            Map<String,Object> mapForOnlineState  = redisTemplate.opsForHash().entries("RobotStatus:"+item.getRobotCode()+":2");
            if(mapForOnlineState.size() != 0){
                String state = mapForOnlineState.get("value").toString();
                if("0".equals(state)){
                    state = "在线";
                }else if("1".equals(state)){
                    state = "离线";
                }else {
                    state ="";
                }
                item.setOnlineState(state);//网络状态
            }else {
                item.setOnlineState("");//网络状态
            }
            Map<String,Object> mapForMileage  = redisTemplate.opsForHash().entries("RobotOperation:"+item.getRobotCode()+":2");
            if(mapForMileage.size() != 0){
                item.setMileage(mapForMileage.get("valueUnit").toString());//里程
            }else {
                item.setMileage("");//里程
            }
            Map<String,Object> mapForControlModel  = redisTemplate.opsForHash().entries("RobotStatus:"+item.getRobotCode()+":61");
            if(mapForMileage.size() != 0){
                String model = mapForControlModel.get("value").toString();
                if("1".equals(model)){
                    model = "任务模式";
                }else if("2".equals(model)){
                    model = "紧急定位模式";
                }else if("3".equals(model)){
                    model = "后台遥控模式";
                }else if("4".equals(model)){
                    model = "手持遥控模式";
                }else {
                    model ="";
                }
                item.setControlModel(model);//控制模式
            }else {
                item.setControlModel("");//控制模式
            }

            Map<String,Object> mapForRobotState  = redisTemplate.opsForHash().entries("RobotStatus:"+item.getRobotCode()+":41");
            if(mapForRobotState.size() != 0){
                String state = mapForRobotState.get("value").toString();
                if("1".equals(state)){
                    state = "空闲状态";
                }else if("2".equals(state)){
                    state = "巡视状态";
                }else if("3".equals(state)){
                    state = "充电状态";
                }else if("4".equals(state)){
                    state = "检修状态";
                }else {
                    state ="";
                }
                item.setRobotStates(state);//机器人状态
            }else {
                item.setRobotStates("");//机器人状态
            }
        }
        return robotList;
    }

    @Logs(title = "变电站概况信息", code = "TaskForHomeService",content = "机器人信息")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> stationInfo() throws Exception{
        Map<String,Object> mapForRe = new HashMap<>();
        //获取投运时间
        //platform
        String platform = redisTemplate.opsForHash().entries("t_sys_param:systemPlatFromServices").get("content").toString()+"/";
        List<Map<String,Object>> mapForTime = systemInfoService.getServices();
        for (Map<String,Object> item: mapForTime) {
            if(platform.equals(item.get("url"))){
                mapForRe.put("usedTime",item.get("usedTime"));
            }
        }
        //电压等级
        String stationVoltageGrade = redisTemplate.opsForHash().entries("t_sys_param:stationVoltageGrade").get("content").toString();
        mapForRe.put("stationVoltageGrade",stationVoltageGrade);
        //所属班所
        String fromWhatClass = redisTemplate.opsForHash().entries("t_sys_param:fromWhatClass").get("content").toString();
        mapForRe.put("fromWhatClass",fromWhatClass);
        //所有人员
        String allPeople = redisTemplate.opsForHash().entries("t_sys_param:allPeople").get("content").toString();
        mapForRe.put("allPeople",allPeople);
        //工作人员
        String workPeople = redisTemplate.opsForHash().entries("t_sys_param:workPeople").get("content").toString();
        mapForRe.put("workPeople",workPeople);
        //所有车辆
        String allCar = redisTemplate.opsForHash().entries("t_sys_param:allCar").get("content").toString();
        mapForRe.put("allCar",allCar);
        //工作车辆
        String workCar = redisTemplate.opsForHash().entries("t_sys_param:workCar").get("content").toString();
        mapForRe.put("workCar",workCar);
        return mapForRe;
    }

    @Logs(title = "获取摄像机分组信息", code = "TaskForHomeService",content = "获取摄像机分组信息")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> getCameraGroupInfo(){
        return tCameraGroupDao.selectGroupName();
    }

    @Logs(title = "获取摄像机id信息", code = "TaskForHomeService",content = "获取摄像机id信息")
    @Transactional(rollbackFor = Exception.class)
    public List<Long> getCameraIdInfo(){
        Map<String,String> map = new HashMap<>();
        //获取相机的状态
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if(re == null){
                continue;
            }
            map.putAll((Map<String,String>)re.getData());
        }
        List<Long> re =  new ArrayList<>();
        List<Long> cameraIdList =tCameraGroupDao.selectCameraIdInfo();
        for (Long item: cameraIdList) {
            //String str = map.get(item.toString());
            if("1".equals(map.get(item.toString()))){
                re.add(item);
            }
        }
        return re;
    }

    @Logs(title = "获取分组下摄像机id信息", code = "TaskForHomeService",content = "获取分组下摄像机id信息")
    @Transactional(rollbackFor = Exception.class)
    public List<String> getCameraIdInfoForGroup(Long groupId){
        Map<String,String> map = new HashMap<>();
        //获取相机的状态
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if(re == null){
                continue;
            }
            map.putAll((Map<String,String>)re.getData());
        }
        List<String> re =  new ArrayList<>();
        String[] cameraIdList =tCameraGroupDao.selectCameraIdInfoForGroup(groupId).split(",");
        for (String item: cameraIdList) {
            //String str = map.get(item.toString());
            if("1".equals(map.get(item))){
                re.add(item);
            }
        }
        return re;
    }



    private static Result cameraStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class,map);
            }
        } catch (Exception e) {

        }
        return re;
    }


}
