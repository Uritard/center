package com.yjh.accessrobot.module.command.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yjh.accessrobot.module.command.dao.*;
import com.yjh.accessrobot.module.command.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hyh
 * @since 2022/11/10
 **/
@Service
@Slf4j
public class TStdDeviceModelService {

    @Resource
    private TStdRegionDao tStdRegionDao;
    @Resource
    private TStdDeviceMapper tStdDeviceMapper;
    @Resource
    private TStdDevicemeteMapper tStdDevicemeteMapper;
    @Resource
    private TCameraInfoMapper tCameraInfoMapper;
    @Resource
    private TRobotInfoDao tRobotInfoDao;
    @Resource
    private TCameraPresetMapper tCameraPresetMapper;
    @Resource
    private TRobotInspectionDao tRobotInspectionDao;
    @Resource
    private TCruisePointInstanceMapper tCruisePointInstanceMapper;
    @Resource
    private TVoiceDeviceMapper tVoiceDeviceMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveReportData(List<Map<String, Object>> deviceModelList, String ftpsPath, String presetPath, String presetRealPath, String edgeCode) {
        //字典表转换
        List<Map<String, String>> dictMapList = tStdDeviceMapper.selectDictCodeByUpDict("device_type");
        //已存在的节点信息
        List<TStdRegion> tStdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeCode, null);
        //已存在的相机信息
        List<TCameraInfo> tCameraInfoList = tCameraInfoMapper.selectByEdgeCode(edgeCode);
        //已存在的机器人信息
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.selectByEdgeCode(edgeCode);
        //已存在的声纹信息
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceMapper.selectByEdgeCode(edgeCode);
        //old 设备信息
        List<TStdDevice> oldStdDeviceList = tStdDeviceMapper.selectByEdgeCode(edgeCode);
        //old 测点信息
        List<TStdDeviceMete> oldStdDeviceMeteList = tStdDevicemeteMapper.selectByEdgeCode(edgeCode);
        //old 预置位信息
        List<TCameraPreset> oldCameraPresetList = tCameraPresetMapper.selectByEdgeCode(edgeCode);
        //old 机器人测点信息
        List<TRobotInspection> oldRobotInspectionList = tRobotInspectionDao.selectByEdgeCode(edgeCode);
        //old 巡视点信息
        List<TCruisePointInstance> oldCruisePointInstanceList = tCruisePointInstanceMapper.selectByEdgeCode(edgeCode);

