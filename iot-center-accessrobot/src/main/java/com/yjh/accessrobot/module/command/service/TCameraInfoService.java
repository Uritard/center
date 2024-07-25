package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.SysUserDao;
import com.yjh.accessrobot.module.command.dao.TCameraInfoMapper;
import com.yjh.accessrobot.module.command.dao.TCameraRecorderDao;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Autowired
    private SysUserDao sysUserDao;

    @Transactional(rollbackFor = Exception.class)
    public void saveReportData(List<CameraModel> cameraModelList, String edgeNode) {
        log.info("开始同步摄像机信息  edgeNode:{}  ", edgeNode);
        if (CollectionUtils.isEmpty(cameraModelList)) {
            tCameraInfoMapper.deleteByEdgeCode(edgeNode);
            return;
        }
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.selectByEdgeCode(edgeNode);
        Map<String, Long> tCameraRecorderMap = tCameraRecorderList.stream().collect(Collectors.toMap(TCameraRecorder::getOriginId, TCameraRecorder::getRecordId));
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeNode, null);
        Map<String, String> stdRegionMap = stdRegionList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, tStdRegion -> tStdRegion.getRegionId().toString()));
        List<TCameraInfo> tCameraInfoList = cameraModelList.stream().peek(cameraModel -> {
            cameraModel.setOriginId(String.valueOf(cameraModel.getCameraId()));
            cameraModel.setEdgeCode(edgeNode);
            cameraModel.setCameraId(null);
            cameraModel.setCameraName(cameraModel.getPatroldeviceName());
            cameraModel.setRecordId(tCameraRecorderMap.get(cameraModel.getRecordId().toString()));
            cameraModel.setUpRegionId(stdRegionMap.get(cameraModel.getUpRegionId()));
            if (StringUtils.length(cameraModel.getPatroldeviceCode()) == 18) {
                cameraModel.setBcameraChannelId(cameraModel.getPatroldeviceCode());
            }
        }).collect(Collectors.toList());

        List<TCameraInfo> oldCameraInfoList = tCameraInfoMapper.selectByEdgeCode(edgeNode);
        if (CollectionUtils.isEmpty(oldCameraInfoList)) {
            tCameraInfoMapper.insertBatch(tCameraInfoList);
            dealDevicePermission(tCameraInfoList.stream().map(TCameraInfo::getCameraId).collect(Collectors.toList()));
        } else {
            Map<String, TCameraInfo> oldCameraInfoMap = oldCameraInfoList.stream().collect(Collectors.toMap(TCameraInfo::getOriginId, Function.identity(), (e1, e2) -> e1));
            Map<String, TCameraInfo> newCameraInfoMap = tCameraInfoList.stream().collect(Collectors.toMap(TCameraInfo::getOriginId, Function.identity(), (e1, e2) -> e1));
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
                dealDevicePermission(insertCameraInfoList.stream().map(TCameraInfo::getCameraId).collect(Collectors.toList()));
            }
        }
    }



    private void dealDevicePermission(List<Long> ids) {
        //更新权限信息
        List<Long> userIds = sysUserDao.selectIdsByRoleId(1235L);
        if (CollectionUtils.isNotEmpty(ids) && CollectionUtils.isNotEmpty(userIds)) {
            //组装权限内容
            List<SysUserDevicePermission> sysUserDevicePermissions = new ArrayList<>();
            ids.forEach(id -> {
                for (Long userId : userIds) {
                    SysUserDevicePermission sysUserDevicePermission = new SysUserDevicePermission();
                    sysUserDevicePermission.setUserId(userId);
                    sysUserDevicePermission.setMonitorDeviceId(id);
                    sysUserDevicePermissions.add(sysUserDevicePermission);
                }
            });
            sysUserDao.batchInsertDevicePermission(sysUserDevicePermissions);
        }
    }
}
