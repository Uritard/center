package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.dao.TCameraScreenDao;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TCameraScreen tCameraScreen) {
        return this.tCameraScreenDao.add(tCameraScreen);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long userId) {
        return this.tCameraScreenDao.deleteByPrimaryId(userId);
    }

    @Logs(title = "更新", code = "module")
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

    @Logs(title = "主键查询", code = "module")
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
                camera.setCameraId(tCameraInfo.getCameraId().toString());
                camera.setCameraName(tCameraInfo.getCameraName());
                cameraList.add(camera);
            }
        }
        tCameraScreenDetail.setCameraList(cameraList);
        return tCameraScreenDetail;
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraScreen> select(Long userId, String screenNum, String cameraIds, Date createTime) {
        List<TCameraScreen> tCameraScreenList = tCameraScreenDao.select(userId, screenNum, cameraIds, createTime);
        return tCameraScreenList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraScreen> selectByPage(TCameraScreen tCameraScreen) {
        List<TCameraScreen> tCameraScreenList = tCameraScreenDao.selectByPage(tCameraScreen);
        return tCameraScreenList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCameraScreen> list) {
        return this.tCameraScreenDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String userId) {
    List<String> list1= Arrays.asList(userId.split(","));
    return this.tCameraScreenDao.batchDelete(list1);
    }

    @Logs(title = "摄像机状态树", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDetail> cameraStateTree() {
        List<AreaInfoDetail> listTree = new ArrayList<>();
        listTree = tCameraScreenDao.selectCameraTreeDevice();
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
            //todo 摄像机状态
        }
        for(Map<String,String> item:listForState){
            map.putAll(item);
        }
        diGui(areaInfoCountryList, listTree,map);
        return areaInfoCountryList;
    }
    private void diGui(List<AreaInfoDetail> areaInfoList, List<AreaInfoDetail> listTree,Map<String,String> map) {
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
                        areaInfoTem.setState(Integer.valueOf(map.get(areaInfoMap.getId().toString())));
                    }
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree,map);
            }
        }
    }



}

