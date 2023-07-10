package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.StdDeviceTreeConstant;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDeviceTreeDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.DevSynthesisTreeCondition;
import com.yjh.platform.module.device.entity.SynthesisTreeAreaInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.entity.enums.UserStateEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备树统一接口查询
 *
 * @author 丫C
 * @date 2023/06/29
 * @since [产品/模块版本] （可选）
 */
@Service
public class TStdDeviceTreeService {

    private final SysUserDao sysUserDao;
    private final TCameraScreenDao tCameraScreenDao;
    private final TCameraInfoDao tCameraInfoDao;
    private final TRobotInfoDao tRobotInfoDao;
    private final TStdDeviceDao tStdDeviceDao;
    private final TStdDeviceTreeDao stdDeviceTreeDao;
    private static final Logger log = LoggerFactory.getLogger(TStdDeviceTreeService.class);

    public TStdDeviceTreeService(SysUserDao sysUserDao, TCameraScreenDao tCameraScreenDao, TCameraInfoDao tCameraInfoDao,
                                 TRobotInfoDao tRobotInfoDao, TStdDeviceDao tStdDeviceDao, TStdDeviceTreeDao stdDeviceTreeDao) {
        this.sysUserDao = sysUserDao;
        this.tCameraScreenDao = tCameraScreenDao;
        this.tCameraInfoDao = tCameraInfoDao;
        this.tRobotInfoDao = tRobotInfoDao;
        this.tStdDeviceDao = tStdDeviceDao;
        this.stdDeviceTreeDao = stdDeviceTreeDao;
    }

    public List<SynthesisTreeAreaInfo> selectDevSynthesisTree(DevSynthesisTreeCondition condition, Long userId) {
        String name = condition.getName();
        if (StringUtils.isEmpty(name)) {
            // 直接获取树
            return devSynthesisTree(condition, userId);
        }
        // 根据名称获取树
        return devSynthesisTreeByName(condition, userId);
    }

    private List<SynthesisTreeAreaInfo> devSynthesisTree(DevSynthesisTreeCondition condition, Long userId) {
        String level = condition.getLevel();
        StdDeviceTreeConstant.InspectedDevLevel levelTemp = StdDeviceTreeConstant.InspectedDevLevel.getLevel(level);

        String deviceShow = condition.getDeviceShow();
        Long id = condition.getId();
        String meteType = condition.getMeteType();
        Integer flag = condition.getFlag();
        String type = condition.getType();
        String analyseType = condition.getAnalyseType();
        switch (levelTemp) {
            case REGION:
                return areaTree(deviceShow, flag, userId, type);
            case DEVICE:
                return deviceTree(id, deviceShow, flag, userId, type);
            case POSITION:
                return customTree(id);
            case DEVICE_METE:
                return deviceMeteTree(id, meteType, analyseType);
            case INSTANCE:
                return cruisePoint(id);
            case LAZY:
                return new ArrayList<>();
            default:
                throw new BusinessException("设备树展示层级输入有误！");
        }
    }

    private List<SynthesisTreeAreaInfo> areaTree(String deviceShow, Integer flag, Long userId, String type) {
        List<SynthesisTreeAreaInfo> areaTree = stdDeviceTreeDao.selectSynthesisTreeRegion();
        areaTree = assembleSynthesisTrees(areaTree);
        if (StringUtils.isNotEmpty(deviceShow)) {
            areaTree = areaAddDeviceTree(areaTree, deviceShow, flag, userId, type);
        }
        return areaTree;
    }

    private List<SynthesisTreeAreaInfo> areaAddDeviceTree(List<SynthesisTreeAreaInfo> areaTree, String deviceShow, Integer flag, Long userId, String type) {
        if (areaTree == null) {
            return Collections.emptyList();
        }

        areaTree.forEach(area -> {
            if ("region".equals(area.getInfoType())) {
                if (CollectionUtils.isNotEmpty(area.getChildren())) {
                    StdDeviceTreeConstant.DeviceShowEnum value = StdDeviceTreeConstant.DeviceShowEnum.getValue(deviceShow);
                    switch (value) {
                        case ALL:
                            area.getChildren().addAll(stdDeviceTreeDao.selectAllByRegionId(area.getId()));
                            break;
                        case ALL_DEVICE:
                            area.getChildren().addAll(stdDeviceTreeDao.getRegionMonitorDevice(area.getId()));
                            break;
                        case DEV:
                            area.getChildren().addAll(stdDeviceTreeDao.selectDeviceByRegionId(area.getId()));
                            break;
                        case CAMERA:
                            area.getChildren().addAll(cameraAndRobotTree(flag, userId, "", area.getId(), type));
                            break;
                        case ROBOT:
                            area.getChildren().addAll(cameraAndRobotTree(flag, userId, "不为空", area.getId(), type));
                            break;
                        default:
                            throw new BusinessException("设备树展示内容输入有误！");
                    }
                    areaAddDeviceTree(area.getChildren(), deviceShow, flag, userId, type);
                }
            }
        });
        return areaTree;
    }

