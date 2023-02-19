package com.yjh.accessrobot.module.command.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yjh.accessrobot.common.utils.ValueUtil.*;
import static com.yjh.accessrobot.common.utils.ValueUtil.objToInt;

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
    private TStdDeviceAttrMapper tStdDeviceAttrMapper;
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
            List<Map<String, String>> dictMapList = tStdDeviceMapper.selectDictCodeByUpDict("device_type,meter_type");
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
            //old 设备信息扩展表
            List<TStdDeviceAttr> oldStdDeviceAttrList = tStdDeviceAttrMapper.selectByEdgeCode(edgeCode);
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
            List<TStdDeviceAttr> finalDeviceAttrList = new ArrayList<>();
            List<TCameraPreset> finalCameraPresetList = new ArrayList<>();
            List<TRobotInspection> finalRobotInspectionList = new ArrayList<>();
            List<TStdDeviceMete> finalStdDeviceMeteList = new ArrayList<>();
            List<TAlgorithmMete> finalAlgorithmMeteList = new ArrayList<>();
            List<TCruisePointInstance> newCruisePointInstances = new ArrayList<>();
            deviceModelList.stream().filter(d -> !JSON.parseObject(JSONArray.parseArray(d.get("video_pos").toString()).get(0).toString()).entrySet().isEmpty())
                    .forEach(device -> {
                        //构建 t_std_device
                        TStdDevice tStdDevice = createStdDevice(edgeCode, device, tStdRegionList, dictMapList);
                        TStdDeviceAttr tStdDeviceAttr = createStdDeviceAttr(tStdDevice, device);
                        finalDeviceList.add(tStdDevice);
                        finalDeviceAttrList.add(tStdDeviceAttr);
                        long cruiseId;
                        int cruiseType;
                        switch (String.valueOf(device.get("data_type"))) {
                            case "1":
                                //视频
                                //构建 t_camera_preset
                                JSONArray cameraArray = JSONArray.parseArray(device.get("video_pos").toString());
                                String cameraOriginId = JSON.parseObject(cameraArray.get(0).toString()).get("device_code").toString();
                                TCameraInfo tCameraInfo = tCameraInfoList.stream().filter(t -> cameraOriginId.equals(t.getOriginId()) && edgeCode.equals(t.getEdgeCode())).collect(Collectors.toList()).get(0);
                                TCameraPreset tCameraPreset = createCameraPreset(edgeCode, device, cameraArray, tCameraInfo.getCameraId());
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
                        TStdDeviceMete tStdDeviceMete = createStdDeviceMete(edgeCode, device, tStdDevice, dictMapList);
                        if (device.containsKey("algorithm_id") && StringUtils.isNotEmpty(device.get("algorithm_id").toString())) {
                            TAlgorithmMete tAlgorithmMete = new TAlgorithmMete();
                            tAlgorithmMete.setEdgeCode(edgeCode);
                            tAlgorithmMete.setOriginId(tStdDeviceMete.getOriginId());
                            tAlgorithmMete.setAlgorithmId(objToLong(device.get("algorithm_id")));
                            finalAlgorithmMeteList.add(tAlgorithmMete);
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
            List<TStdDeviceAttr> newStdDeviceAttrs = finalDeviceAttrList.stream().distinct().collect(Collectors.toList());
            List<TStdDeviceMete> newStdDeviceMetes = finalStdDeviceMeteList.stream().distinct().collect(Collectors.toList());
            List<TAlgorithmMete> newAlgorithmMetes = finalAlgorithmMeteList.stream().distinct().collect(Collectors.toList());

            //将旧的数据和新的数据进行对比相同的去掉  不同的更新  新增的直接入库
            //预置位
            List<TCameraPreset> insertCameraPresetList = dealCameraPreset(oldCameraPresetList, newCameraPresets, edgeCode, presetPath, presetRealPath, edgeLevel, ftpsPath);
            insertCameraPresetList.addAll(oldCameraPresetList);
            //机器人测点
            List<TRobotInspection> insertRobotInspectionList = dealRobotInspection(oldRobotInspectionList, newRobotInspections, edgeCode);
            insertRobotInspectionList.addAll(oldRobotInspectionList);
            //设备点位
            List<TStdDevice> insertStdDevice = dealStdDevice(oldStdDeviceList, newStdDevices, insertCameraPresetList, tCameraInfoList, edgeCode);
            insertStdDevice.addAll(oldStdDeviceList);
            dealStdDeviceAttr(insertStdDevice, oldStdDeviceAttrList, newStdDeviceAttrs, edgeCode);
            //标准测点
            List<TStdDeviceMete> insertStdDeviceMete = dealStdDeviceMete(insertStdDevice, oldStdDeviceMeteList, newStdDeviceMetes, edgeCode, edgeLevel);
            //标准测点算法关系信息
            insertStdDeviceMete.addAll(oldStdDeviceMeteList);
            dealAlgorithmMete(insertStdDeviceMete, oldAlgorithmMeteList, newAlgorithmMetes, edgeCode);
            //巡视点
            dealCruisePointInstance(insertCameraPresetList, insertRobotInspectionList, insertStdDevice, insertStdDeviceMete,
                    oldCruisePointInstanceList, newCruisePointInstances, tVoiceDeviceList, edgeCode);
        } else {
            //模型为空 删除该节点的所有信息
            tCameraPresetMapper.deleteByEdgeCode(edgeCode);
            tRobotInspectionDao.deleteByEdgeCode(edgeCode);
            tCruisePointInstanceMapper.deleteByEdgeCode(edgeCode);
            tAlgorithmMeteMapper.deleteByEdgeCode(edgeCode);
            tStdDevicemeteMapper.deleteByEdgeCode(edgeCode);
            tStdDeviceAttrMapper.deleteByEdgeCode(edgeCode);
            tStdDeviceMapper.deleteByEdgeCode(edgeCode);
        }
    }

    /**
     * 单独处理预置位图片
     * @param tCameraPreset 入库后的预置位数据
     * @param ftpsPath
     * @param presetPath
     * @param presetRealPath
     */
    private void dealCameraPresetImage(TCameraPreset tCameraPreset, String ftpsPath,
                                       String presetPath, String presetRealPath) {
        //当节点为巡视主机接入边缘节点时再触发
        if (StringUtils.isNotEmpty(tCameraPreset.getPresetImg())) {
            String imgPath = tCameraPreset.getPresetImg().replace(presetRealPath, "")
                    .replace(String.valueOf(tCameraPreset.getPresetId()), tCameraPreset.getOriginId());
            String presetFtpsImg = ftpsPath + imgPath;
            String newPresetImagePath = imgPath.replace(tCameraPreset.getOriginId(), tCameraPreset.getPresetId().toString());
            String presetImg = presetPath + newPresetImagePath;
            File ftpsFile = new File(presetFtpsImg);
            if (ftpsFile.exists()) {
                try {
                    FileUtil.copyFileUsingStream(presetFtpsImg, presetImg);
                    tCameraPreset.setPresetImg(presetRealPath + newPresetImagePath);
                } catch (IOException e) {
                    log.error("预置位文件拷贝失败", e);
                }
                tCameraPresetMapper.updateByPrimaryKeySelective(tCameraPreset);
            }
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
    private TStdDeviceMete createStdDeviceMete(String edgeCode, Map<String, Object> device, TStdDevice tStdDevice, List<Map<String, String>> dictMapList) {
        TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
        tStdDeviceMete.setMeteName(String.valueOf(device.get("device_name")).contains("/")
                ? StringUtils.substringBefore(device.get("device_name").toString().replace(" ", ""), "/")
                : String.valueOf(device.get("device_name")));
        tStdDeviceMete.setDeviceId(Long.valueOf(tStdDevice.getOriginId()));
        tStdDeviceMete.setCustomId(String.valueOf(device.get("component_id")));
        tStdDeviceMete.setCustomName(String.valueOf(device.get("component_name")));
        if (device.containsKey("device_mete_id")) {
            tStdDeviceMete.setOriginId(String.valueOf(device.get("device_mete_id")));
            tStdDeviceMete.setDevicePointId((String) device.get("device_point_id"));
            tStdDeviceMete.setMeteKind(objToInt(device.get("mete_kind")));
            tStdDeviceMete.setMeterType(CollectionUtils.isNotEmpty(dictMapList) && StringUtils.isNotEmpty(String.valueOf(device.get("meter_type"))) ?
                    Integer.parseInt(dictMapList.stream().filter(s -> "meter_type".equals(s.get("col_name"))
                                    && device.get("meter_type").equals(String.valueOf(s.get("up_dict")))).map(d -> d.get("dict_code"))
                            .collect(Collectors.toList()).get(0)) : null);
            tStdDeviceMete.setPositionType((String) device.get("position_type"));
            tStdDeviceMete.setAnalyseType(objToInt(device.get("analyse_type")));
            tStdDeviceMete.setIsAi((String) device.get("is_ai"));
            tStdDeviceMete.setIsJudge((String) device.get("is_judge"));
            tStdDeviceMete.setUnit((String) device.get("unit"));
            tStdDeviceMete.setAlarmNote((String) device.get("alarm_note"));
            tStdDeviceMete.setAlarmType((String) device.get("alarm_type"));
            tStdDeviceMete.setUpEffect(objToFloat(device.get("up_effect")));
            tStdDeviceMete.setDownEffect(objToFloat(device.get("down_effect")));
            tStdDeviceMete.setAlarmLevel(objToInt(device.get("alarm_level")));
            tStdDeviceMete.setHighLimit1(objToFloat(device.get("high_limit1")));
            tStdDeviceMete.setHighLimit2(objToFloat(device.get("high_limit2")));
            tStdDeviceMete.setHighLimit3(objToFloat(device.get("high_limit3")));
            tStdDeviceMete.setHighLimit4(objToFloat(device.get("high_limit4")));
            tStdDeviceMete.setLowLimit1(objToFloat(device.get("low_limit1")));
            tStdDeviceMete.setLowLimit2(objToFloat(device.get("low_limit2")));
            tStdDeviceMete.setLowLimit3(objToFloat(device.get("low_limit3")));
            tStdDeviceMete.setLowLimit4(objToFloat(device.get("low_limit4")));
            tStdDeviceMete.setAlarmDelay(objToInt(device.get("alarm_delay")));
            tStdDeviceMete.setAlarmCnt(objToInt(device.get("alarm_cnt")));
            tStdDeviceMete.setThresholdAbs(objToBigDecimal(device.get("threshold_abs")));
            tStdDeviceMete.setThresholdPer(objToBigDecimal(device.get("threshold_per")));
            tStdDeviceMete.setMeteType((String) device.get("mete_type"));
            tStdDeviceMete.setStateZero((String) device.get("state_zero"));
            tStdDeviceMete.setStateOne((String) device.get("state_one"));
            tStdDeviceMete.setAlarmState(objToInt(device.get("alarm_state")));
            tStdDeviceMete.setIsTemdif(objToInt(device.get("is_temdif")));
        } else {
            tStdDeviceMete.setOriginId(String.valueOf(device.get("device_id")));
        }
        tStdDeviceMete.setEdgeCode(edgeCode);
        tStdDeviceMete.setInspectionType(objToInt(device.getOrDefault("inspection_type", "1")));
        tStdDeviceMete.setRedundantType(String.valueOf(device.getOrDefault("redundant_type", "1")));
        return tStdDeviceMete;
    }


    /**
     * 创建 TStdDeviceAttr 表信息
     * @param tStdDevice
     * @param device
     * @return
     */
    private TStdDeviceAttr createStdDeviceAttr(TStdDevice tStdDevice, Map<String, Object> device) {
        TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
        tStdDeviceAttr.setEdgeCode(tStdDevice.getEdgeCode());
        tStdDeviceAttr.setOriginId(tStdDevice.getOriginId());
        tStdDeviceAttr.setDeviceModel(objToInt(device.get("device_model")));
        tStdDeviceAttr.setPmsType(String.valueOf(device.get("pms_type")));
        tStdDeviceAttr.setPmsId(String.valueOf(device.get("pms_id")));
        tStdDeviceAttr.setDeviceVendor(String.valueOf(device.get("device_vendor")));
        tStdDeviceAttr.setUsedTime(DateTimeUtil.getDate(String.valueOf(device.get("used_time"))));
        tStdDeviceAttr.setMaintenanceCount(String.valueOf(device.get("maintenance_count")));
        tStdDeviceAttr.setResponsiblePerson(String.valueOf(device.get("responsible_person")));
        tStdDeviceAttr.setLatitude(String.valueOf(device.get("latitude")));
        tStdDeviceAttr.setLongitude(String.valueOf(device.get("longitude")));
        tStdDeviceAttr.setIp(String.valueOf(device.get("ip")));
        tStdDeviceAttr.setPort(objToInt(device.get("port")));
        tStdDeviceAttr.setVoltageLevel(String.valueOf(device.get("voltage_level")));
        tStdDeviceAttr.setSequencePoint(String.valueOf(device.get("sequence_point")));
        tStdDeviceAttr.setAddress(String.valueOf(device.get("address")));
        return tStdDeviceAttr;
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
        tRobotInspection.setInspectionType(objToInt(device.getOrDefault("inspection_type", 1)));
        return tRobotInspection;
    }

    /**
     * 创建 TCameraPreset 表信息
     * @param edgeCode
     * @param device
     * @param cameraArray
     * @param cameraId
     * @return
     */
    private TCameraPreset createCameraPreset(String edgeCode, Map<String, Object> device, JSONArray cameraArray,
                                             Long cameraId) {
        TCameraPreset tCameraPreset = new TCameraPreset();
        tCameraPreset.setEdgeCode(edgeCode);
        long cruiseId = Long.parseLong(JSON.parseObject(cameraArray.get(0).toString()).get("device_pos").toString());
        tCameraPreset.setOriginId(String.valueOf(cruiseId));
        tCameraPreset.setCameraId(cameraId);
        tCameraPreset.setPresetName(String.valueOf(device.get("device_name")).contains("/")
                ? StringUtils.substringAfter(device.get("device_name").toString(), "/")
                : String.valueOf(device.get("device_name")));
        tCameraPreset.setPresetNum(objToInt(device.getOrDefault("preset_num", 1)));
        tCameraPreset.setPresetType(objToInt(device.getOrDefault("preset_type", 1)));
//        tCameraPreset.setIsKeepWatch(objToInt(device.getOrDefault("is_keep_watch", 0)));
//        tCameraPreset.setIsKeepWatchTask(objToInt(device.getOrDefault("is_keep_watch_task", 0)));
//        tCameraPreset.setIsSecondKeepWatchTask(objToInt(device.getOrDefault("is_second_keep_watch_task", 0)));
        tCameraPreset.setPresetImg(device.get("preset_img").toString());
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
            tStdDevice.setCameraId(objToLong(device.get("device_camera_id")));
            tStdDevice.setPresetId(objToLong(device.get("device_preset_id")));
            tStdDevice.setAliasName(String.valueOf(device.get("alias_name")));
            tStdDevice.setModelId(objToLong(device.get("model_id")));
        }
        return tStdDevice;
    }

    /**
     * 预置位
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TCameraPreset> dealCameraPreset(List<TCameraPreset> oldList, List<TCameraPreset> newList,
                                                 String edgeCode, String presetPath, String presetRealPath,
                                                 String edgeLevel, String ftpsPath) {
        Map<String, TCameraPreset> oldMap = oldList.stream().collect(Collectors.toMap(TCameraPreset::getOriginId, Function.identity()));
        Map<String, TCameraPreset> newMap = newList.stream().collect(Collectors.toMap(TCameraPreset::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TCameraPreset old = oldMap.get(t.getOriginId());
                t.setPresetId(old.getPresetId());
                tCameraPresetMapper.updateByPrimaryKey(t);
                if (EdgeEnum.REGION_NODE.getCode().equals(edgeLevel)) {
                    dealCameraPresetImage(old, ftpsPath, presetPath, presetRealPath);
                }
            });

        }
        //删除的预置位数据
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            deleteIdSet.forEach(deleteId -> {
                String presetImg = oldMap.get(deleteId).getPresetImg();
                FileUtil.deleteDirectory(StringUtils.substringBeforeLast(presetImg.replace(presetRealPath, presetPath), "/"));
            });
            tCameraPresetMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的预置位数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        List<TCameraPreset> insertList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            List<List<TCameraPreset>> partitionList = Lists.partition(insertList, 1000);
            partitionList.forEach(list -> tCameraPresetMapper.batchInsert(list));
            //处理预置位图片
            if (EdgeEnum.REGION_NODE.getCode().equals(edgeLevel)) {
                insertList.forEach(tCameraPreset -> {
                    dealCameraPresetImage(tCameraPreset, ftpsPath, presetPath, presetRealPath);
                });
            }
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
            List<List<TRobotInspection>> partitionList = Lists.partition(insertList, 1000);
            partitionList.forEach(list -> tRobotInspectionDao.batchInsertTRobotInspection(list));
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
            List<List<TCruisePointInstance>> partitionList = Lists.partition(insertList, 1000);
            partitionList.forEach(list -> tCruisePointInstanceMapper.batchInsert(list));
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
    private List<TStdDeviceMete> dealStdDeviceMete(List<TStdDevice> insertStdDevice, List<TStdDeviceMete> oldList, List<TStdDeviceMete> newList, String edgeCode, String edgeLevel) {
        Map<String, TStdDeviceMete> oldMap = oldList.stream().collect(Collectors.toMap(TStdDeviceMete::getOriginId, Function.identity()));
        Map<String, TStdDeviceMete> newMap = newList.stream().collect(Collectors.toMap(TStdDeviceMete::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TStdDeviceMete old = oldMap.get(t.getOriginId());
                t.setDeviceMeteId(old.getDeviceMeteId());
                t.setDeviceId(old.getDeviceId());
                //告警由巡视主机去配置,同步之后不能覆盖
                if (EdgeEnum.REGION_NODE.getCode().equals(edgeLevel)){
                    t.setMeteKind(old.getMeteKind());
                    t.setUnit(old.getUnit());
                    t.setAlarmNote(old.getAlarmNote());
                    t.setAlarmType(old.getAlarmType());
                    t.setUpEffect(old.getUpEffect());
                    t.setDownEffect(old.getDownEffect());
                    t.setStateZero(old.getStateZero());
                    t.setStateOne(old.getStateOne());
                    t.setAlarmState(old.getAlarmState());
                    t.setAlarmLevel(old.getAlarmLevel());
                    t.setHighLimit1(old.getHighLimit1());
                    t.setHighLimit2(old.getHighLimit2());
                    t.setHighLimit3(old.getHighLimit3());
                    t.setHighLimit4(old.getHighLimit4());
                    t.setLowLimit1(old.getLowLimit1());
                    t.setLowLimit2(old.getLowLimit2());
                    t.setLowLimit3(old.getLowLimit3());
                    t.setLowLimit4(old.getLowLimit4());
                    t.setAlarmDelay(old.getAlarmDelay());
                    t.setAlarmCnt(old.getAlarmCnt());
                    t.setThresholdAbs(old.getThresholdAbs());
                    t.setThresholdPer(old.getThresholdPer());
                }
                tStdDevicemeteMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tAlgorithmMeteMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
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
            List<List<TStdDeviceMete>> partitionList = Lists.partition(insertList, 1000);
            partitionList.forEach(list -> tStdDevicemeteMapper.batchInsert(list));
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
//        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
//        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
//            tAlgorithmMeteMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
//        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            List<TAlgorithmMete> insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            insertList.forEach(tAlgorithmMete ->
                    tAlgorithmMete.setDeviceMeteId(insertStdDeviceMete.stream().filter(tStdDeviceMete ->
                                    tAlgorithmMete.getOriginId().equals(tStdDeviceMete.getOriginId()))
                            .collect(Collectors.toList()).get(0).getDeviceMeteId()));
            List<List<TAlgorithmMete>> partitionList = Lists.partition(insertList, 1000);
            partitionList.forEach(list -> tAlgorithmMeteMapper.batchInsert(list));
        }
    }


    /**
     * 设备扩展表
     * @param oldList
     * @param newList
     * @param edgeCode
     */
    private void dealStdDeviceAttr(List<TStdDevice> insertStdDevice, List<TStdDeviceAttr> oldList, List<TStdDeviceAttr> newList, String edgeCode) {
        Map<String, TStdDeviceAttr> oldMap = oldList.stream().collect(Collectors.toMap(TStdDeviceAttr::getOriginId, Function.identity()));
        Map<String, TStdDeviceAttr> newMap = newList.stream().collect(Collectors.toMap(TStdDeviceAttr::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(updateIdSet)) {
            newList.stream().filter(t ->
                    updateIdSet.contains(t.getOriginId())).forEach(t -> {
                TStdDeviceAttr old = oldMap.get(t.getOriginId());
                t.setDeviceId(old.getDeviceId());
                tStdDeviceAttrMapper.updateByPrimaryKey(t);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet = SetUtils.difference(oldMap.keySet(), newMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tStdDeviceAttrMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newMap.keySet(), oldMap.keySet());
        if (CollectionUtils.isNotEmpty(insertIdSet)) {
            List<TStdDeviceAttr> insertList = newList.stream().filter(t -> insertIdSet.contains(t.getOriginId())).collect(Collectors.toList());
            insertList.forEach(tStdDeviceAttr -> tStdDeviceAttr.setDeviceId(insertStdDevice.stream().filter(tStdDevice ->
                            tStdDeviceAttr.getOriginId().equals(tStdDevice.getOriginId()))
                    .collect(Collectors.toList()).get(0).getDeviceId()));
            List<List<TStdDeviceAttr>> partitionList = Lists.partition(insertList, 1000);
            partitionList.forEach(list -> tStdDeviceAttrMapper.batchInsert(list));
        }
    }

    /**
     * 设备
     *
     * @param oldList  旧数据
     * @param newList  新数据
     * @param insertCameraPresetList
     * @param tCameraInfoList
     * @param edgeCode 节点编码
     * @return 直接入库的数据
     */
    private List<TStdDevice> dealStdDevice(List<TStdDevice> oldList, List<TStdDevice> newList,
                                           List<TCameraPreset> insertCameraPresetList, List<TCameraInfo> tCameraInfoList, String edgeCode) {
        Map<String, TStdDevice> oldMap = oldList.stream().collect(Collectors.toMap(TStdDevice::getOriginId, Function.identity()));
        Map<String, TStdDevice> newMap = newList.stream().collect(Collectors.toMap(TStdDevice::getOriginId, Function.identity()));
        SetUtils.SetView<String> updateIdSet = SetUtils.intersection(oldMap.keySet(), newMap.keySet());
        newList.stream().filter(tStdDevice -> Objects.nonNull(tStdDevice.getCameraId()) && Objects.nonNull(tStdDevice.getPresetId()))
                .forEach(tStdDevice -> {
                    List<TCameraInfo> cameraList = tCameraInfoList.stream().filter(tCameraInfo -> tCameraInfo.getOriginId().equals(String.valueOf(tStdDevice.getCameraId()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(cameraList)) {
                        tStdDevice.setCameraId(cameraList.get(0).getCameraId());
                    }
                    List<TCameraPreset> presetList = insertCameraPresetList.stream().filter(tCameraPreset -> tCameraPreset.getOriginId().equals(String.valueOf(tStdDevice.getPresetId()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(presetList)) {
                        tStdDevice.setPresetId(presetList.get(0).getPresetId());
                    }
                });
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
            List<List<TStdDevice>> partitionList = Lists.partition(insertList, 1000);
            partitionList.forEach(list -> tStdDeviceMapper.batchInsert(list));
        }
        return insertList;
    }
}
