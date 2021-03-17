package com.yjh.platform.module.user.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.dao.TCameraScreenDao;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
            if(item.equals("")){
                Camera camera = new Camera();
               camera.setCameraId("");
               camera.setCameraName("");
                cameraList.add(camera);
            }else {
                Camera camera = new Camera();
                TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(Long.parseLong(item));
                if(tCameraInfo == null){
                    tCameraScreenDetail.setCameraIds(tCameraScreenDetail.getCameraIds().replace(","+item,",").replace(item+",",",").replace(item,","));
                    camera.setCameraId("");
                    camera.setCameraName("");
                    cameraList.add(camera);
                    this.update(tCameraScreenDetail,userId.toString());
                    continue;
                }
                camera.setCameraId(tCameraInfo.getCameraId().toString());
                camera.setCameraName(tCameraInfo.getCameraName());
                cameraList.add(camera);
            }
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


    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDetail> selectCameraTreeWithRobot(String cameraName,Integer flag) {
        List<AreaInfoDetail> listTree = new ArrayList<>();
        listTree = tCameraScreenDao.selectCameraTreeWithRobot(cameraName);
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
                        lightCamera.setId(1L);
                        lightCamera.setLabel("机器人可见光");
                        lightCamera.setInfoType("robotCamera");
                        lightCamera.setUpId(areaInfoMap.getId());
                        lightCamera.setUpName(tRobotInfo.getRobotCode());
                        if("在线".equals(tRobotInfo.getRobotStatus())){
                            lightCamera.setState(1);
                        }else {
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
                        redCamera.setId(2L);
                        redCamera.setLabel("机器人红外");
                        redCamera.setInfoType("robotCamera");
                        redCamera.setUpId(areaInfoMap.getId());
                        redCamera.setUpName(tRobotInfo.getRobotCode());
                        if("在线".equals(tRobotInfo.getRobotStatus())){
                            redCamera.setState(1);
                        }else {
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
                    }
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGuiWithRobot(childrenList, listTree,map,flag);
            }
        }
    }

}