    private List<SynthesisTreeAreaInfo> deviceTree(Long upRegionId, String deviceShow, Integer flag, Long userId, String type){
        List<SynthesisTreeAreaInfo> deviceTree;
        StdDeviceTreeConstant.DeviceShowEnum value = StdDeviceTreeConstant.DeviceShowEnum.getValue(deviceShow);

        switch (value) {
            case ALL:
                deviceTree = stdDeviceTreeDao.selectAllByRegionId(upRegionId);
                break;
            case ALL_DEVICE:
                deviceTree = stdDeviceTreeDao.getRegionMonitorDevice(upRegionId);
                break;
            case DEV:
                deviceTree = stdDeviceTreeDao.selectDeviceByRegionId(upRegionId);
                break;
            case CAMERA:
                deviceTree = cameraAndRobotTree(flag, userId, "", upRegionId, type);
                break;
            case ROBOT:
                deviceTree = cameraAndRobotTree(flag, userId, "不为空", upRegionId, type);
                break;
            default:throw new BusinessException("设备树展示内容输入有误！");
        }
        return deviceTree;
    }

    private List<SynthesisTreeAreaInfo> customTree(Long deviceId){
        return stdDeviceTreeDao.selectCustomByRegionId(deviceId);
    }

    private List<SynthesisTreeAreaInfo> deviceMeteTree(Long deviceId, String meteType, String analyseType){
        List<SynthesisTreeAreaInfo> areaInfos = stdDeviceTreeDao.selectDeviceMeteByDeviceAndCustom(deviceId, meteType, analyseType);
        List<SynthesisTreeAreaInfo> uniqueAreaInfos = areaInfos.stream()
                .collect(Collectors.toMap(SynthesisTreeAreaInfo::getId, Function.identity(), (existing, replacement) -> {
                    if (StringUtils.isBlank(existing.getCameraId()) && StringUtils.isBlank(replacement.getCameraId())) {
                        return existing;
                    } else if (StringUtils.isNotBlank(existing.getCameraId()) && StringUtils.isNotBlank(replacement.getCameraId())) {
                        return existing;
                    } else if (StringUtils.isNotBlank(existing.getCameraId())) {
                        return existing;
                    } else {
                        return replacement;
                    }
                })).values().stream().collect(Collectors.toList());
        return uniqueAreaInfos;
    }

    private List<SynthesisTreeAreaInfo> cruisePoint(Long deviceMeteId){
        return stdDeviceTreeDao.selectCruisePointByDeviceMeteId(deviceMeteId);
    }

