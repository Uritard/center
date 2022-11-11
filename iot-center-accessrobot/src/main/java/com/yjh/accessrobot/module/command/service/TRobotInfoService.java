package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TCameraRecorderDao;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.RobotModel;
import com.yjh.accessrobot.module.command.entity.TCameraRecorder;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author YC
 * @since 2020-11-19
 */
@Service
public class TRobotInfoService {

    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.insert(tRobotInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.deleteByPrimaryId(robotId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.update(tRobotInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public TRobotInfo selectByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.selectByPrimaryId(robotId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> select(Long robotId, String robotCode, String robotName, String robotStatus, Integer robotType, String robotIp, Integer robotPort, String lightIp, String lightPort, String lightUsername, String lightPassword, String lnferadIp, Integer inferadPort, String inferadUsername, String inferadPassword, String photePath, String createBy, Date createDate, String updateBy, Date updateDate, String robotFactory, String isUse, Date commissionDate, Long upRegionId, String robotPosition, String remarks) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.select(robotId, robotCode, robotName, robotStatus, robotType, robotIp, robotPort, lightIp, lightPort, lightUsername, lightPassword, lnferadIp, inferadPort, inferadUsername, inferadPassword, photePath, createBy, createDate, updateBy, updateDate, robotFactory, isUse, commissionDate, upRegionId, robotPosition, remarks);
        return tRobotInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> selectByPage(TRobotInfo tRobotInfo) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.selectByPage(tRobotInfo);
        return tRobotInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInfo> list) {
        return this.tRobotInfoDao.batchInsert(list);
    }


    @Transactional
    public void saveReportData(List<RobotModel> robotModelList, String edgeNode, String type) {
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.selectByEdgeCode(edgeNode);
        Map<String, Long> tCameraRecorderMap = tCameraRecorderList.stream().collect(Collectors.toMap(TCameraRecorder::getOriginId, TCameraRecorder::getRecordId));
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeNode, null);
        Map<String, Long> stdRegionMap = stdRegionList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, TStdRegion::getRegionId));
        List<TRobotInfo> tRobotInfoList = robotModelList.stream().map(robotModel -> {
            TRobotInfo tRobotInfo = new TRobotInfo();
            tRobotInfo.setRobotId(null);
            tRobotInfo.setRobotCode(robotModel.getRobotCode());
            tRobotInfo.setRobotName(robotModel.getPatroldeviceName());
            tRobotInfo.setRobotStatus(robotModel.getRobotStatus());
            tRobotInfo.setRobotType(robotModel.getRobotType());
            tRobotInfo.setRobotIp(robotModel.getRobotIp());
            tRobotInfo.setRobotPort(robotModel.getRobotPort());
            tRobotInfo.setLightIp(robotModel.getLightIp());
            tRobotInfo.setLightPort(robotModel.getLightPort());
            tRobotInfo.setIdentityManager(robotModel.getIdentityManager());
            tRobotInfo.setIdentityCode(robotModel.getIdentityCode());
            tRobotInfo.setLnferadIp(robotModel.getLnferadIp());
            tRobotInfo.setInferadPort(robotModel.getInferadPort());
            tRobotInfo.setInferadUsername(robotModel.getInferadUsername());
            tRobotInfo.setInferadPassword(robotModel.getInferadPassword());
            tRobotInfo.setPhotePath(robotModel.getPhotePath());
            tRobotInfo.setCreateBy(robotModel.getCreateBy());
            tRobotInfo.setCreateDate(robotModel.getCreateDate());
            tRobotInfo.setUpdateBy(robotModel.getUpdateBy());
            tRobotInfo.setUpdateDate(robotModel.getUpdateDate());
            tRobotInfo.setRobotFactory(robotModel.getRobotFactory());
            tRobotInfo.setIsUse(robotModel.getIsUse());
            tRobotInfo.setCommissionDate(robotModel.getCommissionDate());
            tRobotInfo.setUpRegionId(stdRegionMap.get(robotModel.getUpRegionId().toString()));
            tRobotInfo.setRobotPosition(robotModel.getRobotPosition());
            tRobotInfo.setRemarks(robotModel.getRemarks());
            tRobotInfo.setNestCode(robotModel.getNestCode());
            tRobotInfo.setNestName(robotModel.getNestName());
            tRobotInfo.setLastOnlineTime(robotModel.getLastOnlineTime());
            tRobotInfo.setDuration(robotModel.getDuration());
            tRobotInfo.setOffLineCount(robotModel.getOffLineCount());
            tRobotInfo.setMadeDate(robotModel.getMadeDate());
            tRobotInfo.setBuildingUser(robotModel.getBuildingUser());
            tRobotInfo.setRobotSource(robotModel.getRobotSource());
            tRobotInfo.setAppearanceNumber(robotModel.getAppearanceNumber());
            tRobotInfo.setRobotNum(robotModel.getRobotNum());
            tRobotInfo.setDroneType(robotModel.getDroneType());
            tRobotInfo.setMadeIn(robotModel.getMadeIn());
            tRobotInfo.setDronePosition(robotModel.getDronePosition());
            tRobotInfo.setAddress(robotModel.getAddress());
            tRobotInfo.setDefectRecord(robotModel.getDefectRecord());
            tRobotInfo.setRepairRecord(robotModel.getRepairRecord());
            tRobotInfo.setExitPutintoRecord(robotModel.getExitPutintoRecord());
            tRobotInfo.setRecordId(tCameraRecorderMap.get(robotModel.getRecordId().toString()));
            tRobotInfo.setChannelNumLight(robotModel.getChannelNumLight());
            tRobotInfo.setChannelNumInferad(robotModel.getChannelNumInferad());
            tRobotInfo.setEdgeCode(edgeNode);
            tRobotInfo.setOriginId(robotModel.getRobotId().toString());
            return tRobotInfo;
        }).collect(Collectors.toList());
        List<TRobotInfo> oldRobotInfoList = tRobotInfoDao.selectByEdgeCodeAndType(edgeNode, type);
        if (CollectionUtils.isEmpty(oldRobotInfoList)) {
            tRobotInfoDao.batchInsert(tRobotInfoList);
        } else {
            Map<String, TRobotInfo> oldTRobotInfoMap = oldRobotInfoList.stream().collect(Collectors.toMap(TRobotInfo::getOriginId, Function.identity()));
            Map<String, TRobotInfo> newTRobotInfoMap = tRobotInfoList.stream().collect(Collectors.toMap(TRobotInfo::getOriginId, Function.identity()));
            // 更新的数据
            SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldTRobotInfoMap.keySet(), newTRobotInfoMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdSet)) {
                tRobotInfoList.stream().filter(tRobotInfo -> updateIdSet.contains(tRobotInfo.getOriginId())).forEach(tRobotInfo -> {
                    TRobotInfo oldTRobotInfo = oldTRobotInfoMap.get(tRobotInfo.getOriginId());
                    tRobotInfo.setRobotId(oldTRobotInfo.getRobotId());
                    tRobotInfoDao.update(tRobotInfo);
                });
            }
            //删除的数据
            SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldTRobotInfoMap.keySet(), newTRobotInfoMap.keySet());
            if (CollectionUtils.isNotEmpty(deleteIdSet)) {
                tRobotInfoDao.deleteByEdgeCodeAndOriginId(edgeNode, deleteIdSet);
            }
            //新增的数据
            SetUtils.SetView<String> insertIdSet = SetUtils.difference(newTRobotInfoMap.keySet(), oldTRobotInfoMap.keySet());
            if (CollectionUtils.isNotEmpty(insertIdSet)) {
                List<TRobotInfo> insertTRobotInfoList = tRobotInfoList.stream().filter(tRobotInfo -> insertIdSet.contains(tRobotInfo.getOriginId())).collect(Collectors.toList());
                tRobotInfoDao.batchInsert(insertTRobotInfoList);
            }
        }
    }

}

