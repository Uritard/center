package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.StdDeviceTreeConstant;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.InspectedDevTreeCondition;
import com.yjh.platform.module.device.entity.PatrolDevTreeCondition;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.AreaInfoDetail;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.entity.enums.UserStateEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private static final Logger log = LoggerFactory.getLogger(TStdDeviceTreeService.class);

    public TStdDeviceTreeService(SysUserDao sysUserDao, TCameraScreenDao tCameraScreenDao, TCameraInfoDao tCameraInfoDao,
                                 TRobotInfoDao tRobotInfoDao, TStdDeviceDao tStdDeviceDao) {
        this.sysUserDao = sysUserDao;
        this.tCameraScreenDao = tCameraScreenDao;
        this.tCameraInfoDao = tCameraInfoDao;
        this.tRobotInfoDao = tRobotInfoDao;
        this.tStdDeviceDao = tStdDeviceDao;
    }

    public List<AreaInfoDetail> selectPatrolDevTree(PatrolDevTreeCondition condition, Long userIdTemp) {
        String name = condition.getName();
        if (StringUtils.isEmpty(name)) {
            List<AreaInfoDetail> areaTree = tCameraInfoDao.selectCameraTreeRegion();
            return assembleTrees(areaTree);
        }

        Long userId = updateUserId(userIdTemp);
        Map<String, String> map = getCameraStatus();

        Integer flag = condition.getFlag();
        Long id = condition.getId();
        Integer type = condition.getType();
        String level = condition.getLevel();

        if (StringUtils.isNotEmpty(level)) {
            StdDeviceTreeConstant.PatrolDevLevel devLevel = StdDeviceTreeConstant.PatrolDevLevel.getLevel(level);
            switch (devLevel) {
                case REGION:
                    return areaTree(name, flag, "robotFlag", userId, map);
                case DEVICE:
                    return cameraTree(name, flag, "robotFlag", userId, id, map, type);
                case LAZY:
                    return new ArrayList<>();
                default:
                    throw new BusinessException("参数错误！");
            }
        }

        List<AreaInfoDetail> areaInfoDetails = new ArrayList<>();
        List<TCameraInfo> cameraList = tCameraInfoDao.selectCameraByName(name, null, userId);
        if (!CollectionUtils.isEmpty(cameraList)) {
            List<Long> regionList = cameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toList());

            List<TCameraInfo> finalCameraList = new ArrayList<>();
            if (flag == StdDeviceTreeConstant.OnlineState.ALL.getFlag()) {
                finalCameraList = cameraList;
            } else if (flag == StdDeviceTreeConstant.OnlineState.ONLINE.getFlag()) {
                finalCameraList = cameraList.stream().filter(tCameraInfo -> StringUtils.equals(map.get(tCameraInfo.getCameraId().toString()), "1")).collect(Collectors.toList());
            } else if (flag == StdDeviceTreeConstant.OnlineState.OFFLINE.getFlag()) {
                finalCameraList = cameraList.stream().filter(tCameraInfo -> !StringUtils.equals(map.get(tCameraInfo.getCameraId().toString()), "1")).collect(Collectors.toList());
            }

            // MySQL 8.0
//            if (!CollectionUtils.isEmpty(finalCameraList)) {
//                regionList.addAll(tCameraInfoDao.selectRegionByCameraList(finalCameraList));
//            }

            if (!CollectionUtils.isEmpty(finalCameraList)) {
                String upRegionList = finalCameraList.stream().map(TCameraInfo::getUpRegionId).map(String::valueOf).collect(Collectors.joining(","));
                regionList.addAll(tCameraInfoDao.selectRegionListByUpRegionId(upRegionList));
            }

            if (!CollectionUtils.isEmpty(regionList)) {
                areaInfoDetails = tCameraInfoDao.selectCameraTreeByName(finalCameraList, regionList);
            }
        }
        areaInfoDetails.forEach(treeNode -> {
            if ("camera".equals(treeNode.getInfoType())) {
                if (map.get(treeNode.getId().toString()) != null) {
                    treeNode.setState(Integer.valueOf(map.get(treeNode.getId().toString())));
                } else {
                    treeNode.setState(0);
                }
            }
        });
        return assembleTrees(areaInfoDetails);
    }

    private List<AreaInfo> areaTree(String deviceShow){
        List<AreaInfo> areaTree = tStdDeviceDao.selectDevTreeRegion();
        areaTree =  assembleTrees2(areaTree);
        if (!StringUtils.isEmpty(deviceShow)){
            areaTree =  areaAddDeviceTree(areaTree,deviceShow);
        }
        return areaTree;
    }

    private List<AreaInfoDetail> areaTree(String cameraName, Integer flag, String robotFlag, Long userId, Map<String,String> map){
        List<AreaInfoDetail> areaTree = tCameraInfoDao.selectCameraTreeRegion();
        areaTree = assembleTrees(areaTree);
        areaTree = areaAddDeviceTree(areaTree, cameraName, flag, robotFlag, userId, map);
        return areaTree;
    }

    public List<AreaInfo> assembleTrees2(Collection<AreaInfo> trees) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, AreaInfo> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(AreaInfo::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            AreaInfo parent = ifNull(tree.getUpId(), mapping::get);
            if (parent != null) {
                parent.getChildren().add(tree);
            }
            return Objects.isNull(parent);
        }).collect(Collectors.toList());
    }

    public List<AreaInfoDetail> assembleTrees(Collection<AreaInfoDetail> trees) {
        if (CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, AreaInfoDetail> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(AreaInfoDetail::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            AreaInfoDetail parent = ifNull(tree.getUpId(), mapping::get);
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
    public static <T, R> R ifNull(T object, Function<T, R> function) {
        return object == null || function == null ? null : function.apply(object);
    }

    private List<AreaInfo> areaAddDeviceTree(List<AreaInfo> areaTree, String deviceShow) {
        if (areaTree == null) {
            return Collections.emptyList();
        }
        areaTree.forEach(area -> {
            if ("region".equals(area.getInfoType())) {
                if (!CollectionUtils.isEmpty(area.getChildren())) {
                    StdDeviceTreeConstant.DeviceShowEnum value = StdDeviceTreeConstant.DeviceShowEnum.getValue(deviceShow);
                    switch (value) {
                        case ALL:
                            area.getChildren().addAll(tStdDeviceDao.selectAllByRegionId(area.getId()));
                            break;
                        case ALL_DEVICE:
                            area.getChildren().addAll(tStdDeviceDao.getRegionMonitorDevice(area.getId()));
                            break;
                        case DEV:
                            area.getChildren().addAll(tStdDeviceDao.selectDeviceByRegionId(area.getId()));
                            break;
                        case CAMERA:
                            area.getChildren().addAll(tStdDeviceDao.selectCameraByRegionId(area.getId()));
                            break;
                        case ROBOT:
                            area.getChildren().addAll(tStdDeviceDao.selectRobotByRegionId(area.getId()));
                            break;
                        default:
                            throw new BusinessException("设备树展示内容输入有误！");
                    }
                    areaAddDeviceTree(area.getChildren(), deviceShow);
                }
            }
        });
        return areaTree;
    }

    private List<AreaInfoDetail> areaAddDeviceTree(List<AreaInfoDetail> areaTree, String cameraName, Integer flag, String robotFlag, Long userId,Map<String,String> map){
        if (areaTree == null){
            return Collections.emptyList();
        }
        areaTree.forEach(area ->{
            if ("region".equals(area.getInfoType())){
                if (!CollectionUtils.isEmpty(area.getChildren())){
                    area.getChildren().addAll(cameraTree(cameraName,flag,robotFlag,userId,area.getId(),map, null));
                    areaAddDeviceTree(area.getChildren(),cameraName,flag,robotFlag,userId,map);
                }
            }
        });
        return areaTree;
    }

    private List<AreaInfoDetail> cameraTree(String cameraName, Integer flag, String robotFlag, Long userId, Long upRegionId,
                                            Map<String, String> map, Integer cameraType) {
        List<AreaInfoDetail> childrenList = new ArrayList<>();
        List<AreaInfoDetail> child = tCameraInfoDao.selectCameraTreeWithRobotNew(cameraName, robotFlag, userId, upRegionId, cameraType);
        child.forEach(children -> {
            if ("camera".equals(children.getInfoType())) {
                if (map.get(children.getId().toString()) != null) {
                    children.setState(Integer.valueOf(map.get(children.getId().toString())));
                } else {
                    children.setState(0);
                }
                if (flag != null) {
                    if (flag.equals(children.getState())) {
                        childrenList.add(children);
                    } else if (flag == 2) {
                        childrenList.add(children);
                    }
                }
            }
            if ("robot".equals(children.getInfoType())) {
                //机器人
                TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(children.getId());
                List<AreaInfoDetail> robotCameraList = new ArrayList<>();
                //可见光
                AreaInfoDetail lightCamera = new AreaInfoDetail();
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

                if (flag != null) {
                    if (flag.equals(lightCamera.getState()) && (flag == 1 || flag == 0)) {
                        robotCameraList.add(lightCamera);
                    } else {
                        robotCameraList.add(lightCamera);
                    }
                }
                //红外
                AreaInfoDetail redCamera = new AreaInfoDetail();
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

                if (flag != null) {
                    if (flag.equals(redCamera.getState()) && (flag == 1 || flag == 0)) {
                        robotCameraList.add(redCamera);
                    } else {
                        robotCameraList.add(redCamera);
                    }
                }
                children.setChildren(robotCameraList);
                if (flag != null) {
                    if ((flag == 1 || flag == 0)) {
                        if (flag.equals(children.getState())) {
                            childrenList.add(children);
                        }
                    } else {
                        childrenList.add(children);
                    }
                }
            }
        });
        return childrenList;
    }

    private Long updateUserId(Long userId) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);
        if (Objects.nonNull(sysUser) && UserStateEnum.INVALID.getCode() == sysUser.getState()) {
            throw new BusinessException("用户不存在或已删除");
        } else {
            if (1234L == sysUser.getRoleId()) {
                userId = null;
            }
            else if(1235L == sysUser.getRoleId()) {

            } else {
                throw  new BusinessException("当前用户角色不可查看");
            }
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

    public List<AreaInfo> selectInspectedDevTree(InspectedDevTreeCondition condition) {
        String name = condition.getName();
        String type = condition.getType();
        String deviceShow = condition.getDeviceShow();
        String meteType = condition.getMeteType();
        if (StringUtils.isNotEmpty(name)) {
            List<AreaInfo> devTreeByName = new ArrayList<>();
            StdDeviceTreeConstant.FilterType filterType = StdDeviceTreeConstant.FilterType.getType(type);
            switch (filterType){
                case REGION:
                    //针对region的过滤
                    List<AreaInfo> allTree = tStdDeviceDao.selectAreaTree();
                    allTree = assembleTrees2(allTree);
                    if (StringUtils.isNotEmpty(name)){
                        if (!matchName(allTree.get(0),name)){
                            allTree.remove(0);
                        }
                    }
                    return allTree;
                case DEV:
                    if ("camera".equals(deviceShow)){
                        //查相机设备
                        List<TCruisePointInstance> cameraList = tStdDeviceDao.selectCameraTreeDeviceByName(name);
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(cameraList)){
                            List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(cameraList);
                            //  MySQL 8.0
//                        regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
                            String upRegionList = regionList.stream().map(String :: valueOf).collect(Collectors.joining(","));
                            regionList.addAll(tCameraInfoDao.selectRegionListByUpRegionId(upRegionList));
                            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(regionList)){
                                devTreeByName = tStdDeviceDao.selectDevTreeDeviceByNameTree(cameraList,regionList);
                            }
                        }
                    }else if ("allDevice".equals(deviceShow)){
                        List<TCameraInfo> cameraList = tStdDeviceDao.selectAllPatrolDeviceByName(name);
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(cameraList)) {
                            List<Long> regionList = cameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toList());
                            List<Long> allRegionList = getAllUpRegionId(regionList);
                            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(regionList)) {
                                devTreeByName = tStdDeviceDao.selectAllPatrolDeviceTreeByName(cameraList, allRegionList);
                            }
                        }
                    }else {
                        //查设备
                        List<TCruisePointInstance> deviceList = tStdDeviceDao.selectDevTreeDeviceByName(name);
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(deviceList)){
                            List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(deviceList);
                            //  MySQL 8.0
//                        regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
                            String upRegionList = regionList.stream().map(String :: valueOf).collect(Collectors.joining(","));
                            regionList.addAll(tCameraInfoDao.selectRegionListByUpRegionId(upRegionList));
                            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(regionList)){
                                devTreeByName = tStdDeviceDao.selectDevTreeDeviceByNameTree(deviceList,regionList);
                            }
                        }
                    }
                    break;
                case INS:
                    //查巡视点
                    List<TCruisePointInstance> insList = tStdDeviceDao.selectAllMeteCruiseTreeByName(name, meteType);
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(insList)){
                        List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(insList);
                        //  MySQL 8.0
//                    regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
                        String upRegionList = regionList.stream().map(String :: valueOf).collect(Collectors.joining(","));
                        regionList.addAll(tCameraInfoDao.selectRegionListByUpRegionId(upRegionList));
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(regionList)){

                            devTreeByName = tStdDeviceDao.selectAllMeteCruiseTreeByNameTree(insList,regionList);
                        }
                    }
                    break;
                default: throw new BusinessException("设备树展示层级输入有误！");
            }
            return assembleTrees2(devTreeByName);
        }

        Long id = condition.getId();
        StdDeviceTreeConstant.InspectedDevLevel level = StdDeviceTreeConstant.InspectedDevLevel.getLevel(condition.getLevel());
        switch (level) {
            case REGION:
                return areaTree(deviceShow);
            case DEVICE:
                return deviceTree(id, deviceShow);
            case POSITION:
                return customTree(id);
            case DEVICE_METE:
                return deviceMeteTree(id, meteType, condition.getAnalyseType());
            case INSTANCE:
                return cruisePoint(id);
            case LAZY:
                return new ArrayList<>();
            default:
                throw new BusinessException("设备树展示层级输入有误！");
        }
    }

    private Boolean matchName(AreaInfo node,String regionName){
        if (node.getLabel().contains(regionName)){
            return true;
        }else {
            List<AreaInfo> child = node.getChildren();
            List<AreaInfo> newChild = new ArrayList<>();
            if (child != null && child.size() > 0){
                for (AreaInfo nodeItem : child){
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

    public List<AreaInfo> customTree(Long deviceId){
        return tStdDeviceDao.selectCustomByRegionId(deviceId);
    }

    public List<AreaInfo> cruisePoint(Long deviceMeteId){
        return tStdDeviceDao.selectCruisePointByDeviceMeteId(deviceMeteId);
    }

    public List<AreaInfo> deviceMeteTree(Long deviceId,String deviceType, String analyseType){
        List<AreaInfo> areaInfos = tStdDeviceDao.selectDeviceMeteByDeviceAndCustom(deviceId,deviceType,analyseType);
        List<AreaInfo> uniqueAreaInfos = areaInfos.stream()
                .collect(Collectors.toMap(AreaInfo::getId, Function.identity(), (existing, replacement) -> {
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

    public List<AreaInfo> deviceTree(Long upRegionId,String deviceShow){
        List<AreaInfo> deviceTree;
        switch (deviceShow){
            case "all":
                deviceTree = tStdDeviceDao.selectAllByRegionId(upRegionId);
                break;
            case "allDevice":
                deviceTree = tStdDeviceDao.getRegionMonitorDevice(upRegionId);
                break;
            case "dev":
                deviceTree = tStdDeviceDao.selectDeviceByRegionId(upRegionId);
                break;
            case "camera":
                deviceTree = tStdDeviceDao.selectCameraByRegionId(upRegionId);
                break;
            case "robot":
                deviceTree = tStdDeviceDao.selectRobotByRegionId(upRegionId);
                break;
            default:throw new BusinessException("设备树展示内容输入有误！");
        }
        return deviceTree;
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

}

