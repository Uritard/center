/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.*;
import com.yjh.accessrobot.module.command.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备维护记录同步类
 *
 * @author Chenfei
 * @date 2024/6/14
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceMaintenanceService {
    private final DeviceMaintenanceInfoDao deviceMaintenanceInfoDao;
    private final TCameraInfoMapper tCameraInfoMapper;
    private final TCameraRecorderDao tCameraRecorderDao;
    private final TVoiceDeviceMapper tVoiceDeviceMapper;
    private final TRobotInfoDao tRobotInfoDao;

    public void saveReportData(List<DeviceMaintenanceInfo> maintenanceList, String edgeNode) {
        log.info("开始设备维护记录信息  edgeNode:{}  ", edgeNode);
        if (CollectionUtils.isEmpty(maintenanceList)) {
            log.warn("设备维护信息为空");
            return;
        }
        List<DeviceMaintenanceInfo> oldMaintenanceList = deviceMaintenanceInfoDao.selectAll(edgeNode);
        Map<String, DeviceMaintenanceInfo> oldMaintenanceMap =
            oldMaintenanceList.stream().collect(Collectors.toMap(DeviceMaintenanceInfo::getRecordCode, Function.identity()));

        List<DeviceMaintenanceInfo> maintenanceUpdateList = new ArrayList<>();
        List<DeviceMaintenanceInfo> maintenanceInsertList = new ArrayList<>();

        if (CollectionUtils.isEmpty(oldMaintenanceList)) {
            maintenanceInsertList.addAll(maintenanceList);
        } else {
            for (DeviceMaintenanceInfo info : maintenanceList) {
                DeviceMaintenanceInfo old = oldMaintenanceMap.get(info.getRecordCode());
                if (old == null) {
                    maintenanceInsertList.add(info);
                } else if (!old.equals(info)) {
                    info.setDeviceId(old.getDeviceId());
                    info.setUpdateTime(new Date());
                    info.setId(old.getId());
                    maintenanceUpdateList.add(info);
                }
            }
        }
        Set<String> newMaintenanceSet =
            maintenanceList.stream().map(DeviceMaintenanceInfo::getPatroldeviceCode).collect(Collectors.toSet());

        // 更新的数据
        if (CollectionUtils.isNotEmpty(maintenanceUpdateList)) {
            deviceMaintenanceInfoDao.batchUpdate(maintenanceUpdateList);
        }
        //TODO 删除的数据 没有区分节点，暂不做删除
        /*Set<String> deleteIdSet = SetUtils.difference(oldMaintenanceMap.keySet(), newMaintenanceSet);
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            deviceMaintenanceInfoDao.deleteByCodes(edgeNode, deleteIdSet);
        }*/
        //新增的数据
        if (CollectionUtils.isNotEmpty(maintenanceInsertList)) {
            Map<Integer, Map<String, Long>> foundDeviceId = foundDeviceId(maintenanceInsertList, edgeNode);
            // 设置当前节点的 deviceId
            maintenanceInsertList.forEach(m -> {
                Integer type = m.getPatroldeviceType();
                type = type == null || type == 2 || type == 3 ? 1 : type;
                Long deviceId = foundDeviceId.getOrDefault(type, Collections.emptyMap()).get(m.getPatroldeviceCode());
                m.setDeviceId(deviceId);
            });
            deviceMaintenanceInfoDao.insertBatch(maintenanceInsertList);
        }
    }

    /**
     * 获取所有维护记录对应deviceId
     */
    Map<Integer, Map<String, Long>> foundDeviceId(List<DeviceMaintenanceInfo> maintenanceList, String edgeNode) {

        Map<Integer, Set<String>> maintenanceGroupMap = maintenanceList.stream()
            .collect(Collectors.groupingBy(DeviceMaintenanceInfo::getPatroldeviceType,
                Collectors.mapping(DeviceMaintenanceInfo::getPatroldeviceCode, Collectors.toSet())));

        Map<Integer, Map<String, Long>> map = new HashMap<>();
        AtomicBoolean hasRobot = new AtomicBoolean(false);
        maintenanceGroupMap.forEach((k, v) -> {
            switch (k) {
                case 1:
                case 2:
                case 3:
                case 13:
                    // 机器人, 无人机 稍后统一查询
                    hasRobot.set(true);
                    break;
                case 10:
                    // 高清相机
                    List<TCameraInfo> cameraInfos = tCameraInfoMapper.selectByEdgeCode(edgeNode);
                    Map<String, Long> cameraMap = cameraInfos.stream()
                        .collect(Collectors.toMap(TCameraInfo::getCameraChannelId, TCameraInfo::getCameraId, (e1, e2) -> e1));
                    map.put(10, cameraMap);
                    break;
                case 11:
                    // nvr
                    List<TCameraRecorder> recorders = tCameraRecorderDao.selectByEdgeCode(edgeNode);
                    Map<String, Long> recorderMap = recorders.stream()
                        .collect(Collectors.toMap(TCameraRecorder::getDeviceChannel, TCameraRecorder::getRecordId, (e1, e2) -> e1));
                    map.put(11, recorderMap);
                    break;
                case 14:
                    // 声纹
                    List<TVoiceDevice> voiceDevices = tVoiceDeviceMapper.selectByEdgeCode(edgeNode);
                    Map<String, Long> voiceMap = voiceDevices.stream()
                        .collect(Collectors.toMap(TVoiceDevice::getVoiceCode, TVoiceDevice::getVoiceDeviceId, (e1, e2) -> e1));
                    map.put(14, voiceMap);
                    break;
                default:
                    log.warn("设备类型不存在 {}", k);
            }
        });
        if (hasRobot.get()) {
            List<TRobotInfo> robotInfoList = tRobotInfoDao.selectByEdgeCode(edgeNode);
            Map<Integer, Map<String, Long>> robotMap = robotInfoList.stream()
                .peek(r -> {
                    if (r.getDroneType() != null) {
                        r.setRobotType(13);
                    } else {
                        r.setRobotType(1);
                    }
                })
                .collect(Collectors.groupingBy(TRobotInfo::getRobotType,
                    Collectors.toMap(TRobotInfo::getRobotCode, TRobotInfo::getRobotId, (e1, e2) -> e1)));
            map.putAll(robotMap);
        }
        return map;
    }
}