        List<TStdDevice> finalDeviceList = new ArrayList<>();
        List<TCameraPreset> finalCameraPresetList = new ArrayList<>();
        List<TRobotInspection> finalRobotInspectionList = new ArrayList<>();
        List<TStdDeviceMete> finalStdDeviceMeteList = new ArrayList<>();
        List<TCruisePointInstance> newCruisePointInstances = new ArrayList<>();
        deviceModelList.stream().filter(d -> !JSON.parseObject(JSONArray.parseArray(d.get("video_pos").toString()).get(0).toString()).entrySet().isEmpty())
                .forEach(device -> {
                    //构建 t_std_device
                    TStdDevice tStdDevice = new TStdDevice();
                    tStdDevice.setEdgeCode(edgeCode);
                    tStdDevice.setOriginId(String.valueOf(device.get("main_device_id")));
                    tStdDevice.setDeviceName(String.valueOf(device.get("main_device_name")));
                    tStdDevice.setUpRegionId(tStdRegionList.stream().filter(t -> device.get("bay_id").equals(String.valueOf(t.getOriginRegionId()))).map(TStdRegion::getRegionId).collect(Collectors.toList()).get(0));
                    tStdDevice.setUpRegionName(String.valueOf(device.get("bay_name")));
                    tStdDevice.setDeviceType(CollectionUtils.isNotEmpty(dictMapList) ? Integer.parseInt(dictMapList.stream().filter(s -> "device_type".equals(s.get("col_name")) && device.get("device_type").equals(String.valueOf(s.get("up_dict")))).map(d -> d.get("dict_code")).collect(Collectors.toList()).get(0)) : null);
                    if (device.containsKey("real_code")) {
                        tStdDevice.setRealCode(String.valueOf(device.get("real_code")));
                    }
                    finalDeviceList.add(tStdDevice);
                    long cruiseId;
                    int cruiseType;
                    switch (String.valueOf(device.get("data_type"))) {
                        case "1":
                            //视频
                            //构建 t_camera_preset
                            TCameraPreset tCameraPreset = new TCameraPreset();
                            JSONArray cameraArray = JSONArray.parseArray(device.get("video_pos").toString());
                            tCameraPreset.setEdgeCode(edgeCode);
                            cruiseId = Long.parseLong(JSON.parseObject(cameraArray.get(0).toString()).get("device_pos").toString());
                            tCameraPreset.setOriginId(String.valueOf(cruiseId));
                            String cameraOriginId = JSON.parseObject(cameraArray.get(0).toString()).get("device_code").toString();
                            TCameraInfo tCameraInfo = tCameraInfoList.stream().filter(t -> cameraOriginId.equals(t.getOriginId()) && edgeCode.equals(t.getEdgeCode())).collect(Collectors.toList()).get(0);
                            tCameraPreset.setCameraId(tCameraInfo.getCameraId());
                            tCameraPreset.setPresetName(String.valueOf(device.get("device_name")).contains("/") ? StringUtils.substringAfter(device.get("device_name").toString(), "/") : String.valueOf(device.get("device_name")));
                            finalCameraPresetList.add(tCameraPreset);
                            cruiseType = tCameraInfo.getCameraType() == 206 ? 230 : 229;
                            break;
                        case "01":
                        case "001":
                            //机器人、无人机
                            //构建 t_robot_inspection
                            TRobotInspection tRobotInspection = new TRobotInspection();
                            JSONArray robotArray = JSONArray.parseArray(device.get("video_pos").toString());
                            String keyPos = "01".equals(device.get("data_type")) ? "robot_pos" : "uav_pos";
                            String keyCode = "01".equals(device.get("data_type")) ? "robot_code" : "uav_code";
                            tRobotInspection.setEdgeCode(edgeCode);
                            cruiseId = Long.parseLong(JSON.parseObject(robotArray.get(0).toString()).get(keyPos).toString());
                            cruiseType = "01".equals(device.get("data_type")) ? 228 : 524;
                            tRobotInspection.setOriginId(String.valueOf(cruiseId));
                            TRobotInfo tRobotInfo = tRobotInfoList.stream().filter(t -> String.valueOf(robotArray.getJSONObject(0).get(keyCode)).equals(String.valueOf(t.getRobotNum()))).collect(Collectors.toList()).get(0);
                            tRobotInspection.setRobotId(tRobotInfo.getRobotId());
                            tRobotInspection.setInspectionName(String.valueOf(device.get("device_name")).contains("/") ? StringUtils.substringAfter(device.get("device_name").toString(), "/") : String.valueOf(device.get("device_name")));
                            finalRobotInspectionList.add(tRobotInspection);
                            break;
                        case "0001":
                            //声纹
                            JSONArray voiceArray = JSONArray.parseArray(device.get("video_pos").toString());
                            boolean voiceFlag = JSON.parseObject(voiceArray.get(0).toString()).containsKey("voice_pos");
                            cruiseId = voiceFlag ? Long.parseLong(JSON.parseObject(voiceArray.get(0).toString()).get("voice_pos").toString()) : Long.parseLong(JSON.parseObject(voiceArray.get(0).toString()).get("device_pos").toString());
                            cruiseType = 232;
                            break;
                        default:
                            return;
                    }
                    //构建 t_std_devicemete
                    TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
                    tStdDeviceMete.setMeteName(String.valueOf(device.get("device_name")).contains("/") ? StringUtils.substringBefore(device.get("device_name").toString().replace(" ", ""), "/") : String.valueOf(device.get("device_name")));
                    tStdDeviceMete.setDeviceId(Long.valueOf(tStdDevice.getOriginId()));
                    tStdDeviceMete.setCustomId(String.valueOf(device.get("component_id")));
                    tStdDeviceMete.setCustomName(String.valueOf(device.get("component_name")));
                    if (device.containsKey("device_mete_id")) {
                        tStdDeviceMete.setOriginId(String.valueOf(device.get("device_mete_id")));
                    } else {
                        tStdDeviceMete.setOriginId(String.valueOf(device.get("device_id")));
                    }
                    tStdDeviceMete.setEdgeCode(edgeCode);
                    tStdDeviceMete.setRedundantType("1");
                    finalStdDeviceMeteList.add(tStdDeviceMete);
                    //构建 t_cruise_point_instance
                    TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
                    tCruisePointInstance.setDeviceMeteId(Long.valueOf(tStdDeviceMete.getOriginId()));
                    tCruisePointInstance.setDeviceId(Long.valueOf(tStdDevice.getOriginId()));
                    tCruisePointInstance.setCustomId(tStdDeviceMete.getCustomId());
                    tCruisePointInstance.setCruiseId(cruiseId);
                    tCruisePointInstance.setCruiseName(String.valueOf(device.get("device_name")).contains("/") ? StringUtils.substringAfter(device.get("device_name").toString(), "/") : String.valueOf(device.get("device_name")));
                    tCruisePointInstance.setOriginId(String.valueOf(device.get("device_id")));
                    tCruisePointInstance.setEdgeCode(edgeCode);
                    tCruisePointInstance.setCruiseType(cruiseType);
                    newCruisePointInstances.add(tCruisePointInstance);

                });
        //去重
        List<TCameraPreset> newCameraPresets = finalCameraPresetList.stream().distinct().collect(Collectors.toList());
        List<TRobotInspection> newRobotInspections = finalRobotInspectionList.stream().distinct().collect(Collectors.toList());
        List<TStdDevice> newStdDevices = finalDeviceList.stream().distinct().collect(Collectors.toList());
        List<TStdDeviceMete> newStdDeviceMetes = finalStdDeviceMeteList.stream().distinct().collect(Collectors.toList());

