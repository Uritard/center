package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.commons.logs.Logs;
import com.yjh.accessrobot.module.command.dao.TRobotInspectionDao;
import com.yjh.accessrobot.module.command.entity.TRobotInspection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author YC
 * @since 2020-11-19
 */
@Service
public class TRobotInspectionService {

    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    private Logger log = LoggerFactory.getLogger(TRobotInspectionService.class);


    //@Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.insert(tRobotInspection);
    }

    //@Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.deleteByPrimaryId(inspectionId);
    }

    //@Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.update(tRobotInspection);
    }

   // @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TRobotInspection selectByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.selectByPrimaryId(inspectionId);
    }

    //@Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> select(Long inspectionId, String inspectionCode ,Long robotId, String inspectionName,
                                         String componentId,String meterType,String appearanceType,String saveTypeList,
                                         String recognitionTypeList,String phase,String deviceInfo) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.select(inspectionId, inspectionCode, robotId, inspectionName,
                componentId,meterType,appearanceType,saveTypeList,
                recognitionTypeList,phase,deviceInfo);
        return tRobotInspectionList;
    }

    //@Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> selectByPage(TRobotInspection tRobotInspection) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.selectByPage(tRobotInspection);
        return tRobotInspectionList;
    }

    //@Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInspection> list) {
        return this.tRobotInspectionDao.batchInsertTRobotInspection(list);
    }
}