    private List<SynthesisTreeAreaInfo> cameraAndRobotTree(Integer flag, Long userId, String robotFlag, Long upRegionId, String cameraType) {
        Long userIdTemp = null;
        Integer cameraTypeTemp = null;
        // flag为空：被巡视设备 否则为巡视设备
        Map<String, String> mapTemp = new HashMap<>(8);
        if (null != flag){
            userIdTemp = updateUserId(userId);
            mapTemp = getCameraStatus();
            if (StringUtils.equals("light", cameraType) || StringUtils.equals("infrared", cameraType)) {
                cameraTypeTemp = StringUtils.equals("light", cameraType) ? 205 : 206;
            }
        }
        List<SynthesisTreeAreaInfo> child = stdDeviceTreeDao.selectCameraOrRobot(robotFlag, userIdTemp, upRegionId, cameraTypeTemp);

        if (null == flag) {
            return child;
        }
        // 给设备赋值在线状态
        List<SynthesisTreeAreaInfo> childrenList = new ArrayList<>();
        Map<String, String> map = mapTemp;
        child.forEach(children -> {
            if (StringUtils.equals("camera", children.getInfoType())) {
                String id = map.get(children.getId().toString());
                children.setState(id != null ? Integer.valueOf(id) : 0);

                if (children.getState().equals(flag) || (flag == 2)) {
                    childrenList.add(children);
                }
            }
            if (StringUtils.equals("robot", children.getInfoType())) {
                TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(children.getId());
                List<SynthesisTreeAreaInfo> robotCameraList = new ArrayList<>();

                SynthesisTreeAreaInfo lightCamera = new SynthesisTreeAreaInfo();
                String light = tRobotInfo.getRobotId() + "9901";
                lightCamera.setId(Long.parseLong(light));
                lightCamera.setLabel("机器人可见光");
                lightCamera.setInfoType("robotCamera");
                lightCamera.setUpId(children.getId());
                lightCamera.setUpName(tRobotInfo.getRobotCode());

                if ("在线".equals(tRobotInfo.getRobotStatus())) {
                    children.setState(1);
                    lightCamera.setState(1);
                } else {
                    children.setState(0);
                    lightCamera.setState(0);
                }

                if (flag.equals(lightCamera.getState()) && (flag == 1 || flag == 0)) {
                    robotCameraList.add(lightCamera);
                } else {
                    robotCameraList.add(lightCamera);
                }

                SynthesisTreeAreaInfo redCamera = new SynthesisTreeAreaInfo();
                String infrared = tRobotInfo.getRobotId() + "9902";
                redCamera.setId(Long.parseLong(infrared));
                redCamera.setLabel("机器人红外");
                redCamera.setInfoType("robotCamera");
                redCamera.setUpId(children.getId());
                redCamera.setUpName(tRobotInfo.getRobotCode());

                if ("在线".equals(tRobotInfo.getRobotStatus())) {
                    children.setState(1);
                    redCamera.setState(1);
                } else {
                    children.setState(0);
                    redCamera.setState(0);
                }

                if (flag.equals(redCamera.getState()) && (flag == 1 || flag == 0)) {
                    robotCameraList.add(redCamera);
                } else {
                    robotCameraList.add(redCamera);
                }
                children.setChildren(robotCameraList);
                if ((flag == 1 || flag == 0)) {
                    if (flag.equals(children.getState())) {
                        childrenList.add(children);
                    }
                } else {
                    childrenList.add(children);
                }
            }
        });
        return childrenList;
    }

