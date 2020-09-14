package com.yjh.platform.module.user.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.dao.TRobotInfoDao;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.user.entity.TRobotInspectionTree;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;


/**
* @author tt
* @since 2020-08-05
*/
@Service
public class TRobotInfoService{

    @Autowired
    private TRobotInfoDao tRobotInfoDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.insert(tRobotInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.deleteByPrimaryId(robotId)+this.tRobotInfoDao.deletePlan(robotId)+this.tRobotInfoDao.deleteInstance(robotId)+this.tRobotInfoDao.deleteInspection(robotId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.update(tRobotInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TRobotInfo selectByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.selectByPrimaryId(robotId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> select(Long robotId, String robotCode, String robotName, String robotStatus, Integer robotType, String robotIp, Integer robotPort,
                                    String upRegionName, String lightIp, String lightPort, String lightUsername, String lightPassword,
                                   String lnferadIp, Integer inferadPort, String inferadUsername, String inferadPassword, String photePath,
                                   String createBy, Date createDate, String updateBy, Date updateDate, String robotFactory,String isUse,
                                   Date commissionDateString, Long upRegionId, String robotPosition, String remarks) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.select(robotId, robotCode, robotName, robotStatus, robotType, robotIp, robotPort,
                upRegionName, lightIp, lightPort, lightUsername, lightPassword, lnferadIp, inferadPort, inferadUsername, inferadPassword,
                photePath, createBy, createDate, updateBy, updateDate, robotFactory, isUse, commissionDateString, upRegionId, robotPosition, remarks);
        return tRobotInfoList;
    }

    @Logs(title = "分页模糊查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> selectByPage(TRobotInfo tRobotInfo, List<Long> upRegionIds) {
        if(upRegionIds.size() != 0){
            tRobotInfo.setUpRegionIds(upRegionIds);
        }else {
            upRegionIds.add(tRobotInfo.getUpRegionId());
            tRobotInfo.setUpRegionIds(upRegionIds);
        }
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.selectByPage(tRobotInfo);
        return tRobotInfoList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInfo> list) {
        return this.tRobotInfoDao.batchInsert(list);
    }

    @Logs(title = "查询所有机器人巡检点信息树", code = "robotInspectionTree")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectInspectionTree() {
        List<TRobotInspectionTree> robotList = tRobotInfoDao.selectInspectionTree();
        List<TRobotInspectionTree> tRobotInspectionTreeList = tRobotInfoDao.batchSelectInspection();
        List<Map<String, Object>> robotPresetTreeTemList = new ArrayList<>();
        for (TRobotInspectionTree tRobot : robotList) {
            Map<String, Object> robotPresetTreeTem = new HashMap<>();
            List<Map<String, Object>> presetList = new ArrayList<>();
            Long robotId = tRobot.getRobotId();
            for (TRobotInspectionTree tRobotInspectionTree : tRobotInspectionTreeList) {
                Long robotIdTem = tRobotInspectionTree.getRobotId();
                if (Objects.nonNull(robotIdTem) && Objects.equals(robotId, robotIdTem)) {
                    Map<String, Object> inspectionMap = new HashMap<>();
                    inspectionMap.put("id", tRobotInspectionTree.getInspectionId());
                    inspectionMap.put("label", tRobotInspectionTree.getInspectionName());
                    inspectionMap.put("infoType", "inspection");
                    presetList.add(inspectionMap);
                }
            }
            robotPresetTreeTem.put("id", robotId);
            robotPresetTreeTem.put("label", tRobot.getRobotName());
            robotPresetTreeTem.put("position", tRobot.getRobotPosition());
            robotPresetTreeTem.put("children", presetList);
            robotPresetTreeTem.put("infoType", "robot");
            robotPresetTreeTemList.add(robotPresetTreeTem);
        }
        Map<String, Object> robotInspectionTree = new HashMap<>();
        robotInspectionTree.put("children", robotPresetTreeTemList);
        robotInspectionTree.put("label", "机器人巡检点列表");
        robotInspectionTree.put("infoType", "tree");
        robotInspectionTree.put("id", "-1");
        List<Map<String, Object>> robotInspectionList = new ArrayList<>();
        robotInspectionList.add(robotInspectionTree);
        return robotInspectionList;
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedRobot(String[] robotIds) {
        return this.tRobotInfoDao.deleteSelectedRobot(robotIds);
    }

}
