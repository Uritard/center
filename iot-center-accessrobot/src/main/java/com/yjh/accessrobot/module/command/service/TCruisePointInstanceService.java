package com.yjh.accessrobot.module.command.service;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.enumeration.*;
import com.yjh.accessrobot.module.command.dao.LinkAutoMapper;
import com.yjh.accessrobot.module.command.dao.TRobotInspectionDao;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/4/3
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TCruisePointInstanceService {

    @Autowired
    LinkAutoMapper linkAutoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;

    /**
     * 新建测点并连接巡视点
     *
     * @param tRobotInspectionsInfoModelFile 模型巡视点列表
     * @param robotId                        机器人id
     */
    public void instanceLinkAuto(List<TRobotInspection> tRobotInspectionsInfoModelFile, Long robotId) {
        //取出当前根节点
        Long rootId = linkAutoMapper.selectRoot();
        //取出所有未连接测点
        List<TRobotInspection> tRobotInspections = linkAutoMapper.selectInspectionNonLink(robotId);
        Map<String, Long> inspectionMap =
            tRobotInspections.stream().collect(Collectors.toMap(TRobotInspection::getInspectionCode, TRobotInspection::getInspectionId));

        List<String> inspectionCodes = new ArrayList<>(inspectionMap.keySet());
        List<TRobotInspection> buildList =
            tRobotInspectionsInfoModelFile.stream().filter(o -> inspectionCodes.contains(o.getInspectionCode()))
                .map(o -> o.setInspectionId(inspectionMap.get(o.getInspectionCode()))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(buildList)) {
            return;
        }

        //取出所有区域
        Set<String> regionNameSet = buildList.stream().map(TRobotInspection::getAreaName).collect(Collectors.toSet());
        //查询已存在区域
        List<TStdRegion> tStdRegionList = linkAutoMapper.listTStdRegionByParentIdAndNameList(rootId, regionNameSet);
        //处理不存在区域
        if (CollectionUtils.isNotEmpty(tStdRegionList)) {
            tStdRegionList.forEach(o -> regionNameSet.remove(o.getRegionName()));
        }

        if (CollectionUtils.isNotEmpty(regionNameSet)) {
            List<TStdRegion> insertList = new ArrayList<>(regionNameSet.size());
            regionNameSet.forEach(o -> {
                TStdRegion tStdRegion = new TStdRegion();
                tStdRegion.setUpRegionId(rootId);
                tStdRegion.setRegionName(o);
                tStdRegion.setState(1);
                insertList.add(tStdRegion);
            });
            linkAutoMapper.batchInsertRegion(insertList);
            //将新增区域放入list
            tStdRegionList.addAll(insertList);
        }

        //替换区域Id
        Map<String, TStdRegion> areaNameToId =
            tStdRegionList.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity()));
        buildList.forEach(o -> o.setAreaId(String.valueOf(areaNameToId.get(o.getAreaName()).getRegionId())));
        Multimap<Long, TRobotInspection> areaToBayMap = HashMultimap.create();
        buildList.forEach(o -> areaToBayMap.put(Long.valueOf(o.getAreaId()), o));

        Set<Long> areaIdSet = areaToBayMap.keySet();
        List<TRobotInspection> buildListAfterRegion = new LinkedList<>();
        List<TStdRegion> totalRegion = new ArrayList<>();
        for (Long areaId : areaIdSet) {
            List<TRobotInspection> sameAreaList = new ArrayList<>(areaToBayMap.get(areaId));
            Set<String> bayNameSet = sameAreaList.stream().map(TRobotInspection::getBayName).collect(Collectors.toSet());
            List<TStdRegion> existsBayList = linkAutoMapper.selectByRegionNameEqual(areaId, bayNameSet);

            existsBayList.forEach(tStdRegion -> bayNameSet.remove(tStdRegion.getRegionName()));
            List<TStdRegion> insertList = new ArrayList<>(bayNameSet.size());
            if (CollectionUtils.isNotEmpty(bayNameSet)) {
                bayNameSet.forEach(o -> {
                    TStdRegion tStdRegion = new TStdRegion();
                    tStdRegion.setUpRegionId(areaId);
                    tStdRegion.setRegionName(o);
                    tStdRegion.setState(1);
                    insertList.add(tStdRegion);
                });
                linkAutoMapper.batchInsertRegion(insertList);
                existsBayList.addAll(insertList);
            }

            Map<String, TStdRegion> bayNameToBayToId =
                existsBayList.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity()));

            sameAreaList.forEach(tRobotInspection -> {
                tRobotInspection.setBayId(String.valueOf(bayNameToBayToId.get(tRobotInspection.getBayName()).getRegionId()));
            });

            buildListAfterRegion.addAll(sameAreaList);
            totalRegion.addAll(existsBayList);
        }
        Map<Long, TStdRegion> idToName = totalRegion.stream().collect(Collectors.toMap(TStdRegion::getRegionId, Function.identity()));

        Multimap<Long, TRobotInspection> multimap = HashMultimap.create();
        buildListAfterRegion.forEach(o -> multimap.put(Long.valueOf(o.getBayId()), o));

        Set<Long> set = multimap.keySet();

        List<TStdDevice> totalStdDevices = new ArrayList<>();
        List<TStdDevice> insertDevices = new ArrayList<>();

        List<TRobotInspection> buildListAfterDevice = new LinkedList<>();
        for (Long regionId : set) {
            List<TRobotInspection> tRobotInspections1 = new ArrayList<>(multimap.get(regionId));
            Set<String> deviceNames = new HashSet<>();
            Map<String, String> map = tRobotInspections1.stream().filter(o -> deviceNames.add(o.getMainDeviceName()))
                .collect(Collectors.toMap(TRobotInspection::getMainDeviceName, TRobotInspection::getDeviceType));
            deviceNames.clear();
            Map<String, String> nameToMainId = tRobotInspections1.stream().filter(o -> deviceNames.add(o.getMainDeviceName()))
                .collect(Collectors.toMap(TRobotInspection::getMainDeviceName, TRobotInspection::getMainDeviceId));
            List<TStdDevice> tStdDevices = linkAutoMapper.selectByDeviceNameEqual(regionId, deviceNames);
            if (CollectionUtils.isNotEmpty(tStdDevices)) {
                tStdDevices.forEach(o -> o.setMainDeviceId(nameToMainId.get(o.getDeviceName())));
                tStdDevices.forEach(tStdDevice -> deviceNames.remove(tStdDevice.getDeviceName()));
                totalStdDevices.addAll(tStdDevices);
            }
            if (CollectionUtils.isNotEmpty(deviceNames)) {
                deviceNames.forEach(deviceName -> {
                    Integer deviceType = DeviceTypeEnum.getDictCodeByUpDict(Integer.parseInt(map.get(deviceName)));
                    TStdDevice tStdDevice = new TStdDevice();
                    tStdDevice.setUpRegionId(regionId);
                    tStdDevice.setMainDeviceId(nameToMainId.get(deviceName));
                    tStdDevice.setDeviceName(deviceName);
                    tStdDevice.setDeviceType(deviceType);
                    tStdDevice.setUpRegionName(idToName.get(regionId).getRegionName());
                    insertDevices.add(tStdDevice);
                    TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
                    tStdDeviceAttr.setUsedTime(new Date());
                    tStdDevice.setTStdDeviceAttr(tStdDeviceAttr);

                });
            }
            tRobotInspections1.forEach(o -> {
                o.setMainDeviceId(nameToMainId.get(o.getMainDeviceName()));
            });
            buildListAfterDevice.addAll(tRobotInspections1);
        }
        if (CollectionUtils.isNotEmpty(insertDevices)) {
            linkAutoMapper.batchInsertDevice(insertDevices);
            List<TStdDeviceAttr> tStdDeviceAttrs = insertDevices.stream().map(o -> {
                o.getTStdDeviceAttr().setDeviceId(o.getDeviceId());
                return o.getTStdDeviceAttr();
            }).collect(Collectors.toList());
            linkAutoMapper.batchInsertDeviceAttr(tStdDeviceAttrs);
            totalStdDevices.addAll(insertDevices);

        }

        Set<String> strings = new HashSet<>();

        Map<String, Long> stringLongMap = totalStdDevices.stream().filter(o -> strings.add(o.getMainDeviceId()))
            .collect(Collectors.toMap(TStdDevice::getMainDeviceId, TStdDevice::getDeviceId));

        buildListAfterDevice.forEach(o-> o.setDeviceId(stringLongMap.get(o.getMainDeviceId())));

        Multimap<Long, TRobotInspection> multiMapForDevice = HashMultimap.create();
        buildListAfterDevice.forEach(o -> multiMapForDevice.put(o.getDeviceId(), o));
        List<TStdDeviceMete> totalMete = new ArrayList<>();
        multiMapForDevice.keySet().forEach(deviceId -> {
            List<TRobotInspection> tRobotInspectionList = new ArrayList<>(multiMapForDevice.get(deviceId));
            Set<String> meteNames = tRobotInspectionList.stream().map(TRobotInspection::getDeviceName).collect(Collectors.toSet());
            List<TStdDeviceMete> tStdDeviceMetes = linkAutoMapper.selectByMeteNameEqual(deviceId,meteNames);
                Map<String, TStdDeviceMete> tStdDeviceMeteMap = tStdDeviceMetes.stream().collect(Collectors.toMap(TStdDeviceMete::getMeteName,Function.identity()));

            tRobotInspectionList.forEach(tRobotInspection -> {
                if (tStdDeviceMeteMap.containsKey(tRobotInspection.getDeviceName())) {
                    tStdDeviceMeteMap.get(tRobotInspection.getDeviceName()).getTRobotInspections().add(tRobotInspection);
                }
                else {
                    TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
                    Integer deviceType = DeviceTypeEnum.getDictCodeByUpDict(Integer.parseInt(tRobotInspection.getDeviceType()));
                    tStdDeviceMete.setDeviceId(tRobotInspection.getDeviceId());
                    tStdDeviceMete.setDevicePointId(String.valueOf(tRobotInspection.getInspectionId()));
                    tStdDeviceMete.setDeviceType(deviceType);
                    tStdDeviceMete.setCustomId(CustomTypeEnum.CUSTOM_TYPE_700.getDictCode());
                    tStdDeviceMete.setInspectionType(1);
                    tStdDeviceMete.setCustomName(CustomTypeEnum.CUSTOM_TYPE_700.getDictNote());
                    tStdDeviceMete.setMeterType(MeterTypeEnum.getDictCodeByUpDict(tRobotInspection.getMeterType()));
                    if (StringUtils.isNotBlank(tRobotInspection.getRecognitionTypeList())) {
                        String[] meteTypes = tRobotInspection.getRecognitionTypeList().split(",");
                        if (meteTypes.length != 0) {
                            Integer meteType = Integer.parseInt(meteTypes[0]);
                            String meteTypeSys = MeteTypeEnum.getEnumByUpDict(meteType) == null ? null :
                                String.valueOf(MeteTypeEnum.getEnumByUpDict(meteType).getDictCode());
                            tStdDeviceMete.setMeteType(meteTypeSys);
                        }
                    }
                    tStdDeviceMete.setMeteName(tRobotInspection.getDeviceName());
                    //默认字段
                    tStdDeviceMete.setPositionType("inside");
                    tStdDeviceMete.setIsAi("off");
                    tStdDeviceMete.setIsJudge("off");
                    tStdDeviceMete.setAlarmNote("0");
                    tStdDeviceMete.setAlarmState(0);
                    tStdDeviceMete.setRedundantType("1");
                    tStdDeviceMete.setIsTemdif(0);
                    tStdDeviceMete.getTRobotInspections().add(tRobotInspection);
                    tStdDeviceMeteMap.put(tRobotInspection.getDeviceName(),tStdDeviceMete);
                }
            });
            totalMete.addAll(tStdDeviceMeteMap.values());
        });

        List<TStdDeviceMete> insert = totalMete.stream().filter(o -> Objects.isNull(o.getDeviceMeteId())).collect(Collectors.toList());
        List<TStdDeviceMete> link = totalMete.stream().filter(o -> Objects.nonNull(o.getDeviceMeteId())).collect(Collectors.toList());


        if (CollectionUtils.isNotEmpty(insert)) {
            linkAutoMapper.insertDeviceMete(insert);
            List<TCruisePointInstance> tCruisePointInstanceList = new ArrayList<>(insert.size());
            TStdRegion tStdRegion = linkAutoMapper.selectTSRegionForStation();
            insert.forEach(o -> {
                List<TRobotInspection> tRobotInspections1 = o.getTRobotInspections();
                if (CollectionUtils.isNotEmpty(tRobotInspections1)) {
                    tRobotInspections1.forEach(tRobotInspection -> {
                        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
                        tCruisePointInstance.setDeviceMeteId(o.getDeviceMeteId());
                        tCruisePointInstance.setDeviceId(o.getDeviceId());
                        tCruisePointInstance.setCustomId(o.getCustomId());
                        tCruisePointInstance.setCruiseType(228);
                        tCruisePointInstance.setIfSy(1);
                        tCruisePointInstance.setStationId(tStdRegion.getStationId());
                        tCruisePointInstance.setStationName(tStdRegion.getStationName());
                        tCruisePointInstance.setCruiseId(tRobotInspection.getInspectionId());
                        tCruisePointInstance.setCruiseName(tRobotInspection.getInspectionName());
                        tCruisePointInstanceList.add(tCruisePointInstance);
                    });
                }
            });
            linkAutoMapper.batchInsertCruisePointInstance(tCruisePointInstanceList);
        }

        if (CollectionUtils.isNotEmpty(link)) {
            List<TCruisePointInstance> tCruisePointInstanceList = new ArrayList<>(insert.size());
            TStdRegion tStdRegion = linkAutoMapper.selectTSRegionForStation();
            link.forEach(o -> {
                List<TRobotInspection> tRobotInspections1 = o.getTRobotInspections();
                if (CollectionUtils.isNotEmpty(tRobotInspections1)) {
                    tRobotInspections1.forEach(tRobotInspection -> {
                        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
                        tCruisePointInstance.setDeviceMeteId(o.getDeviceMeteId());
                        tCruisePointInstance.setDeviceId(o.getDeviceId());
                        tCruisePointInstance.setCustomId(o.getCustomId());
                        tCruisePointInstance.setCruiseType(228);
                        tCruisePointInstance.setIfSy(1);
                        tCruisePointInstance.setStationId(tStdRegion.getStationId());
                        tCruisePointInstance.setStationName(tStdRegion.getStationName());
                        tCruisePointInstance.setCruiseId(tRobotInspection.getInspectionId());
                        tCruisePointInstance.setCruiseName(tRobotInspection.getInspectionName());
                        tCruisePointInstanceList.add(tCruisePointInstance);
                    });
                }
            });
            linkAutoMapper.batchInsertCruisePointInstance(tCruisePointInstanceList);
        }
    }

    public void importExcel(List<ExcelEntity> excelEntities, Long robotId) throws InterruptedException {
        log.info("开始执行导入:{}", new Date());
        List<TRobotInspection> tRobotInspectionsInfoModelFile = new ArrayList<>();

        String linkMete = redisTemplate.opsForHash().entries("t_sys_param:linkMete").get("content").toString();
        for (ExcelEntity excelEntity : excelEntities) {
            try {
                TRobotInspection tRobotInspection = new TRobotInspection();
                tRobotInspection.setInspectionCode(String.valueOf(excelEntity.getInspectionCode()));
                tRobotInspection.setRobotId(robotId);
                tRobotInspection.setInspectionName(excelEntity.getMainDeviceName() + "/" + excelEntity.getInspectionName());
                // save_type_list和recognition_type_list为空的话，容错  默认为jpg和1
                tRobotInspection.setDeviceName(String.valueOf(excelEntity.getInspectionName()));
                tRobotInspection.setSaveTypeList(String.valueOf("jpg"));
                tRobotInspection.setComponentId(excelEntity.getComponentId());
                tRobotInspection.setComponentName(excelEntity.getComponentName());
                tRobotInspection.setMainDeviceName(excelEntity.getMainDeviceName());
                tRobotInspection.setMainDeviceId(excelEntity.getMainDeviceId());
                tRobotInspection.setBayName(excelEntity.getRegionName());
                tRobotInspection.setDeviceType(excelEntity.getDeviceTypeId());
                tRobotInspection.setRecognitionTypeList("1");
                tRobotInspection.setAreaName(excelEntity.getAreaName());
                tRobotInspection.setRecognitionTypeList(excelEntity.getMeteTypeId());
                tRobotInspection.setSaveTypeList(excelEntity.getFileType());
                tRobotInspection.setInspectionType(1);
                tRobotInspectionsInfoModelFile.add(tRobotInspection);
                //            log.info("获得的deviceList是：" + deviceList);
            } catch (Exception e) {
                log.error("获取设备点位信息异常:", e);
            }
        }

        // 该机器人现有的巡检点
        List<String> nowInspectionList = tRobotInspectionDao.selectAllByRobotId(robotId);
        //        log.info("机器人现有的deviceList是：" + nowList);

        List<TRobotInspection> newInspectionList = new ArrayList<>();
        for (TRobotInspection tRobotInspection : tRobotInspectionsInfoModelFile) {
            if (nowInspectionList.contains(tRobotInspection.getInspectionCode())) {
                nowInspectionList.remove(tRobotInspection.getInspectionCode());
            } else {
                newInspectionList.add(tRobotInspection);
            }
        }
        log.info("最后要插库的deviceList是==={}", newInspectionList.size());
        log.info("准备要删除的inspectionCodeList是==={}", nowInspectionList.size());
        if (CollectionUtils.isNotEmpty(nowInspectionList)) {
            List<Long> inspectionIdList = tRobotInspectionDao.selectInspectionIdList(nowInspectionList);
            //            log.info("这些inspectionCode对应的inspectionIdList是==" + inspectionIdList);
            List<Long> instanceIdList = tRobotInspectionDao.selectInstanceIdList(inspectionIdList);
            //            log.info("这些inspectionId对应的instanceIdList是==" + instanceIdList);

            // 删库TRI
            int deleteCountTRI = 0, deleteCountTCPI = 0, deleteCountTCPA = 0, deleteCountTDM = 0;
            if (CollectionUtils.isNotEmpty(inspectionIdList)) {
                deleteCountTRI = tRobotInspectionDao.batchDeleteTRobotInspection(inspectionIdList);
            }
            if (CollectionUtils.isNotEmpty(instanceIdList)) {
                //查询出instanceId对应的设备测点信息
                List<Long> deviceMeteIds = tRobotInspectionDao.selectDeviceMeteIdByInstanceId(instanceIdList);
                // 删库TCPI
                deleteCountTCPI = tRobotInspectionDao.batchDeleteTCruisePointInstance(instanceIdList);
                // 删库TCPA
                deleteCountTCPA = tRobotInspectionDao.batchDeleteTCruisePlanAttr(instanceIdList);
                //查询出清理后的无绑定关系的测点
                List<Long> deviceMeteIdsDeleted = tRobotInspectionDao.selectDeviceMeteIdByDeviceMeteId(deviceMeteIds);
                deleteCountTDM = tRobotInspectionDao.batchDeleteTDeviceMete(deviceMeteIdsDeleted);
            }
            log.info("TRI删除条数=={},TCPI删除条数=={},TCPA删除条数=={},TDM删除条数=={}", deleteCountTRI, deleteCountTCPI, deleteCountTCPA,deleteCountTDM);
        }
        if (CollectionUtils.isNotEmpty(newInspectionList)) {
            // 插库TRI
            if (tRobotInspectionsInfoModelFile.size() > 1000) {
                List<List<TRobotInspection>> lists = Lists.partition(tRobotInspectionsInfoModelFile, 1000);
                CountDownLatch count = new CountDownLatch(lists.size());
                lists.forEach(list -> TaskExecutePool.getInstance().execute(() -> {
                    tRobotInspectionDao.batchInsertTRobotInspection(list);
                    count.countDown();
                }));
                count.await();
            } else {
                tRobotInspectionDao.batchInsertTRobotInspection(tRobotInspectionsInfoModelFile);
            }
        }

        List<TRobotInspection> updateInspections =
            tRobotInspectionsInfoModelFile.stream().filter(o -> !newInspectionList.contains(o)).collect(Collectors.toList());

        log.info("准备更新的deviceList是=={}", updateInspections.size());
        if (CollectionUtils.isNotEmpty(updateInspections)) {
            tRobotInspectionDao.batchUpdate(updateInspections);
        }
        //自动建立并连接测点
        if ("true".equals(linkMete)) {
            instanceLinkAuto(tRobotInspectionsInfoModelFile,robotId);
        }
    }
}
