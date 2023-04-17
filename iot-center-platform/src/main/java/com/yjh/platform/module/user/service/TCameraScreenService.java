package com.yjh.platform.module.user.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.dao.TCameraScreenDao;

import java.util.*;

import com.yjh.platform.module.user.entity.enums.UserStateEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private SysUserDao sysUserDao;

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
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if(re == null){
                continue;
            }
            map.putAll((Map<String,String>)re.getData());
        }
//        for(Map<String,String> item:listForState){
//            map.putAll(item);
//        }
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
    private static Result cameraStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class,map);
            }
        } catch (Exception e) {

        }
        return re;
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
        Map<String,String> cameraStatusMap = getCameraStatusMap();
        diGuiWithRobot(treeRootList, listTree,cameraStatusMap,1);
        return treeRootList;
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
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if(re != null && re.getData() != null){
                cameraStatusMap.putAll((Map<String,String>)re.getData());
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
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if(re == null){
                continue;
            }
            map.putAll((Map<String,String>)re.getData());
        }
//        for(Map<String,String> item:listForState){
//            map.putAll(item);
//        }
        diGuiWithRobot(areaInfoCountryList, listTree,map,flag);
        return areaInfoCountryList;
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
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if(re == null){
                continue;
            }
            map.putAll((Map<String,String>)re.getData());
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

