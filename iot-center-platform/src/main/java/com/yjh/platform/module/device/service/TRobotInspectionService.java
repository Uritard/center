package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Robot;
import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;

import java.util.*;

import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.AreaInfoDetail;
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
    public List<RobotTaskMessage> selectRobotTaskMessage(Long robotId){
        return this.tRobotInspectionDao.selectRobotTaskMessage(robotId);
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
        //获取状态信息
        Map<String,Object> mapForCell  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":3");
        re.put("batteryLevel",mapForCell.get("valueUnit"));//电池电量
        Map<String,Object> mapForState  = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
        re.put("state",mapForState.get("value"));//状态
        Map<String,Object> mapForRobotCoordinate  = redisTemplate.opsForHash().entries("RobotCoordinate:"+robotCode);
        re.put("robotCoordinate",mapForRobotCoordinate.get("coordinatePixel"));//机器人坐标
        Map<String,Object> mapForMileage  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":2");
        re.put("mileage",mapForMileage.get("valueUnit"));//里程
        Map<String,Object> mapForSpeed  = redisTemplate.opsForHash().entries("RobotOperation:"+robotCode+":1");
        re.put("speed",mapForSpeed.get("valueUnit"));//速度
        Map<String,Object> mapForCruiseMap  = redisTemplate.opsForHash().entries("RobotRoad:"+robotCode);
        re.put("cruiseMapPath",mapForCruiseMap.get("filePath"));//巡视路径地图路径
        return re;
    }

    @Logs(title = "查询机器人任务进度", code = "device",content = "查询机器人任务进度")
    @Transactional(rollbackFor = Exception.class)
    public Object selectRobotTaskProgress(Long robotId){
        TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(robotId);
        //获取状态信息
        Map<String,Object> mapTaskProgress  = redisTemplate.opsForHash().entries("RobotTaskStatus:"+tRobotInfo.getRobotCode());
        return mapTaskProgress.get("taskProgress");
    }

}

