package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TCameraInfoMapper;
import com.yjh.accessrobot.module.command.dao.TCameraRecorderDao;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.CameraModel;
import com.yjh.accessrobot.module.command.entity.TCameraInfo;
import com.yjh.accessrobot.module.command.entity.TCameraRecorder;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/10
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TCameraInfoService {

    @Autowired
    private TCameraInfoMapper tCameraInfoMapper;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;

    @Transactional
    public void saveReportData(List<CameraModel> cameraModelList, String edgeNode) {
        log.info("开始同步摄像机信息  edgeNode:{}  ", edgeNode);
        if (CollectionUtils.isEmpty(cameraModelList)) {
            tCameraInfoMapper.deleteByEdgeCode(edgeNode);
            return;
        }
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.selectByEdgeCode(edgeNode);
        Map<String, Long> tCameraRecorderMap = tCameraRecorderList.stream().collect(Collectors.toMap(TCameraRecorder::getOriginId, TCameraRecorder::getRecordId));
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeNode, null);
        Map<String, String> stdRegionMap = stdRegionList.stream().collect(Collectors.toMap(tStdRegion -> tStdRegion.getOriginRegionId().toString(), tStdRegion -> tStdRegion.getRegionId().toString()));
        List<TCameraInfo> tCameraInfoList = cameraModelList.stream().map(cameraModel -> {
            TCameraInfo tCameraInfo = new CameraModel();
            tCameraInfo.setCameraId(null);
            tCameraInfo.setCameraName(cameraModel.getPatroldeviceName());
            tCameraInfo.setCameraModel(cameraModel.getCameraModel());
            tCameraInfo.setPmsId(cameraModel.getPmsId());
            tCameraInfo.setAliasName(cameraModel.getAliasName());
            tCameraInfo.setRecordId(tCameraRecorderMap.get(cameraModel.getRecordId().toString()));
            tCameraInfo.setUpRegionId(stdRegionMap.get(cameraModel.getUpRegionId()));
            tCameraInfo.setChannelNum(cameraModel.getChannelNum());
            tCameraInfo.setCameraNum(cameraModel.getCameraNum());
            tCameraInfo.setSmsId(cameraModel.getSmsId());
            tCameraInfo.setRmsId(cameraModel.getRmsId());
            tCameraInfo.setMonitorId(cameraModel.getMonitorId());
            tCameraInfo.setVendorId(cameraModel.getVendorId());
            tCameraInfo.setStreamType(cameraModel.getStreamType());
            tCameraInfo.setProtocolType(cameraModel.getProtocolType());
            tCameraInfo.setUrl(cameraModel.getUrl());
            tCameraInfo.setCameraIp(cameraModel.getCameraIp());
            tCameraInfo.setPort(cameraModel.getPort());
            tCameraInfo.setInfreadPort(cameraModel.getInfreadPort());
            tCameraInfo.setCameraManager(cameraModel.getCameraManager());
            tCameraInfo.setCameraCode(cameraModel.getCameraCode());
            tCameraInfo.setCameraType(cameraModel.getCameraType());
            tCameraInfo.setIsControl(cameraModel.getIsControl());
            tCameraInfo.setLatitude(cameraModel.getLatitude());
            tCameraInfo.setLongitude(cameraModel.getLongitude());
            tCameraInfo.setAddress(cameraModel.getAddress());
            tCameraInfo.setUnit(cameraModel.getUnit());
            tCameraInfo.setCreateTime(cameraModel.getCreateTime());
            tCameraInfo.setCommissionDate(cameraModel.getCommissionDate());
            tCameraInfo.setEdgeCode(edgeNode);
            tCameraInfo.setOriginId(cameraModel.getPatroldeviceCode());
            return tCameraInfo;
        }).collect(Collectors.toList());

        List<TCameraInfo> oldTCameraInfoList = tCameraInfoMapper.selectByEdgeCode(edgeNode);
        if (CollectionUtils.isEmpty(oldTCameraInfoList)) {
            tCameraInfoMapper.insertBatch(tCameraInfoList);
        } else {
            Map<String, TCameraInfo> oldCameraInfoMap = oldTCameraInfoList.stream().collect(Collectors.toMap(TCameraInfo::getOriginId, Function.identity()));
            Map<String, TCameraInfo> newCameraInfoMap = tCameraInfoList.stream().collect(Collectors.toMap(TCameraInfo::getOriginId, Function.identity()));
            // 更新的数据
            SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldCameraInfoMap.keySet(), newCameraInfoMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdSet)) {
                tCameraInfoList.stream().filter(tCameraInfo -> updateIdSet.contains(tCameraInfo.getOriginId())).forEach(tCameraInfo -> {
                    TCameraInfo oldCameraInfo = oldCameraInfoMap.get(tCameraInfo.getOriginId());
                    tCameraInfo.setCameraId(oldCameraInfo.getCameraId());
                    tCameraInfoMapper.updateByPrimaryKey(tCameraInfo);
                });
            }
            //删除的数据
            SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldCameraInfoMap.keySet(), newCameraInfoMap.keySet());
            if (CollectionUtils.isNotEmpty(deleteIdSet)) {
                tCameraInfoMapper.deleteByEdgeCodeAndOriginId(edgeNode, deleteIdSet);
            }
            //新增的数据
            SetUtils.SetView<String> insertIdSet = SetUtils.difference(newCameraInfoMap.keySet(), oldCameraInfoMap.keySet());
            if (CollectionUtils.isNotEmpty(insertIdSet)) {
                List<TCameraInfo> insertCameraInfoList = tCameraInfoList.stream().filter(tCameraInfo -> insertIdSet.contains(tCameraInfo.getOriginId())).collect(Collectors.toList());
                tCameraInfoMapper.insertBatch(insertCameraInfoList);
            }
        }
    }
}
