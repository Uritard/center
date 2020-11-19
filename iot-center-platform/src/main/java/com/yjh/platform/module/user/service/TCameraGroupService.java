package com.yjh.platform.module.user.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.entity.Camera;
import com.yjh.platform.module.user.entity.TCameraGroup;
import com.yjh.platform.module.user.dao.TCameraGroupDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.yjh.platform.module.user.entity.TCameraGroupDetail;
import com.yjh.platform.module.user.entity.TCameraInfo;
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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TCameraGroup tCameraGroup) {
        if(tCameraGroup.getGroupId() == null){
            return this.tCameraGroupDao.add(tCameraGroup);
        }
        return this.update(tCameraGroup);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long groupId) {
        return this.tCameraGroupDao.deleteByPrimaryId(groupId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraGroup tCameraGroup) {
        return this.tCameraGroupDao.update(tCameraGroup);
    }


    @Logs(title = "主键查询", code = "module")
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


    @Logs(title = "查询所有的相机", code = "module")
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


}

