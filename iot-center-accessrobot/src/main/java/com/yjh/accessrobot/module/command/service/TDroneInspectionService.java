package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TDroneInspectionDao;
import com.yjh.accessrobot.module.command.entity.TDroneInspection;
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
public class TDroneInspectionService {

    @Autowired
    private TDroneInspectionDao tDroneInspectionDao;
    private Logger log = LoggerFactory.getLogger(TDroneInspectionService.class);


    //@Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TDroneInspection tDroneInspection) {
        return this.tDroneInspectionDao.insert(tDroneInspection);
    }

    //@Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long inspectionId) {
        return this.tDroneInspectionDao.deleteByPrimaryId(inspectionId);
    }

    //@Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TDroneInspection tDroneInspection) {
        return this.tDroneInspectionDao.update(tDroneInspection);
    }

   // @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TDroneInspection selectByPrimaryId(Long inspectionId) {
        return this.tDroneInspectionDao.selectByPrimaryId(inspectionId);
    }

    //@Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDroneInspection> select(Long inspectionId, String inspectionCode, Long droneId, String inspectionName, Integer inspectionType,
                                         String componentId, Integer meterType, Integer appearanceType, Integer mainOperationType,
                                         Integer operationType, String saveTypeList, String recognitionTypeList, String phase, String deviceInfo) {
        return tDroneInspectionDao.select(inspectionId, inspectionCode, droneId, inspectionName,inspectionType,
                componentId, meterType, appearanceType, mainOperationType, operationType, saveTypeList,
                recognitionTypeList, phase, deviceInfo);
    }

    //@Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDroneInspection> selectByPage(TDroneInspection tDroneInspection) {
        List<TDroneInspection> tDroneInspectionList = tDroneInspectionDao.selectByPage(tDroneInspection);
        return tDroneInspectionList;
    }

    //@Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TDroneInspection> list) {
        return this.tDroneInspectionDao.batchInsertTDroneInspection(list);
    }
}