    private Long updateUserId(Long userId) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);

        if (sysUser == null) {
            throw new BusinessException("用户不存在或已删除");
        }
        if (UserStateEnum.INVALID.getCode() == sysUser.getState()) {
            throw new BusinessException("用户不存在或已删除");
        }
        Long roleId = sysUser.getRoleId();

        if (1234L == roleId) {
            userId = null;
        }
        else if(1235L == roleId) {

        } else {
            throw new BusinessException("当前用户角色不可查看");
        }
        return userId;
    }

    /**
     * 获取相机的状态
     * map包含在离线的  不包含未知状态的
     *
     * @return Map<String, String>
     */
    private Map<String,String> getCameraStatus() {
        Map<String,String> map = new HashMap<>(8);
        try {
            List<Long> recordIdList = tCameraScreenDao.selectRecordId();
            for(Long recordId:recordIdList){
                HashMap<String, Object> recordIdMap = new HashMap<>(4);
                recordIdMap.put("recordId",recordId );
                Result re = cameraStates(recordIdMap);
                if(re == null){
                    continue;
                }
                map.putAll((Map<String, String>)re.getData());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return map;
    }

    private static Result cameraStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    private List<SynthesisTreeAreaInfo> devSynthesisTreeByName(DevSynthesisTreeCondition condition, Long userId) {
        String name = condition.getName();
        if (StringUtils.isEmpty(name)) {
            //这不是傻逼嘛 没有名称  查啥
        }
        String level = condition.getLevel();

        // level不为空：被巡视设备 否则为巡视设备
        if (StringUtils.isNotEmpty(level)) {
            String type = condition.getType();
            String meteType = condition.getMeteType();
            String deviceShow = condition.getDeviceShow();

            StdDeviceTreeConstant.FilterType filterType = StdDeviceTreeConstant.FilterType.getType(type);
            switch (filterType) {
                case REGION:
                    return areaTreeByName(name);
                case DEV:
                    return devTreeByName(name, deviceShow);
                case INS:
                    return cruisePointByName(name, meteType);
                default:
                    throw new BusinessException("设备树展示过滤条件输入有误！");
            }
        }

        List<SynthesisTreeAreaInfo> patrolTree = devPatrolTreeByName(condition, userId);
        return patrolTree;
    }

    private List<SynthesisTreeAreaInfo> areaTreeByName(String name) {
        List<SynthesisTreeAreaInfo> areTree = stdDeviceTreeDao.selectSynthesisTreeRegion();
        areTree = assembleSynthesisTrees(areTree);
        if (StringUtils.isNotEmpty(name) && !matchName(areTree.get(0), name)) {
            areTree.remove(0);
        }
        return areTree;
    }

    private Boolean matchName(SynthesisTreeAreaInfo node,String regionName){
        if (node.getLabel().contains(regionName)){
            return true;
        }else {
            List<SynthesisTreeAreaInfo> child = node.getChildren();
            List<SynthesisTreeAreaInfo> newChild = new ArrayList<>();
            if (child != null && child.size() > 0){
                for (SynthesisTreeAreaInfo nodeItem : child){
                    if (matchName(nodeItem,regionName)){
                        newChild.add(nodeItem);
                    }
                }
            }
            node.setChildren(newChild);
            if (newChild.size() > 0){
                return true;
            }
            return false;
        }
    }

    private List<SynthesisTreeAreaInfo> devTreeByName(String name, String deviceShow) {
        StdDeviceTreeConstant.DeviceShowEnum value = StdDeviceTreeConstant.DeviceShowEnum.getValue(deviceShow);
        switch (value) {
            case CAMERA:
                // 暂时没用到 先不管
                return devTreeCameraByName();
            case ALL_DEVICE:
                return devTreeAllDeviceByName(name);
            case DEV:
                return devTreeDevByName(name);
            default:
                throw new BusinessException("设备树展示过滤条件输入有误！");
        }
    }

    private List<SynthesisTreeAreaInfo> devTreeCameraByName() {
        List<SynthesisTreeAreaInfo> devTreeByName = new ArrayList<>();
        // 暂时还没有 后面出现再完善
        return assembleSynthesisTrees(devTreeByName);
    }

    private List<SynthesisTreeAreaInfo> devTreeAllDeviceByName(String name) {
        List<SynthesisTreeAreaInfo> devTreeByName = new ArrayList<>();

        List<TCameraInfo> cameraList = stdDeviceTreeDao.selectAllPatrolDeviceByName(name);
        if (CollectionUtils.isNotEmpty(cameraList)) {
            List<Long> regionList = cameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toList());
            List<Long> allRegionList = getAllUpRegionId(regionList);
            if (CollectionUtils.isNotEmpty(regionList)) {
                devTreeByName = stdDeviceTreeDao.selectAllPatrolDeviceTreeByName(cameraList, allRegionList);
            }
        }
        return assembleSynthesisTrees(devTreeByName);
    }

    private List<Long> getAllUpRegionId(List<Long> regionList){
        List<AreaInfo> allRegionList = tStdDeviceDao.selectAllRegion();
        List<Long> reList = new ArrayList<>(regionList);
        for (Long regionId : regionList){
            Long id = getUpRegionId(allRegionList,regionId);
            if (reList.contains(id)){
                continue;
            }
            while (true){
                reList.add(id);
                id = getUpRegionId(allRegionList,reList.get(reList.size() - 1));
                if (id == -1){
                    break;
                }
            }
        }
        return reList;
    }

    private Long getUpRegionId(List<AreaInfo> regionList,Long regionId) {
        for (AreaInfo areaInfo: regionList) {
            if (Objects.equals(areaInfo.getId(), regionId)){
                return areaInfo.getUpId();
            }
        }
        return  -1L;
    }

    private List<SynthesisTreeAreaInfo> devTreeDevByName(String name) {
        List<SynthesisTreeAreaInfo> devTreeByName = new ArrayList<>();

        List<TCruisePointInstance> deviceList = stdDeviceTreeDao.selectDevTreeDeviceByName(name);
        if (CollectionUtils.isNotEmpty(deviceList)){
            List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(deviceList);
            //  MySQL 8.0
//            regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
            regionList = getRegionIdByLeafNode(new HashSet<>(regionList));
            if (CollectionUtils.isNotEmpty(regionList)){
                devTreeByName = stdDeviceTreeDao.selectDevTreeDeviceByNameTree(deviceList, regionList);
            }
        }
        return assembleSynthesisTrees(devTreeByName);
    }

    private List<SynthesisTreeAreaInfo> cruisePointByName(String name, String meteType) {
        List<SynthesisTreeAreaInfo> devTreeByName = new ArrayList<>();

        List<TCruisePointInstance> insList = stdDeviceTreeDao.selectAllMeteCruiseTreeByName(name, meteType);
        if (CollectionUtils.isNotEmpty(insList)){
            List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(insList);
            //  MySQL 8.0
//            regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
            regionList = getRegionIdByLeafNode(new HashSet<>(regionList));
            if (CollectionUtils.isNotEmpty(regionList)){
                devTreeByName = stdDeviceTreeDao.selectAllMeteCruiseTreeByNameTree(insList, regionList);
            }
        }
        return assembleSynthesisTrees(devTreeByName);
    }

    private List<Long> getRegionIdByLeafNode(Set<Long> regionParam) {
        Set<Long> regionTemp = new HashSet<>(regionParam);
        do {
            //通过regionList查询上层节点，后将结果放入插入参数继续查询，直到结果与入参一致
            regionParam.addAll(regionTemp);
            regionTemp.addAll(tCameraInfoDao.selectRegionListByUpRegionId(regionParam));
        } while (!regionParam.containsAll(regionTemp));
        return  new ArrayList<>(regionTemp);
    }

    private List<SynthesisTreeAreaInfo> assembleSynthesisTrees(Collection<SynthesisTreeAreaInfo> trees) {
        if (CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, SynthesisTreeAreaInfo> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(SynthesisTreeAreaInfo::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            SynthesisTreeAreaInfo parent = ifNull(tree.getUpId(), mapping::get);
            if (parent != null) {
                parent.getChildren().add(tree);
            }
            return Objects.isNull(parent);
        }).collect(Collectors.toList());
    }

    /**
     * 返回不为空的对象（如果第一个对象为空，则返回第二个对象）
     *
     * @param object   目标对象
     * @param function 目标对象方法
     * @param <T>      目标对象类型泛型
     * @param <R>      返回对象类型泛型
     * @return 返回对象
     */
    private static <T, R> R ifNull(T object, Function<T, R> function) {
        return object == null || function == null ? null : function.apply(object);
    }

    private List<SynthesisTreeAreaInfo> devPatrolTreeByName(DevSynthesisTreeCondition condition, Long userId) {
        Long updateUserId = updateUserId(userId);
        Map<String, String> map = getCameraStatus();


        List<SynthesisTreeAreaInfo> areaInfoDetails = new ArrayList<>();
        String name = condition.getName();
        List<TCameraInfo> cameraList = tCameraInfoDao.selectCameraByName(name, "", updateUserId);
        if (!CollectionUtils.isEmpty(cameraList)){
            List<Long> regionList = cameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toList());
            Integer flag = condition.getFlag();

            List<TCameraInfo> finalCameraList = new ArrayList<>();
            if (StdDeviceTreeConstant.OnlineState.ALL.getFlag() == flag){
                finalCameraList = cameraList;
            } else if (StdDeviceTreeConstant.OnlineState.ONLINE.getFlag() == flag) {
                finalCameraList = cameraList.stream().filter(tCameraInfo -> StringUtils.equals(map.get(tCameraInfo.getCameraId().toString()), "1")).collect(Collectors.toList());
            } else if (StdDeviceTreeConstant.OnlineState.OFFLINE.getFlag() == flag) {
                finalCameraList = cameraList.stream().filter(tCameraInfo -> !StringUtils.equals(map.get(tCameraInfo.getCameraId().toString()), "1")).collect(Collectors.toList());
            }

            // MySQL 8.0
//            if (!CollectionUtils.isEmpty(finalCameraList)) {
//                regionList.addAll(tCameraInfoDao.selectRegionByCameraList(finalCameraList));
//            }

            if (CollectionUtils.isNotEmpty(finalCameraList)) {
                Set<Long> upRegionList = finalCameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toSet());
                Set<Long> upRegionResult = new HashSet<>(upRegionList);
                do {
                    upRegionList.addAll(upRegionResult);
                    upRegionResult.addAll(tCameraInfoDao.selectRegionListByUpRegionId(upRegionList));
                } while (!upRegionList.containsAll(upRegionResult));
                regionList.addAll(upRegionResult);
            }

            if (CollectionUtils.isNotEmpty(regionList)){
                areaInfoDetails = stdDeviceTreeDao.selectCameraTreeByName(finalCameraList,regionList);
            }
        }
        areaInfoDetails.forEach(treeNode ->{
            if("camera".equals(treeNode.getInfoType())){
                String id = map.get(treeNode.getId().toString());
                treeNode.setState(id != null ? Integer.valueOf(id) : 0);

                if(map.get(treeNode.getId().toString()) != null){
                    treeNode.setState(Integer.valueOf(map.get(treeNode.getId().toString())));
                }else {
                    treeNode.setState(0);
                }
            }
        });
        return assembleSynthesisTrees(areaInfoDetails);
    }
}

