package com.yjh.accessrobot.module.command.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yjh.accessrobot.commons.utils.file.FileUtil;
import com.yjh.accessrobot.module.command.dao.*;
import com.yjh.accessrobot.module.command.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
    private TAlgorithmMeteMapper tAlgorithmMeteMapper;
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
    public void saveReportData(List<Map<String, Object>> deviceModelList, String ftpsPath, String presetPath, String presetRealPath, String edgeCode, String edgeLevel) {
        if (CollectionUtils.isNotEmpty(deviceModelList)) {
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
            //old 测点对应算法类型表信息
            List<TAlgorithmMete> oldAlgorithmMeteList = tAlgorithmMeteMapper.selectByEdgeCode(edgeCode);
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
            List<TAlgorithmMete> finalAlgorithmMeteList = new ArrayList<>();
            List<TCruisePointInstance> newCruisePointInstances = new ArrayList<>();
            deviceModelList.stream().filter(d -> !JSON.parseObject(JSONArray.parseArray(d.get("video_pos").toString()).get(0).toString()).entrySet().isEmpty())
                    .forEach(device -> {
                        //构建 t_std_device
                        TStdDevice tStdDevice = createStdDevice(edgeCode, device, tStdRegionList, dictMapList);
                        finalDeviceList.add(tStdDevice);
                        long cruiseId;
                        int cruiseType;
                        switch (String.valueOf(device.get("data_type"))) {
                            case "1":
                                //视频
                                //构建 t_camera_preset
                                JSONArray cameraArray = JSONArray.parseArray(device.get("video_pos").toString());
                                String cameraOriginId = JSON.parseObject(cameraArray.get(0).toString()).get("device_code").toString();
                                TCameraInfo tCameraInfo = tCameraInfoList.stream().filter(t -> cameraOriginId.equals(t.getOriginId()) && edgeCode.equals(t.getEdgeCode())).collect(Collectors.toList()).get(0);
                                TCameraPreset tCameraPreset = createCameraPreset(edgeCode, device, cameraArray, tCameraInfo.getCameraId(), edgeLevel, ftpsPath, presetPath, presetRealPath);
                                cruiseId = Long.parseLong(JSON.parseObject(cameraArray.get(0).toString()).get("device_pos").toString());
                                cruiseType = tCameraInfo.getCameraType() == 206 ? 230 : 229;
                                finalCameraPresetList.add(tCameraPreset);
                                break;
                            case "01":
                            case "001":
                                //机器人、无人机
                                //构建 t_robot_inspection
                                JSONArray robotArray = JSONArray.parseArray(device.get("video_pos").toString());
                                String keyPos = "01".equals(device.get("data_type")) ? "robot_pos" : "uav_pos";
                                cruiseId = Long.parseLong(JSON.parseObject(robotArray.get(0).toString()).get(keyPos).toString());
                                cruiseType = "01".equals(device.get("data_type")) ? 228 : 524;
                                TRobotInspection tRobotInspection = createRobotInspection(edgeCode, cruiseId, device, tRobotInfoList, robotArray);
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
                        TStdDeviceMete tStdDeviceMete = createStdDeviceMete(edgeCode, device, tStdDevice);
                        if (EdgeEnum.REGION_NODE.getCode().equals(edgeLevel)) {
                            tStdDeviceMete = convertDeviceMete(device);
                            if (device.containsKey("algorithm_id") && StringUtils.isNotEmpty(device.get("algorithm_id").toString())) {
                                TAlgorithmMete tAlgorithmMete = new TAlgorithmMete();
                                tAlgorithmMete.setEdgeCode(edgeCode);
                                tAlgorithmMete.setOriginId(tStdDeviceMete.getOriginId());
                                tAlgorithmMete.setAlgorithmId((Long) device.get("algorithm_id"));
                                finalAlgorithmMeteList.add(tAlgorithmMete);
                            }
                        }
                        finalStdDeviceMeteList.add(tStdDeviceMete);
                        //构建 t_cruise_point_instance
                        TCruisePointInstance tCruisePointInstance = createCruisePointInstance(edgeCode, cruiseId, cruiseType, device, tStdDevice, tStdDeviceMete);
                        newCruisePointInstances.add(tCruisePointInstance);

                    });
            //去重
            List<TCameraPreset> newCameraPresets = finalCameraPresetList.stream().distinct().collect(Collectors.toList());
            List<TRobotInspection> newRobotInspections = finalRobotInspectionList.stream().distinct().collect(Collectors.toList());
            List<TStdDevice> newStdDevices = finalDeviceList.stream().distinct().collect(Collectors.toList());
            List<TStdDeviceMete> newStdDeviceMetes = finalStdDeviceMeteList.stream().distinct().collect(Collectors.toList());
            List<TAlgorithmMete> newAlgorithmMetes = finalAlgorithmMeteList.stream().distinct().collect(Collectors.toList());

            //将旧的数据和新的数据进行对比相同的去掉  不同的更新  新增的直接入库
            //预置位
            List<TCameraPreset> insertCameraPresetList = dealCameraPreset(oldCameraPresetList, newCameraPresets, edgeCode);
            insertCameraPresetList.addAll(oldCameraPresetList);
            //机器人测点
            List<TRobotInspection> insertRobotInspectionList = dealRobotInspection(oldRobotInspectionList, newRobotInspections, edgeCode);
            insertRobotInspectionList.addAll(oldRobotInspectionList);
            //设备点位
            List<TStdDevice> insertStdDevice = dealStdDevice(oldStdDeviceList, newStdDevices, edgeCode);
            insertStdDevice.addAll(oldStdDeviceList);
            //标准测点
            List<TStdDeviceMete> insertStdDeviceMete = dealStdDeviceMete(insertStdDevice, oldStdDeviceMeteList, newStdDeviceMetes, edgeCode);
            insertStdDeviceMete.addAll(oldStdDeviceMeteList);
            //标准测点算法关系信息
            if (CollectionUtils.isNotEmpty(newAlgorithmMetes)) {
                dealAlgorithmMete(insertStdDeviceMete, oldAlgorithmMeteList, newAlgorithmMetes, edgeCode);
            }
            //巡视点
            dealCruisePointInstance(insertCameraPresetList, insertRobotInspectionList, insertStdDevice, insertStdDeviceMete,
                    oldCruisePointInstanceList, newCruisePointInstances, tVoiceDeviceList, edgeCode);
        } else {
            //模型为空 删除该节点的所有信息
            tCameraPresetMapper.deleteByEdgeCodeAndOriginId(edgeCode, null);
            tRobotInspectionDao.deleteByEdgeCodeAndOriginId(edgeCode, null);
            tCruisePointInstanceMapper.deleteByEdgeCodeAndOriginId(edgeCode, null);
            tAlgorithmMeteMapper.deleteByEdgeCodeAndOriginId(edgeCode, null);
            tStdDevicemeteMapper.deleteByEdgeCodeAndOriginId(edgeCode, null);
            tStdDeviceMapper.deleteByEdgeCodeAndOriginId(edgeCode, null);
        }
    }

    /**
     * 创建 TCruisePointInstance 表信息
     * @param edgeCode
     * @param cruiseId
     * @param cruiseType
     * @param device
     * @param tStdDevice
     * @param tStdDeviceMete
     * @return
     */
    private TCruisePointInstance createCruisePointInstance(String edgeCode, long cruiseId, int cruiseType,
                                                           Map<String, Object> device, TStdDevice tStdDevice,
                                                           TStdDeviceMete tStdDeviceMete) {
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(tStdDeviceMete.getOriginId()));
        tCruisePointInstance.setDeviceId(Long.valueOf(tStdDevice.getOriginId()));
        tCruisePointInstance.setCustomId(tStdDeviceMete.getCustomId());
        tCruisePointInstance.setCruiseId(cruiseId);
        tCruisePointInstance.setCruiseName(String.valueOf(device.get("device_name")).contains("/")
                ? StringUtils.substringAfter(device.get("device_name").toString(), "/")
                : String.valueOf(device.get("device_name")));
        tCruisePointInstance.setOriginId(String.valueOf(device.get("device_id")));
        tCruisePointInstance.setEdgeCode(edgeCode);
        tCruisePointInstance.setCruiseType(cruiseType);
        return tCruisePointInstance;
    }

    /**
     *创建 TStdDeviceMete 表信息
     * @param edgeCode
     * @param device
     * @param tStdDevice
     * @return
     */
    private TStdDeviceMete createStdDeviceMete(String edgeCode, Map<String, Object> device, TStdDevice tStdDevice) {
        TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
        tStdDeviceMete.setMeteName(String.valueOf(device.get("device_name")).contains("/")
                ? StringUtils.substringBefore(device.get("device_name").toString().replace(" ", ""), "/")
                : String.valueOf(device.get("device_name")));
        tStdDeviceMete.setDeviceId(Long.valueOf(tStdDevice.getOriginId()));
        tStdDeviceMete.setCustomId(String.valueOf(device.get("component_id")));
        tStdDeviceMete.setCustomName(String.valueOf(device.get("component_name")));
        if (device.containsKey("device_mete_id")) {
            tStdDeviceMete.setOriginId(String.valueOf(device.get("device_mete_id")));
        } else {
            tStdDeviceMete.setOriginId(String.valueOf(device.get("device_id")));
        }
        tStdDeviceMete.setEdgeCode(edgeCode);
        tStdDeviceMete.setRedundantType(String.valueOf(device.getOrDefault("redundant_type", "1")));
        return tStdDeviceMete;
    }

    /**
     * 创建 TRobotInspection 表信息
     * @param edgeCode
     * @param cruiseId
     * @param device
     * @param tRobotInfoList
     * @param robotArray
     * @return
     */
    private TRobotInspection createRobotInspection(String edgeCode, long cruiseId, Map<String, Object> device,
                                                   List<TRobotInfo> tRobotInfoList, JSONArray robotArray) {
        TRobotInspection tRobotInspection = new TRobotInspection();
        String keyCode = "01".equals(device.get("data_type")) ? "robot_code" : "uav_code";
        tRobotInspection.setEdgeCode(edgeCode);
        tRobotInspection.setOriginId(String.valueOf(cruiseId));
        TRobotInfo tRobotInfo = tRobotInfoList.stream().filter(t ->
                String.valueOf(robotArray.getJSONObject(0).get(keyCode)).equals(String.valueOf(t.getRobotNum())))
                .collect(Collectors.toList()).get(0);
        tRobotInspection.setRobotId(tRobotInfo.getRobotId());
        tRobotInspection.setInspectionName(String.valueOf(device.get("device_name")).contains("/")
                ? StringUtils.substringAfter(device.get("device_name").toString(), "/")
                : String.valueOf(device.get("device_name")));
        return tRobotInspection;
    }

    /**
     * 创建 TCameraPreset 表信息
     * @param edgeCode
     * @param device
     * @param cameraArray
     * @param cameraId
     * @param edgeLevel
     * @param ftpsPath
     * @param presetPath
     * @param presetRealPath
     * @return
     */
    private TCameraPreset createCameraPreset(String edgeCode, Map<String, Object> device, JSONArray cameraArray,
                                             Long cameraId, String edgeLevel, String ftpsPath,
                                             String presetPath, String presetRealPath) {
        TCameraPreset tCameraPreset = new TCameraPreset();
        tCameraPreset.setEdgeCode(edgeCode);
        long cruiseId = Long.parseLong(JSON.parseObject(cameraArray.get(0).toString()).get("device_pos").toString());
        tCameraPreset.setOriginId(String.valueOf(cruiseId));
        tCameraPreset.setCameraId(cameraId);
        tCameraPreset.setPresetName(String.valueOf(device.get("device_name")).contains("/")
                ? StringUtils.substringAfter(device.get("device_name").toString(), "/")
                : String.valueOf(device.get("device_name")));
        //当节点为巡视主机接入边缘节点时再触发
        if (EdgeEnum.REGION_NODE.getCode().equals(edgeLevel)) {
            tCameraPreset.setPresetNum((Integer) device.getOrDefault("preset_num", 1));
            tCameraPreset.setIsKeepWatch((Integer) device.getOrDefault("is_keep_watch", 0));
            tCameraPreset.setIsKeepWatchTask((Integer) device.getOrDefault("is_keep_watch_task", 0));
            tCameraPreset.setIsSecondKeepWatchTask((Integer) device.getOrDefault("is_second_keep_watch_task", 0));
            if (device.containsKey("preset_img")) {
                String presetFtpsImg = ftpsPath + device.get("preset_img").toString();
                String presetImg = presetPath + device.get("preset_img").toString();
                File ftpsFile = new File(presetFtpsImg);
                if (ftpsFile.exists()) {
                    try {
                        FileUtil.copyFileUsingStream(presetFtpsImg, presetImg);
                        tCameraPreset.setPresetImg(presetRealPath + device.get("preset_img").toString());
                    } catch (IOException e) {
                        log.error("预置位文件拷贝失败", e);
                    }
                }
            }
        }
        return tCameraPreset;
    }

    /**
     * 创建 TStdDevice 表信息
     * @param edgeCode 节点编码
     * @param device 模型信息
     * @param tStdRegionList 区域信息
     * @param dictMapList 字典表
     * @return TStdDevice对象
     */
    private TStdDevice createStdDevice(String edgeCode, Map<String, Object> device,
                                       List<TStdRegion> tStdRegionList, List<Map<String, String>> dictMapList) {
        TStdDevice tStdDevice =  new TStdDevice();
        tStdDevice.setEdgeCode(edgeCode);
        tStdDevice.setOriginId(String.valueOf(device.get("main_device_id")));
        tStdDevice.setDeviceName(String.valueOf(device.get("main_device_name")));
        tStdDevice.setUpRegionId(tStdRegionList.stream().filter(t ->
                device.get("bay_id").equals(String.valueOf(t.getOriginRegionId()))).map(TStdRegion::getRegionId)
                .collect(Collectors.toList()).get(0));
        tStdDevice.setUpRegionName(String.valueOf(device.get("bay_name")));
        tStdDevice.setDeviceType(CollectionUtils.isNotEmpty(dictMapList) ?
                Integer.parseInt(dictMapList.stream().filter(s -> "device_type".equals(s.get("col_name"))
                        && device.get("device_type").equals(String.valueOf(s.get("up_dict")))).map(d -> d.get("dict_code"))
                        .collect(Collectors.toList()).get(0)) : null);
        if (device.containsKey("real_code")) {
            tStdDevice.setRealCode(String.valueOf(device.get("real_code")));
        }
        return tStdDevice;
    }

    private TStdDeviceMete convertDeviceMete(Map<String, Object> device) {
        TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
        tStdDeviceMete.setDevicePointId((String) device.get("device_point_id"));
        tStdDeviceMete.setMeteKind((Integer) device.get("mete_kind"));
        tStdDeviceMete.setMeterType((Integer) device.get("meter_type"));
        tStdDeviceMete.setPositionType((String) device.get("position_type"));
        tStdDeviceMete.setAnalyseType((Integer) device.get("analyse_type"));
        tStdDeviceMete.setIsAi((String) device.get("is_ai"));
        tStdDeviceMete.setIsJudge((String) device.get("is_judge"));
        tStdDeviceMete.setUnit((String) device.get("unit"));
        tStdDeviceMete.setAlarmNote((String) device.get("alarm_note"));
        tStdDeviceMete.setAlarmType((String) device.get("alarm_type"));
        tStdDeviceMete.setUpEffect((Float) device.get("up_effect"));
        tStdDeviceMete.setDownEffect((Float) device.get("down_effect"));
        tStdDeviceMete.setAlarmLevel((Integer) device.get("alarm_level"));
        tStdDeviceMete.setHighLimit1((Float) device.get("high_limit1"));
        tStdDeviceMete.setHighLimit2((Float) device.get("high_limit2"));
        tStdDeviceMete.setHighLimit3((Float) device.get("high_limit3"));
        tStdDeviceMete.setHighLimit4((Float) device.get("high_limit4"));
        tStdDeviceMete.setLowLimit1((Float) device.get("low_limit1"));
        tStdDeviceMete.setLowLimit2((Float) device.get("low_limit2"));
        tStdDeviceMete.setLowLimit3((Float) device.get("low_limit3"));
        tStdDeviceMete.setLowLimit4((Float) device.get("low_limit4"));
        tStdDeviceMete.setAlarmDelay((Integer) device.get("alarm_delay"));
        tStdDeviceMete.setAlarmCnt((Integer) device.get("alarm_cnt"));
        tStdDeviceMete.setThresholdAbs((BigDecimal) device.get("threshold_abs"));
        tStdDeviceMete.setThresholdPer((BigDecimal) device.get("threshold_per"));
        tStdDeviceMete.setMeteType((String) device.get("mete_type"));
        tStdDeviceMete.setStateZero((String) device.get("state_zero"));
        tStdDeviceMete.setStateOne((String) device.get("state_one"));
        tStdDeviceMete.setAlarmState((Integer) device.get("alarm_state"));
        return tStdDeviceMete;
    }

    /**
     * 预置位
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TCameraPreset> dealCameraPreset(List<TCameraPreset> oldList, List<TCameraPreset> newList, String edgeCode) {
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
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tCameraPresetMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的预置位数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TCameraPreset> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            tCameraPresetMapper.batchInsert(insertList);
        }
        return insertList;
    }

    /**
     * 机器人测点
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TRobotInspection> dealRobotInspection(List<TRobotInspection> oldList, List<TRobotInspection> newList, String edgeCode) {
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
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tRobotInspectionDao.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的预置位数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TRobotInspection> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            tRobotInspectionDao.batchInsertTRobotInspection(insertList);
        }
        return insertList;
    }

    /**
     * 巡视点
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param edgeCode 节点编码
     */
    private void dealCruisePointInstance(List<TCameraPreset> insertCameraPresetList, List<TRobotInspection> insertRobotInspectionList,
                                         List<TStdDevice> insertStdDevice, List<TStdDeviceMete> insertStdDeviceMete,
                                         List<TCruisePointInstance> oldList, List<TCruisePointInstance> newList,
                                         List<TVoiceDevice> tVoiceDeviceList, String edgeCode) {
        Map<String, TCruisePointInstance> oldMap = oldList.stream().collect(Collectors.toMap(TCruisePointInstance::getOriginId, Function.identity()));
        Map<String, TCruisePointInstance> newMap = newList.stream().collect(Collectors.toMap(TCruisePointInstance::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TCruisePointInstance old = oldMap.get(t.getOriginId());
                t.setInstanceId(old.getInstanceId());
                t.setDeviceId(old.getDeviceId());
                t.setDeviceMeteId(old.getDeviceMeteId());
                t.setCruiseId(old.getCruiseId());
                tCruisePointInstanceMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tCruisePointInstanceMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            List<TCruisePointInstance> insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            insertList.forEach(tCruisePointInstance -> {
                tCruisePointInstance.setDeviceId(insertStdDevice.stream().filter(tStdDevice ->
                                String.valueOf(tCruisePointInstance.getDeviceId()).equals(tStdDevice.getOriginId()))
                        .collect(Collectors.toList()).get(0).getDeviceId());
                tCruisePointInstance.setDeviceMeteId(insertStdDeviceMete.stream().filter(tStdDeviceMete ->
                                String.valueOf(tCruisePointInstance.getDeviceMeteId()).equals(tStdDeviceMete.getOriginId()))
                        .collect(Collectors.toList()).get(0).getDeviceMeteId());
                switch (tCruisePointInstance.getCruiseType()) {
                    case 230:
                    case 229:
                        tCruisePointInstance.setCruiseId(insertCameraPresetList.stream().filter(tCameraPreset ->
                                        String.valueOf(tCruisePointInstance.getCruiseId()).equals(tCameraPreset.getOriginId()))
                                .collect(Collectors.toList()).get(0).getPresetId());
                        break;
                    case 228:
                    case 524:
                        tCruisePointInstance.setCruiseId(insertRobotInspectionList.stream().filter(tRobotInspection ->
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
            tCruisePointInstanceMapper.batchInsert(insertList);
        }
    }

    /**
     * 测点
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TStdDeviceMete> dealStdDeviceMete(List<TStdDevice> insertStdDevice, List<TStdDeviceMete> oldList, List<TStdDeviceMete> newList, String edgeCode) {
        Map<String, TStdDeviceMete> oldMap = oldList.stream().collect(Collectors.toMap(TStdDeviceMete::getOriginId, Function.identity()));
        Map<String, TStdDeviceMete> newMap = newList.stream().collect(Collectors.toMap(TStdDeviceMete::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TStdDeviceMete old = oldMap.get(t.getOriginId());
                t.setDeviceMeteId(old.getDeviceMeteId());
                t.setDeviceId(old.getDeviceId());
                tStdDevicemeteMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tStdDevicemeteMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TStdDeviceMete> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            insertList.forEach(tStdDeviceMete -> tStdDeviceMete.setDeviceId(insertStdDevice.stream().filter(tStdDevice ->
                            String.valueOf(tStdDeviceMete.getDeviceId()).equals(tStdDevice.getOriginId()))
                    .collect(Collectors.toList()).get(0).getDeviceId()));
            tStdDevicemeteMapper.batchInsert(insertList);
        }
        return insertList;
    }

    /**
     * 测点
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param edgeCode 节点编码
     */
    private void dealAlgorithmMete(List<TStdDeviceMete> insertStdDeviceMete, List<TAlgorithmMete> oldList, List<TAlgorithmMete> newList, String edgeCode) {
        Map<String, TAlgorithmMete> oldMap = oldList.stream().collect(Collectors.toMap(TAlgorithmMete::getOriginId, Function.identity()));
        Map<String, TAlgorithmMete> newMap = newList.stream().collect(Collectors.toMap(TAlgorithmMete::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TAlgorithmMete old = oldMap.get(t.getOriginId());
                t.setDeviceMeteId(old.getDeviceMeteId());
                tAlgorithmMeteMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tAlgorithmMeteMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            List<TAlgorithmMete> insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            insertList.forEach(tAlgorithmMete ->
                    tAlgorithmMete.setDeviceMeteId(insertStdDeviceMete.stream().filter(tStdDeviceMete ->
                                    String.valueOf(tAlgorithmMete.getDeviceMeteId()).equals(tStdDeviceMete.getOriginId()))
                            .collect(Collectors.toList()).get(0).getDeviceMeteId()));
            tAlgorithmMeteMapper.batchInsert(insertList);
        }
    }

    /**
     * 设备
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TStdDevice> dealStdDevice(List<TStdDevice> oldList, List<TStdDevice> newList, String edgeCode) {
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
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tStdDeviceMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TStdDevice> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            tStdDeviceMapper.batchInsert(insertList);
        }
        return insertList;
    }
}
