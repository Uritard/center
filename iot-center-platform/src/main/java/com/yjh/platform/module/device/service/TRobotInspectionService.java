package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;

import java.util.List;

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
    public List<TRobotInspection> select(Long inspectionId, Long robotId, String inspectionName, Integer inspectionType, String alarmTop, String alarmBottom, String defaultValue, Integer inspectionPostion, Integer collectStatus, Integer calibrationStatus, String unit) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.select(inspectionId, robotId, inspectionName, inspectionType, alarmTop, alarmBottom, defaultValue, inspectionPostion, collectStatus, calibrationStatus, unit);
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
}

