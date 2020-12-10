package com.yjh.platform.module.device.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Robot;
import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.entity.TCruisePointAttr;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

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


    @Logs(title = "插入", code = "device",content = "根据页面传入的参数新增数据")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.insert(tRobotInspection);
    }

    @Logs(title = "删除", code = "device",content = "根据页面传入的参数删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.deleteByPrimaryId(inspectionId);
    }

    @Logs(title = "更新", code = "device",content = "根据页面传入的参数更新数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.update(tRobotInspection);
    }

    @Logs(title = "主键查询", code = "device",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public TRobotInspection selectByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.selectByPrimaryId(inspectionId);
    }

    @Logs(title = "查询", code = "device",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> select(Long inspectionId, String inspectionCode ,Long robotId, String inspectionName, Integer inspectionType, String alarmTop, String alarmBottom, String defaultValue, Integer inspectionPosition, Integer collectStatus, Integer calibrationStatus, String unit) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.select(inspectionId, inspectionCode, robotId, inspectionName, inspectionType, alarmTop, alarmBottom, defaultValue, inspectionPosition, collectStatus, calibrationStatus, unit);
        return tRobotInspectionList;
    }

    @Logs(title = "分页查询", code = "device",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> selectByPage(TRobotInspection tRobotInspection) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.selectByPage(tRobotInspection);
        return tRobotInspectionList;
    }

    @Logs(title = "批量插入", code = "device",content = "根据页面传入的参数批量插入数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInspection> list) {
        return this.tRobotInspectionDao.batchInsert(list);
    }

    @Logs(title = "机器人监控", code = "device",content = "查询机器人的任务信息")
    @Transactional(rollbackFor = Exception.class)
    public List<RobotTaskMessage> selectRobotTaskMessage(Long robotId) throws Exception{
        List<RobotTaskMessage> re = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取此机器人的巡视点
        String robotCode = tRobotInspectionDao.selectRobotCode(robotId);
        Map<String,Object> mapForRobotInstance = redisTemplate.opsForHash().entries("RobotTaskStatus:"+robotCode);
        Map<String,Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":41");
        String taskId = (String)mapForRobotInstance.get("taskId");
        String robotState =(String) mapForRobotState.get("value");
        if( !"2".equals(robotState)){
            //机器人未在做任务
            return  null;
        }
        String instanceList = (String)mapForRobotInstance.get("instanceIdList");
        instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
        String[] instanceIdList = instanceList.split(", ");
        List<TCruisePointAttr> nameList =  tRobotInspectionDao.selectRobotTaskMessage(instanceIdList);
        for (String item:instanceIdList) {
            //获取任务数据
            Map<String,Object> mapForRobotTaskMessage = redisTemplate.opsForHash().entries("t_cruise_task_result:"+taskId+item);
            RobotTaskMessage robotTaskMessage = new RobotTaskMessage();
            robotTaskMessage.setDeviceName(getName(Long.valueOf(item),1,nameList));
            robotTaskMessage.setInstanceName(getName(Long.valueOf(item),0,nameList));
            if(mapForRobotTaskMessage.get("cruiseTime") != null){
                Date cruiseTime = simpleDateFormat.parse(mapForRobotTaskMessage.get("cruiseTime").toString());
                robotTaskMessage.setCruiseTime(cruiseTime);
                robotTaskMessage.setResult((String)mapForRobotTaskMessage.get("resultNum"));
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

    @Logs(title = "查询机器人信息", code = "device",content = "查询机器人信息")
    @Transactional(rollbackFor = Exception.class)
    public List<Robot> selectRobotInfo(){
        return this.tRobotInspectionDao.selectRobotInfo();
    }

    @Logs(title = "查询机器人状态信息", code = "device",content = "查询机器人状态信息")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectRobotStatus(String robotCode){
        Map<String,Object> re = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00");
        //获取状态信息
        Map<String,Object> mapForCell  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":3");
        re.put("batteryLevel",mapForCell.get("valueUnit"));//电池电量
        Map<String,Object> mapForOnlineState  = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
        re.put("onlineState",mapForOnlineState.get("value"));//网络状态
        Map<String,Object> mapForRobotCoordinate  = redisTemplate.opsForHash().entries("RobotCoordinate:"+robotCode);
        re.put("robotCoordinate",mapForRobotCoordinate.get("coordinatePixel"));//机器人坐标
        Map<String,Object> mapForMileage  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":2");
        re.put("mileage",mapForMileage.get("valueUnit"));//里程
        Map<String,String> mapForSpeed  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":1");
        Double speed =Double.valueOf(mapForSpeed.get("value")) ;
        re.put("speed",df.format(speed)+mapForSpeed.get("unit"));//速度
        Map<String,Object> mapForCruiseMap  = redisTemplate.opsForHash().entries("RobotRoad:"+robotCode);
        re.put("cruiseMapPath",mapForCruiseMap.get("relativePath"));//巡视路径地图路径
        Map<String,Object> mapForRobotState  = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":41");
        re.put("robotState",mapForRobotState.get("value"));//机器人状态
        return re;
    }

    @Logs(title = "查询机器人任务进度", code = "device",content = "查询机器人任务进度")
    @Transactional(rollbackFor = Exception.class)
    public Object selectRobotTaskProgress(Long robotId){

        String robotCode = tRobotInspectionDao.selectRobotCode(robotId);
        Map<String,Object> mapForRobotInstance = redisTemplate.opsForHash().entries("RobotTaskStatus:"+robotCode);
        String taskId = (String)mapForRobotInstance.get("taskId");
        String instanceList = (String)mapForRobotInstance.get("instanceIdList");
        instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
        String[] instanceIdList = instanceList.split(", ");
        int i = 0;
        for (String item:instanceIdList) {
            Map<String,Object> mapForRobotTaskMessage = redisTemplate.opsForHash().entries("t_cruise_task_result:"+taskId+item);
            if(mapForRobotTaskMessage.size() != 0){
                i = i + 1;
            }
        }
        Integer re = (int) ((new BigDecimal((float) i / instanceIdList.length).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())*100);
        return re;
    }

}