        //该节点未同步过  直接入库
        if (CollectionUtils.isEmpty(oldCruisePointInstanceList)) {
            insertDeviceModel(newCameraPresets, newRobotInspections, newStdDevices, newStdDeviceMetes, newCruisePointInstances, tVoiceDeviceList);
        } else {
            //将旧的数据和新的数据进行对比相同的去掉  不通的更新  新增的直接入库
            //预置位
            List<TCameraPreset> insertCameraInfoList = dealCameraPreset(oldCameraPresetList, newCameraPresets, edgeCode);
            //机器人测点
            List<TRobotInspection> insertRobotInfoList = dealRobotInspection(oldRobotInspectionList, newRobotInspections, edgeCode);
            //设备
            List<TStdDevice> insertStdDevice = dealStdDevice(oldStdDeviceList, newStdDevices, edgeCode);
            //测点
            List<TStdDeviceMete> insertStdDeviceMete = dealStdDeviceMete(oldStdDeviceMeteList, newStdDeviceMetes, edgeCode);
            //巡视点
            List<TCruisePointInstance> insertCruisePointInstanceList = dealCruisePointInstance(oldCruisePointInstanceList, newCruisePointInstances, edgeCode);
            //新增的数据入库
            insertDeviceModel(insertCameraInfoList, insertRobotInfoList, insertStdDevice, insertStdDeviceMete, insertCruisePointInstanceList, tVoiceDeviceList);
        }
    }

    /**
     * 预置位
     * @param oldList 旧数据
     * @param newList 新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TCameraPreset> dealCameraPreset(List<TCameraPreset> oldList, List<TCameraPreset> newList, String edgeCode){
        Map<String, TCameraPreset> oldMap = oldList.stream().collect(Collectors.toMap(TCameraPreset::getOriginId, Function.identity()));
        Map<String, TCameraPreset> newMap = newList.stream().collect(Collectors.toMap(TCameraPreset::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TCameraPreset old = oldMap.get(t.getOriginId());
                t.setPresetId(old.getPresetId());
                tCameraPresetMapper.updateByPrimaryKey(t);
            });
        }
        //删除的预置位数据
        SetUtils.SetView<String> deleteIdSet=SetUtils.difference(oldMap.keySet(),newMap.keySet());
        if(CollectionUtils.isNotEmpty(deleteIdSet)){
            tCameraPresetMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的预置位数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TCameraPreset> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
        }
        return insertList;
    }

    /**
     * 机器人测点
     * @param oldList 旧数据
     * @param newList 新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TRobotInspection> dealRobotInspection(List<TRobotInspection> oldList, List<TRobotInspection> newList, String edgeCode){
        Map<String, TRobotInspection> oldMap = oldList.stream().collect(Collectors.toMap(TRobotInspection::getOriginId, Function.identity()));
        Map<String, TRobotInspection> newMap = newList.stream().collect(Collectors.toMap(TRobotInspection::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TRobotInspection old = oldMap.get(t.getOriginId());
                t.setInspectionId(old.getInspectionId());
                tRobotInspectionDao.updateByPrimaryKey(t);
            });
        }
        //删除的预置位数据
        SetUtils.SetView<String> deleteIdSet=SetUtils.difference(oldMap.keySet(),newMap.keySet());
        if(CollectionUtils.isNotEmpty(deleteIdSet)){
            tRobotInspectionDao.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的预置位数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TRobotInspection> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
        }
        return insertList;
    }

    /**
     * 巡视点
     * @param oldList 旧数据
     * @param newList 新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TCruisePointInstance> dealCruisePointInstance(List<TCruisePointInstance> oldList, List<TCruisePointInstance> newList, String edgeCode){
        Map<String, TCruisePointInstance> oldMap = oldList.stream().collect(Collectors.toMap(TCruisePointInstance::getOriginId, Function.identity()));
        Map<String, TCruisePointInstance> newMap = newList.stream().collect(Collectors.toMap(TCruisePointInstance::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TCruisePointInstance old = oldMap.get(t.getOriginId());
                t.setInstanceId(old.getInstanceId());
                tCruisePointInstanceMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet=SetUtils.difference(oldMap.keySet(),newMap.keySet());
        if(CollectionUtils.isNotEmpty(deleteIdSet)){
            tCruisePointInstanceMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TCruisePointInstance> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
        }
        return insertList;
    }

    /**
     * 测点
     * @param oldList 旧数据
     * @param newList 新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TStdDeviceMete> dealStdDeviceMete(List<TStdDeviceMete> oldList, List<TStdDeviceMete> newList, String edgeCode){
        Map<String, TStdDeviceMete> oldMap = oldList.stream().collect(Collectors.toMap(TStdDeviceMete::getOriginId, Function.identity()));
        Map<String, TStdDeviceMete> newMap = newList.stream().collect(Collectors.toMap(TStdDeviceMete::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TStdDeviceMete old = oldMap.get(t.getOriginId());
                t.setDeviceMeteId(old.getDeviceMeteId());
                tStdDevicemeteMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet=SetUtils.difference(oldMap.keySet(),newMap.keySet());
        if(CollectionUtils.isNotEmpty(deleteIdSet)){
            tStdDevicemeteMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TStdDeviceMete> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
        }
        return insertList;
    }

    /**
     * 设备
     * @param oldList 旧数据
     * @param newList 新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TStdDevice> dealStdDevice(List<TStdDevice> oldList, List<TStdDevice> newList, String edgeCode){
        Map<String, TStdDevice> oldMap = oldList.stream().collect(Collectors.toMap(TStdDevice::getOriginId, Function.identity()));
        Map<String, TStdDevice> newMap = newList.stream().collect(Collectors.toMap(TStdDevice::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TStdDevice old = oldMap.get(t.getOriginId());
                t.setDeviceId(old.getDeviceId());
                tStdDeviceMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet=SetUtils.difference(oldMap.keySet(),newMap.keySet());
        if(CollectionUtils.isNotEmpty(deleteIdSet)){
            tStdDeviceMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TStdDevice> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
        }
        return insertList;
    }

    /**
     * 新增数据 直接入库
     * @param newCameraPresets  新的预置位信息
     * @param newRobotInspections  新的机器人测点信息
     * @param newStdDevices  新的设备信息
     * @param newStdDeviceMetes  新的测点信息
     * @param newCruisePointInstances  新的巡视点信息
     * @param tVoiceDeviceList  声纹设备信息
     */
    private void insertDeviceModel(List<TCameraPreset> newCameraPresets, List<TRobotInspection> newRobotInspections,
                                   List<TStdDevice> newStdDevices, List<TStdDeviceMete> newStdDeviceMetes,
                                   List<TCruisePointInstance> newCruisePointInstances, List<TVoiceDevice> tVoiceDeviceList){
        if (CollectionUtils.isNotEmpty(newCameraPresets)) {
            tCameraPresetMapper.batchInsert(newCameraPresets);
        }
        if (CollectionUtils.isNotEmpty(newRobotInspections)){
            tRobotInspectionDao.batchInsertTRobotInspection(newRobotInspections);
        }
        if (CollectionUtils.isNotEmpty(newStdDevices)) {
            tStdDeviceMapper.batchInsert(newStdDevices);
            newStdDeviceMetes.forEach(tStdDeviceMete -> {
                tStdDeviceMete.setDeviceId(newStdDevices.stream().filter(tStdDevice ->
                                String.valueOf(tStdDeviceMete.getDeviceId()).equals(tStdDevice.getOriginId()))
                        .collect(Collectors.toList()).get(0).getDeviceId());
            });
            tStdDevicemeteMapper.batchInsert(newStdDeviceMetes);
            newCruisePointInstances.forEach(tCruisePointInstance -> {
                tCruisePointInstance.setDeviceId(newStdDevices.stream().filter(tStdDevice ->
                                String.valueOf(tCruisePointInstance.getDeviceId()).equals(tStdDevice.getOriginId()))
                        .collect(Collectors.toList()).get(0).getDeviceId());
                tCruisePointInstance.setDeviceMeteId(newStdDeviceMetes.stream().filter(tStdDeviceMete ->
                                String.valueOf(tCruisePointInstance.getDeviceMeteId()).equals(tStdDeviceMete.getOriginId()))
                        .collect(Collectors.toList()).get(0).getDeviceMeteId());
                switch (tCruisePointInstance.getCruiseType()) {
                    case 230:
                    case 229:
                        tCruisePointInstance.setCruiseId(newCameraPresets.stream().filter(tCameraPreset ->
                                        String.valueOf(tCruisePointInstance.getCruiseId()).equals(tCameraPreset.getOriginId()))
                                .collect(Collectors.toList()).get(0).getPresetId());
                        break;
                    case 228:
                    case 524:
                        tCruisePointInstance.setCruiseId(newRobotInspections.stream().filter(tRobotInspection ->
                                        String.valueOf(tCruisePointInstance.getCruiseId()).equals(tRobotInspection.getOriginId()))
                                .collect(Collectors.toList()).get(0).getInspectionId());
                        break;
                    default:
                        tCruisePointInstance.setCruiseId(tVoiceDeviceList.stream().filter(tVoiceDevice ->
                                        String.valueOf(tCruisePointInstance.getCruiseId()).equals(tVoiceDevice.getOriginId()))
                                .collect(Collectors.toList()).get(0).getVoiceDeviceId());
                        break;
                }
            });
            tCruisePointInstanceMapper.batchInsert(newCruisePointInstances);
        }
    }
}
