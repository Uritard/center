package com.yjh.platform.module.user.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.dao.TCameraGroupDao;

import java.util.*;

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

    @Logs(title = "插入", code = "module",content = "根据参数新增一个分组信息")
    @Transactional(rollbackFor = Exception.class)
    public int add(TCameraGroup tCameraGroup) {
        if(tCameraGroup.getGroupId() == null){
            return this.tCameraGroupDao.add(tCameraGroup);
        }
        return this.update(tCameraGroup);
    }

    @Logs(title = "删除", code = "module",content = "删除分组信息")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long groupId) {
        return this.tCameraGroupDao.deleteByPrimaryId(groupId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraGroup tCameraGroup) {
        return this.tCameraGroupDao.update(tCameraGroup);
    }


    @Logs(title = "主键查询", code = "module",content = "根据web传递的参数查询分组信息")
    @Transactional(rollbackFor = Exception.class)
    public TCameraGroupDetail selectByPrimaryId(Long groupId) {
        TCameraGroup tCameraGroup = this.tCameraGroupDao.selectByPrimaryId(groupId);
        List<Camera> list = new ArrayList<>();
        if(tCameraGroup.getCameraIds() != null) {
            String[] cameraList = tCameraGroup.getCameraIds().split(",");
            for (String item : cameraList) {
                TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(Long.valueOf(item));
                Camera camera = new Camera();
                camera.setCameraId(item);
                camera.setCameraName(tCameraInfo.getCameraName());
                list.add(camera);
            }
        }
        TCameraGroupDetail tCameraGroupDetail =new TCameraGroupDetail();
        tCameraGroupDetail.setGroupId(tCameraGroup.getGroupId());
        tCameraGroupDetail.setGroupName(tCameraGroup.getGroupName());
        tCameraGroupDetail.setCameraIds(tCameraGroup.getCameraIds());
        tCameraGroupDetail.setRemarks(tCameraGroup.getRemarks());
        tCameraGroupDetail.setCameraList(list);
        return tCameraGroupDetail;
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraGroup> select(Long groupId, String groupName, String cameraIds, String remarks) {
        List<TCameraGroup> tCameraGroupList = tCameraGroupDao.select(groupId, groupName, cameraIds, remarks);
        return tCameraGroupList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraGroup> selectByPage(TCameraGroup tCameraGroup) {
        List<TCameraGroup> tCameraGroupList = tCameraGroupDao.selectByPage(tCameraGroup);
        return tCameraGroupList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCameraGroup> list) {
        return this.tCameraGroupDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String groupId) {
    List<String> list1= Arrays.asList(groupId.split(","));
    return this.tCameraGroupDao.batchDelete(list1);
    }

    @Logs(title = "查询摄像机分组树", code = "module",content = "查询摄像机分组树")
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


    @Logs(title = "查询所有的相机", code = "module",content = "根据参数查询分组内的摄像头信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraGroupDetail> selectAllCamera() {
        List<TCameraGroup> all = tCameraGroupDao.selectAll();
        List<TCameraGroupDetail> re = new ArrayList<>();
        for (TCameraGroup item: all) {
            TCameraGroupDetail tCameraGroupDetail = this.selectByPrimaryId(item.getGroupId());
            re.add(tCameraGroupDetail);
        }
        return re;
    }

    @Logs(title = "摄像机状态树", code = "module")
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

