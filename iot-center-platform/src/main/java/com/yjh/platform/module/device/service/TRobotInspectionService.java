package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Robot;
import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;

import java.util.*;

import com.yjh.platform.module.user.entity.AreaInfoDetail;
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

    @Logs(title = "插入", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.insert(tRobotInspection);
    }

    @Logs(title = "删除", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.deleteByPrimaryId(inspectionId);
    }

    @Logs(title = "更新", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.update(tRobotInspection);
    }

    @Logs(title = "主键查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public TRobotInspection selectByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.selectByPrimaryId(inspectionId);
    }

    @Logs(title = "查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> select(Long inspectionId, String inspectionCode ,Long robotId, String inspectionName, Integer inspectionType, String alarmTop, String alarmBottom, String defaultValue, Integer inspectionPosition, Integer collectStatus, Integer calibrationStatus, String unit) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.select(inspectionId, inspectionCode, robotId, inspectionName, inspectionType, alarmTop, alarmBottom, defaultValue, inspectionPosition, collectStatus, calibrationStatus, unit);
        return tRobotInspectionList;
    }

    @Logs(title = "分页查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> selectByPage(TRobotInspection tRobotInspection) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.selectByPage(tRobotInspection);
        return tRobotInspectionList;
    }

    @Logs(title = "批量插入", code = "device")
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
    public Map<String,Object> selectRobotStatus(Long robotId){
        //获取状态信息
        Map<String,Object> mapForGet  = redisTemplate.opsForHash().entries("robotId");
        return mapForGet;
    }

}

