package com.yjh.platform.module.user.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.dao.TCameraGroupDao;

import java.util.*;

import com.yjh.platform.module.video.controller.CameraConController;
import com.yjh.platform.module.video.service.CameraConService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-11-17
*/
@Service
public class TCameraGroupService{

    @Autowired
    private TCameraGroupDao tCameraGroupDao;
    @Autowired
    private TCameraInfoDao tCameraInfoDao;
    @Autowired
    private TCameraScreenDao tCameraScreenDao;
    @Autowired
    private CameraConService cameraConService;

    @Transactional(rollbackFor = Exception.class)
    public int add(TCameraGroup tCameraGroup) {
        if(tCameraGroup.getGroupId() == null){
            return this.tCameraGroupDao.add(tCameraGroup);
        }
        return this.update(tCameraGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long groupId) {
        return this.tCameraGroupDao.deleteByPrimaryId(groupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraGroup tCameraGroup) {
        return this.tCameraGroupDao.update(tCameraGroup);
    }


    @Transactional(rollbackFor = Exception.class)
    public TCameraGroupDetail selectByPrimaryId(Long groupId,Integer state) {
        TCameraGroup tCameraGroup = this.tCameraGroupDao.selectByPrimaryId(groupId);
        List<Camera> list = new ArrayList<>();
        boolean flag = false;
        //获取状态
        Map<String,String> map = new HashMap<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if(recordIdMap == null){
                continue;
            }
            map.putAll(recordIdMap);
        }
        if(tCameraGroup.getCameraIds() != null && !"".equals(tCameraGroup.getCameraIds())) {
            String[] cameraList = tCameraGroup.getCameraIds().split(",");
            for (String item : cameraList) {
                TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(Long.valueOf(item));
                if(tCameraInfo == null){
                    tCameraGroup.setCameraIds(tCameraGroup.getCameraIds().replace(item+",","").replace(","+item,"").replace(item,""));
                    flag = true;
                    continue;
                }
                Camera camera = new Camera();
                camera.setCameraId(item);
                camera.setCameraName(tCameraInfo.getCameraName());
                if(state == 1){
                    //只查在线的
                    if("1".equals(map.get(item.toString()))){
                        list.add(camera);
                    }
                }else {
                    //全查
                    list.add(camera);
                }

            }
        }
        TCameraGroupDetail tCameraGroupDetail =new TCameraGroupDetail();
        tCameraGroupDetail.setGroupId(tCameraGroup.getGroupId());
        tCameraGroupDetail.setGroupName(tCameraGroup.getGroupName());
        tCameraGroupDetail.setCameraIds(tCameraGroup.getCameraIds());
        tCameraGroupDetail.setRemarks(tCameraGroup.getRemarks());
        tCameraGroupDetail.setCameraList(list);
        if(flag){
            tCameraGroupDao.update(tCameraGroup);
        }
        return tCameraGroupDetail;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraGroup> select(Long groupId, String groupName, String cameraIds, String remarks) {
        List<TCameraGroup> tCameraGroupList = tCameraGroupDao.select(groupId, groupName, cameraIds, remarks);
        return tCameraGroupList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraGroup> selectByPage(TCameraGroup tCameraGroup) {
        List<TCameraGroup> tCameraGroupList = tCameraGroupDao.selectByPage(tCameraGroup);
        return tCameraGroupList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCameraGroup> list) {
        return this.tCameraGroupDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String groupId) {
    List<String> list1= Arrays.asList(groupId.split(","));
    return this.tCameraGroupDao.batchDelete(list1);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> groupTree(){
        List<AreaInfo> group = new ArrayList<>();
        AreaInfo up = new AreaInfo();
        up.setInfoType("groupTree");
        up.setId(1L);
        up.setLabel("摄像机分组树");
        List<TCameraGroup> list = tCameraGroupDao.selectAll();
        List<AreaInfo> child = new ArrayList<>();
        for (TCameraGroup item: list) {
            AreaInfo areaInfo = new AreaInfo();
            areaInfo.setUpId(up.getId());
            areaInfo.setUpName(up.getLabel());
            areaInfo.setId(item.getGroupId());
            areaInfo.setLabel(item.getGroupName());
            areaInfo.setInfoType("group");
            child.add(areaInfo);
        }
        up.setChildren(child);
        group.add(up);
        return group;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TCameraGroupDetail> selectAllCamera() {
        List<TCameraGroup> all = tCameraGroupDao.selectAll();
        List<TCameraGroupDetail> re = new ArrayList<>();
        for (TCameraGroup item: all) {
            TCameraGroupDetail tCameraGroupDetail = this.selectByPrimaryId(item.getGroupId(),0);
            re.add(tCameraGroupDetail);
        }
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> cameraTree() {
        List<AreaInfo> listTree = new ArrayList<>();
        listTree =tCameraInfoDao.selectCameraTreeDevice();
        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        for(Iterator<AreaInfo> it = listTree.iterator(); it.hasNext();){
            AreaInfo areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfo areaInfoCountry = new AreaInfo();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        Map<String,Object> map = new HashMap<>();
        diGui(areaInfoCountryList, listTree);
        deleteNull(areaInfoCountryList);
        return areaInfoCountryList;
    }
    private void diGui(List<AreaInfo> areaInfoList, List<AreaInfo> listTree) {
        for(AreaInfo areaInfo : areaInfoList){
            List<AreaInfo> childrenList = new ArrayList<>();
            for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
                AreaInfo areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    AreaInfo areaInfoTem = new AreaInfo();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    //todo 摄像机状态
                    //areaInfoTem.setState()
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree);
            }
        }
    }
    private void deleteNull(List<AreaInfo> areaInfoCountryList){
        if(areaInfoCountryList == null){
            return ;
        }
        for(AreaInfo item:areaInfoCountryList){
           checkList(areaInfoCountryList);
            deleteNull(item.getChildren());
        }

    }
    private AreaInfo checkList(List<AreaInfo> areaInfoCountryList){
        if(areaInfoCountryList == null){
            return null;
        }
        for (AreaInfo item:areaInfoCountryList) {
            if("region".equals(item.getInfoType())){
                if(item.getChildren() == null){
                    areaInfoCountryList.remove(item);
                }
            }
        }
        return null;
    }

}

