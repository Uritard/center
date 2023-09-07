package com.yjh.platform.module.user.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.entity.enums.UserStateEnum;
import com.yjh.platform.module.video.controller.CameraConController;
import com.yjh.platform.module.video.service.CameraConService;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author lqh
* @since 2020-10-16
*/
@Service
public class TCameraScreenService{

    @Autowired
    private TCameraScreenDao tCameraScreenDao;
    @Autowired
    private TCameraInfoDao tCameraInfoDao;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private CameraConService cameraConService;


    @Autowired
    private SysUserDao sysUserDao;

    private Logger log = LoggerFactory.getLogger(TCameraScreenService.class);

    @Transactional(rollbackFor = Exception.class)
    public int add(TCameraScreen tCameraScreen) {
        return this.tCameraScreenDao.add(tCameraScreen);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long userId) {
        return this.tCameraScreenDao.deleteByPrimaryId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Date update(TCameraScreen tCameraScreen,String userId) {
        Long user = Long.parseLong(userId);
        tCameraScreen.setUserId(user);
        TCameraScreen ifSave =this.tCameraScreenDao.selectByPrimaryId(user);
        if(ifSave != null){
            this.tCameraScreenDao.update(tCameraScreen);
            return tCameraScreen.getCreateTime();
        }else {
            this.tCameraScreenDao.add(tCameraScreen);
            return tCameraScreen.getCreateTime();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public TCameraScreenDetail selectByPrimaryId(Long userId) {
        TCameraScreenDetail tCameraScreenDetail = this.tCameraScreenDao.selectByPrimaryId(userId);
        if(tCameraScreenDetail == null){
            return null;
        }
        List<Object> cameraList = new LinkedList<>();
        String[] strList = tCameraScreenDetail.getCameraIds().split(",");
        for (String item: strList) {
            Camera camera = new Camera();
            if(item.isEmpty()){
                camera.setCameraId("");
                camera.setCameraName("");
            }else {
                if (StringUtils.indexOfAny(item,"9901", "9902") >= 1){
                    Long robotId = item.contains("9901") ? Long.parseLong(item.replace("9901", "")) : Long.parseLong(item.replace("9902", ""));
                    TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(robotId);
                    if (tRobotInfo == null) {
                        tCameraScreenDetail.setCameraIds(tCameraScreenDetail.getCameraIds().replace("," + item, ",").replace(item + ",", ",").replace(item, ","));
                        camera.setCameraId("");
                        camera.setCameraName("");
                        cameraList.add(camera);
                        this.update(tCameraScreenDetail, userId.toString());
                        continue;
                    }
                    camera.setCameraId(item);
                    if (item.contains("9901")) {
                        camera.setCameraName(tRobotInfo.getRobotName() + "机器人可见光");
                        camera.setCameraType(2);
                    } else {
                        camera.setCameraName(tRobotInfo.getRobotName() + "机器人红外");
                        camera.setCameraType(3);
                    }
                }else {
                    TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(Long.parseLong(item));
                    if (tCameraInfo == null) {
                        tCameraScreenDetail.setCameraIds(tCameraScreenDetail.getCameraIds().replace("," + item, ",").replace(item + ",", ",").replace(item, ","));
                        camera.setCameraId("");
                        camera.setCameraName("");
                        cameraList.add(camera);
                        this.update(tCameraScreenDetail, userId.toString());
                        continue;
                    }
                    camera.setCameraId(tCameraInfo.getCameraId().toString());
                    camera.setCameraName(tCameraInfo.getCameraName());
                    camera.setCameraType(1);
                }
            }
            cameraList.add(camera);
        }
        tCameraScreenDetail.setCameraList(cameraList);
        return tCameraScreenDetail;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraScreen> select(Long userId, String screenNum, String cameraIds, Date createTime) {
        List<TCameraScreen> tCameraScreenList = tCameraScreenDao.select(userId, screenNum, cameraIds, createTime);
        return tCameraScreenList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraScreen> selectByPage(TCameraScreen tCameraScreen) {
        List<TCameraScreen> tCameraScreenList = tCameraScreenDao.selectByPage(tCameraScreen);
        return tCameraScreenList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCameraScreen> list) {
        return this.tCameraScreenDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String userId) {
    List<String> list1= Arrays.asList(userId.split(","));
    return this.tCameraScreenDao.batchDelete(list1);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDetail> cameraStateTree(String cameraName,Integer flag) {
        List<AreaInfoDetail> listTree = new ArrayList<>();
        listTree = tCameraScreenDao.selectCameraTreeDevice(cameraName);
        List<AreaInfoDetail> areaInfoCountryList = new ArrayList<>();
        for(Iterator<AreaInfoDetail> it = listTree.iterator(); it.hasNext();){
            AreaInfoDetail areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfoDetail areaInfoCountry = new AreaInfoDetail();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        Map<String,String> map = new HashMap<>();
        //获取相机的状态
        List<Map<String,String>> listForState = new ArrayList<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            map.putAll(cameraConService.getCameraStatus(recordId));
        }
        diGui(areaInfoCountryList, listTree,map,flag);
        return areaInfoCountryList;
    }
    private void diGui(List<AreaInfoDetail> areaInfoList, List<AreaInfoDetail> listTree,Map<String,String> map,Integer flag) {
        for(AreaInfoDetail areaInfo : areaInfoList){
            List<AreaInfoDetail> childrenList = new ArrayList<>();
            for(Iterator<AreaInfoDetail> it = listTree.iterator();it.hasNext();){
                AreaInfoDetail areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    AreaInfoDetail areaInfoTem = new AreaInfoDetail();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    if("camera".equals(areaInfoMap.getInfoType())){
                        if(map.get(areaInfoMap.getId().toString()) != null){
                            areaInfoTem.setState(Integer.valueOf(map.get(areaInfoMap.getId().toString())));
                        }else {
                            areaInfoTem.setState(0);
                        }
                        if(flag != null && flag == 1){
                            if(1 == areaInfoTem.getState()){
                                //在线
                                childrenList.add(areaInfoTem);
                                continue;
                            }else {
                                continue;
                            }
                        }
                        if(flag != null && flag == 0){
                            if(0 == areaInfoTem.getState()){
                                //不在线
                                childrenList.add(areaInfoTem);
                                continue;
                            }else {
                                continue;
                            }
                        }
                    }
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree,map,flag);
            }
        }
    }


    /**
     * 获取当前用户所属的所有巡视点
     *
     * @param cruiseName cruiseName
     * @param robotFlag robotFlag
     * @param userId userId
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDetail> selectCameraCruiseTree(String cruiseName, String robotFlag, Long userId) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);
        if (Objects.nonNull(sysUser) && UserStateEnum.INVALID.getCode() == sysUser.getState()) {
            throw new BusinessException("用户不存在或已删除");
        }

        List<AreaInfoDetail> listTree = new ArrayList<>();
        if (1234L == sysUser.getRoleId() || 1235L == sysUser.getRoleId()) {
            listTree = tCameraScreenDao.selectCameraCruiseTreeWithRobot(cruiseName, robotFlag, userId);
        } else {
            throw  new BusinessException("当前用户角色不可查看");
        }

        List<AreaInfoDetail> treeRootList = getTreeRoot(listTree);
        Map<Long, List<AreaInfoDetail>> upIdMap = getUpIdMap(listTree);
        processAreaInfoTree(treeRootList, upIdMap);
        return treeRootList;
    }

    /**
     * 将List转成map结构，key为上级节点id
     *
     * @param listTree listTree
     * @return result
     */
    private Map<Long, List<AreaInfoDetail>> getUpIdMap(List<AreaInfoDetail> listTree) {
        Map<Long, List<AreaInfoDetail>> map = new HashMap<>();
        listTree.forEach(item -> {
            if (item.getUpId() != null) {
                if (map.containsKey(item.getUpId())) {
                    map.get(item.getUpId()).add(item);
                } else {
                    List<AreaInfoDetail> list = new ArrayList<>();
                    list.add(item);
                    map.put(item.getUpId(), list);
                }
            }
        });

        return map;
    }

    /**
     * 递归构建巡视点列表的树形结构
     *
     * @param treeRootList treeRootList
     * @param upIdMap upIdMap
     */
    private void processAreaInfoTree(List<AreaInfoDetail> treeRootList, Map<Long, List<AreaInfoDetail>> upIdMap) {
        if (CollectionUtils.isEmpty(treeRootList) || upIdMap == null || upIdMap.size() == 0) {
            return;
        }

        List<AreaInfoDetail> childrenList = new ArrayList<>();
        treeRootList.forEach(item -> {
            if (upIdMap.containsKey(item.getId())) {
                List<AreaInfoDetail> list = upIdMap.get(item.getId());
                item.setChildren(list);
                childrenList.addAll(list);
            }
        });

        processAreaInfoTree(childrenList, upIdMap);
    }

    /**
     * 获取根节点
     *
     * @param listTree listTree
     * @return result
     */
    private List<AreaInfoDetail> getTreeRoot(List<AreaInfoDetail> listTree) {
        if (CollectionUtils.isEmpty(listTree)) {
            return null;
        }

        List<AreaInfoDetail> treeRootList = new ArrayList<>();
        for(Iterator<AreaInfoDetail> it = listTree.iterator(); it.hasNext();){
            AreaInfoDetail areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfoDetail areaInfoCountry = new AreaInfoDetail();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountry.setCameraId(areaInfoMap.getCameraId());
                treeRootList.add(areaInfoCountry);
            }
        }

        return treeRootList;
    }

    /**
     * 获取相机状态列表
     *
     * @return result
     */
    private Map<String,String> getCameraStatusMap() {
        Map<String,String> cameraStatusMap = new HashMap<>();
        //获取相机的状态
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if(recordIdMap != null ){
                cameraStatusMap.putAll(recordIdMap);
            }
        }

        return cameraStatusMap;
    }

    /**
     * 处理巡视点是否在线
     *
     * @param areaInfoMap areaInfoMap
     * @param areaInfoTem areaInfoTem
     * @param childrenList childrenList
     * @param flag flag
     * @param map map
     */
    private void processCruisePointStatus(AreaInfoDetail areaInfoMap, AreaInfoDetail areaInfoTem, List<AreaInfoDetail> childrenList, Integer flag, Map<String,String> map) {
        if (!StringUtils.isEmpty(areaInfoTem.getCameraId())) {
            if (map.containsKey(areaInfoTem.getCameraId())) {
                areaInfoTem.setState(Integer.valueOf(map.get(areaInfoTem.getCameraId().toString())));
            } else {
                areaInfoTem.setState(0);
            }
        }

        if(flag != null && flag == 1){
            if(1 == areaInfoTem.getState()){
                //在线
                childrenList.add(areaInfoTem);
            }
        } else if(flag != null && flag == 0){
            if(0 == areaInfoTem.getState()){
                //不在线
                childrenList.add(areaInfoTem);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDetail> selectCameraTreeWithRobot(String cameraName, Integer flag, String robotFlag, Long userId) {
        List<AreaInfoDetail> listTree = new ArrayList<>();
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);

        if (Objects.nonNull(sysUser) && UserStateEnum.INVALID.getCode() == sysUser.getState()) {
            throw new BusinessException("用户不存在或已删除");
        }
        else {
            if (1234L == sysUser.getRoleId()) {
                listTree = tCameraScreenDao.selectCameraTreeWithRobot(cameraName, robotFlag, null);
            }
            else if(1235L == sysUser.getRoleId()) {
                listTree = tCameraScreenDao.selectCameraTreeWithRobot(cameraName, robotFlag, userId);
            }
            else {
                throw  new BusinessException("当前用户角色不可查看");
            }
        }
        List<AreaInfoDetail> areaInfoCountryList = new ArrayList<>();
        for(Iterator<AreaInfoDetail> it = listTree.iterator(); it.hasNext();){
            AreaInfoDetail areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfoDetail areaInfoCountry = new AreaInfoDetail();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountry.setCameraId(areaInfoMap.getCameraId());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        Map<String,String> map = new HashMap<>();
        //获取相机的状态
        List<Map<String,String>> listForState = new ArrayList<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if(recordIdMap == null){
                continue;
            }
            map.putAll(recordIdMap);
        }
//        for(Map<String,String> item:listForState){
//            map.putAll(item);
//        }
        diGuiWithRobot(areaInfoCountryList, listTree,map,flag);
        return areaInfoCountryList;
    }

    public List<AreaInfoDetail> selectCameraTreeWithRobotNew(String cameraName, Integer flag, String robotFlag, Long userId, String level,
        Long id, Integer cameraType) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);

        if (Objects.nonNull(sysUser) && UserStateEnum.INVALID.getCode() == sysUser.getState()) {
            throw new BusinessException("用户不存在或已删除");
        } else {
            if (1234L == sysUser.getRoleId()) {
                userId = null;
            } else if (1235L == sysUser.getRoleId()) {

            } else {
                throw new BusinessException("当前用户角色不可查看");
            }
        }
        List<AreaInfoDetail> areaInfoDetails = new ArrayList<>();
        //获取相机的状态
        Map<String, String> map = new HashMap<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for (Long recordId : recordIdList) {
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if (recordIdMap == null) {
                continue;
            }
            map.putAll(recordIdMap);
        }
        switch (level) {
            case "5":
                return areaTree(cameraName, flag, robotFlag, userId, map);
            case "6":
                return cameraTree(cameraName, flag, robotFlag, userId, id, map, cameraType);
            case "66":
                return new ArrayList<>();
            default:
                throw new BusinessException("参数错误！");
        }
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

    private List<AreaInfoDetail> areaTree(String cameraName, Integer flag, String robotFlag, Long userId,Map<String,String> map){
        List<AreaInfoDetail> areaTree = tCameraInfoDao.selectCameraTreeRegion();
        areaTree =  assembleTrees(areaTree);
        areaTree =  areaAddDeviceTree(areaTree,cameraName,flag,robotFlag,userId,map);
        return areaTree;
    }

    private List<AreaInfoDetail> areaAddDeviceTree(List<AreaInfoDetail> areaTree, String cameraName, Integer flag, String robotFlag, Long userId,Map<String,String> map){
        if (areaTree == null){
            return null;
        }
        areaTree.forEach(area ->{
            if ("region".equals(area.getInfoType())){
                if (area.getChildren() != null && area.getChildren().size() > 0){
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
        child.forEach(childs -> {
            if ("camera".equals(childs.getInfoType())) {
                if (map.get(childs.getId().toString()) != null) {
                    childs.setState(Integer.valueOf(map.get(childs.getId().toString())));
                } else {
                    childs.setState(0);
                }
                if (flag != null) {
                    if (flag.equals(childs.getState())) {
                        childrenList.add(childs);
                    } else if (flag == 2) {
                        childrenList.add(childs);
                    }
                }
            }
            if ("robot".equals(childs.getInfoType())) {
                //机器人
                TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(childs.getId());
                List<AreaInfoDetail> robotCameraList = new ArrayList<>();
                //可见光
                AreaInfoDetail lightCamera = new AreaInfoDetail();
                String light = String.valueOf(tRobotInfo.getRobotId()) + "9901";
                lightCamera.setId(Long.parseLong(light));
                lightCamera.setLabel("机器人可见光");
                lightCamera.setInfoType("robotCamera");
                lightCamera.setUpId(childs.getId());
                lightCamera.setUpName(tRobotInfo.getRobotCode());
                if ("在线".equals(tRobotInfo.getRobotStatus())) {
                    childs.setState(1);
                    lightCamera.setState(1);
                } else {
                    childs.setState(0);
                    lightCamera.setState(0);
                }

                if (flag != null) {
                    if (flag == lightCamera.getState() && (flag == 1 || flag == 0)) {
                        robotCameraList.add(lightCamera);
                    } else {
                        robotCameraList.add(lightCamera);
                    }
                }
                //红外
                AreaInfoDetail redCamera = new AreaInfoDetail();
                String infrared = String.valueOf(tRobotInfo.getRobotId()) + "9902";
                redCamera.setId(Long.parseLong(infrared));
                redCamera.setLabel("机器人红外");
                redCamera.setInfoType("robotCamera");
                redCamera.setUpId(childs.getId());
                redCamera.setUpName(tRobotInfo.getRobotCode());
                if ("在线".equals(tRobotInfo.getRobotStatus())) {
                    childs.setState(1);
                    redCamera.setState(1);
                } else {
                    childs.setState(0);
                    redCamera.setState(0);
                }

                if (flag != null) {
                    if (flag == redCamera.getState() && (flag == 1 || flag == 0)) {
                        robotCameraList.add(redCamera);
                    } else {
                        robotCameraList.add(redCamera);
                    }
                }
                childs.setChildren(robotCameraList);
                if (flag != null) {
                    if ((flag == 1 || flag == 0)) {
                        if (flag == childs.getState()) {
                            childrenList.add(childs);
                        }
                    } else {
                        childrenList.add(childs);
                    }
                }
            }
        });
        return childrenList;
    }

    public List<AreaInfoDetail> selectCameraTreeWithRobotByName(String cameraName, Integer flag, String robotFlag, Long userId, Integer cameraType){
        if (StringUtils.isEmpty(cameraName)){
            List<AreaInfoDetail> areaTree = tCameraInfoDao.selectCameraTreeRegion();
            return assembleTrees(areaTree);
        }

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

        List<AreaInfoDetail> areaInfoDetails = new ArrayList<>();
        //获取相机的状态:  map包含在离线的  不包含未知状态的
        Map<String,String> map = new HashMap<>(8);
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if(recordIdMap == null){
                continue;
            }
            map.putAll(recordIdMap);
        }

        List<TCameraInfo> cameraList = tCameraInfoDao.selectCameraByName(cameraName,robotFlag,userId, cameraType);
        if (!CollectionUtils.isEmpty(cameraList)){
            List<Long> regionList = cameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toList());

            List<TCameraInfo> finalCameraList = new ArrayList<>();
            // flag:1-在线 0-离线 2-全部
            if (flag == 2){
                finalCameraList = cameraList;
            } else if (flag == 1) {
                finalCameraList = cameraList.stream().filter(tCameraInfo -> StringUtils.equals(map.get(tCameraInfo.getCameraId().toString()), "1")).collect(Collectors.toList());
            } else if (flag == 0) {
                finalCameraList = cameraList.stream().filter(tCameraInfo -> !StringUtils.equals(map.get(tCameraInfo.getCameraId().toString()), "1")).collect(Collectors.toList());
            }

            // MySQL 8.0
//            if (!CollectionUtils.isEmpty(finalCameraList)) {
//                regionList.addAll(tCameraInfoDao.selectRegionByCameraList(finalCameraList));
//            }

            if (!CollectionUtils.isEmpty(finalCameraList)) {
                Set<Long> upRegionList = finalCameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toSet());
                Set<Long> upRegionResult = new HashSet<>(upRegionList);
                do {
                    upRegionList.addAll(upRegionResult);
                    upRegionResult.addAll(tCameraInfoDao.selectRegionListByUpRegionId(upRegionList));
                } while (!upRegionList.containsAll(upRegionResult));
                regionList.addAll(upRegionResult);
            }

            if (!CollectionUtils.isEmpty(regionList)){
                areaInfoDetails = tCameraInfoDao.selectCameraTreeByName(finalCameraList,regionList);
            }
        }
        areaInfoDetails.forEach(treeNode ->{
            if("camera".equals(treeNode.getInfoType())){
                if(map.get(treeNode.getId().toString()) != null){
                    treeNode.setState(Integer.valueOf(map.get(treeNode.getId().toString())));
                }else {
                    treeNode.setState(0);
                }
            }
        });
        return assembleTrees(areaInfoDetails);
    }

    private void diGuiWithRobot(List<AreaInfoDetail> areaInfoList, List<AreaInfoDetail> listTree,Map<String,String> map,Integer flag) {
        for(AreaInfoDetail areaInfo : areaInfoList){
            List<AreaInfoDetail> childrenList = new ArrayList<>();
            for(Iterator<AreaInfoDetail> it = listTree.iterator();it.hasNext();){
                AreaInfoDetail areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    AreaInfoDetail areaInfoTem = new AreaInfoDetail();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    areaInfoTem.setCameraId(areaInfoMap.getCameraId());
                    areaInfoTem.setDeviceType(areaInfoMap.getDeviceType());
                    if("camera".equals(areaInfoMap.getInfoType())){
                        if(map.get(areaInfoMap.getId().toString()) != null){
                            areaInfoTem.setState(Integer.valueOf(map.get(areaInfoMap.getId().toString())));
                        }else {
                            areaInfoTem.setState(0);
                        }
                        if(flag != null && flag == 1){
                            if(1 == areaInfoTem.getState()){
                                //在线
                                childrenList.add(areaInfoTem);
                                continue;
                            }else {
                                continue;
                            }
                        }
                        if(flag != null && flag == 0){
                            if(0 == areaInfoTem.getState()){
                                //不在线
                                childrenList.add(areaInfoTem);
                                continue;
                            }else {
                                continue;
                            }
                        }
                    }
                    if("robot".equals(areaInfoMap.getInfoType())){
                        //机器人
                        TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(areaInfoMap.getId());
                        List<AreaInfoDetail> robotCameraList = new ArrayList<>();
                        //可见光
                        AreaInfoDetail lightCamera = new AreaInfoDetail();
                        String light = String.valueOf(tRobotInfo.getRobotId()) + "9901";
                        lightCamera.setId(Long.parseLong(light));
                        lightCamera.setLabel("机器人可见光");
                        lightCamera.setInfoType("robotCamera");
                        lightCamera.setUpId(areaInfoMap.getId());
                        lightCamera.setUpName(tRobotInfo.getRobotCode());
                        if("在线".equals(tRobotInfo.getRobotStatus())){
                            areaInfoTem.setState(1);
                            lightCamera.setState(1);
                        }else {
                            areaInfoTem.setState(0);
                            lightCamera.setState(0);
                        }

                        if(flag != null){
                            if(flag == lightCamera.getState() && (flag == 1 || flag == 0)){
                                robotCameraList.add(lightCamera);
                            }else {
                                robotCameraList.add(lightCamera);
                            }
                        }
                        //红外
                        AreaInfoDetail redCamera = new AreaInfoDetail();
                        String infrared = String.valueOf(tRobotInfo.getRobotId()) + "9902";
                        redCamera.setId(Long.parseLong(infrared));
                        redCamera.setLabel("机器人红外");
                        redCamera.setInfoType("robotCamera");
                        redCamera.setUpId(areaInfoMap.getId());
                        redCamera.setUpName(tRobotInfo.getRobotCode());
                        if("在线".equals(tRobotInfo.getRobotStatus())){
                            areaInfoTem.setState(1);
                            redCamera.setState(1);
                        }else {
                            areaInfoTem.setState(0);
                            redCamera.setState(0);
                        }

                        if(flag != null){
                            if(flag == redCamera.getState()&& (flag == 1 || flag == 0)){
                                robotCameraList.add(redCamera);
                            }else {
                                robotCameraList.add(redCamera);
                            }
                        }
                        areaInfoTem.setChildren(robotCameraList);
                        if(flag != null){
                            if((flag == 1 || flag == 0)){
                                if(flag == areaInfoTem.getState()){
                                    childrenList.add(areaInfoTem);
                                }
                                continue;
                            }else {
                                childrenList.add(areaInfoTem);
                                continue;
                            }
                        }
                    }
//                    if ("cruisePoint".equals(areaInfoMap.getInfoType())) {
//                        processCruisePointStatus(areaInfoMap, areaInfoTem, childrenList, flag, map);
//                    }

                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGuiWithRobot(childrenList, listTree,map,flag);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDetail> infraredCameraStateTree(String cameraName,Integer flag) {
        List<AreaInfoDetail> listTree = new ArrayList<>();
        listTree = tCameraScreenDao.infraredCameraStateTree(cameraName);
        List<AreaInfoDetail> areaInfoCountryList = new ArrayList<>();
        for(Iterator<AreaInfoDetail> it = listTree.iterator(); it.hasNext();){
            AreaInfoDetail areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfoDetail areaInfoCountry = new AreaInfoDetail();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        Map<String,String> map = new HashMap<>();
        //获取相机的状态
        List<Map<String,String>> listForState = new ArrayList<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if(recordIdMap == null){
                continue;
            }
            map.putAll(recordIdMap);
        }
//        for(Map<String,String> item:listForState){
//            map.putAll(item);
//        }
        diGui(areaInfoCountryList, listTree,map,flag);
        return areaInfoCountryList;
    }

    public List<AreaInfoOfMonitorDevice> selectRegionMonitorDeviceTree() {
        List<AreaInfoOfMonitorDevice> originList = tCameraScreenDao.selectRegionMonitorDevice();
        List<AreaInfoOfMonitorDevice> loopList = new ArrayList<>();
        for (AreaInfoOfMonitorDevice monitorDevice:originList) {
            if (Objects.nonNull(monitorDevice.getUpId()) && monitorDevice.getUpId() == -1) {
                AreaInfoOfMonitorDevice areaInfoCountry = new AreaInfoOfMonitorDevice();
                areaInfoCountry.setId(monitorDevice.getId());
                areaInfoCountry.setLabel(monitorDevice.getLabel());
                areaInfoCountry.setInfoType(monitorDevice.getInfoType());
                loopList.add(areaInfoCountry);
            }
        }
          recursionTree(loopList, originList);
        return loopList;
    }

    private void recursionTree(List<AreaInfoOfMonitorDevice> loopList, List<AreaInfoOfMonitorDevice> originList) {
        for (AreaInfoOfMonitorDevice originItem : loopList) {
            List<AreaInfoOfMonitorDevice> childList = new ArrayList<>();
            for (AreaInfoOfMonitorDevice monitorDevice:originList) {
                if (Objects.equals(originItem.getId(), monitorDevice.getUpId())) {
                    AreaInfoOfMonitorDevice areaInfoTem = new AreaInfoOfMonitorDevice();
                    areaInfoTem.setId(monitorDevice.getId());
                    areaInfoTem.setUpId(monitorDevice.getUpId());
                    areaInfoTem.setLabel(monitorDevice.getLabel());
                    areaInfoTem.setInfoType(monitorDevice.getInfoType());
                    areaInfoTem.setUpName(monitorDevice.getUpName());

                    childList.add(areaInfoTem);
                }
            }
            if (childList.size()>0 ) {
                originItem.setChildren(childList);
                recursionTree(childList,originList);
            }

        }

    }
}

