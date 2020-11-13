package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.entity.Camera;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraScreen;
import com.yjh.platform.module.user.dao.TCameraScreenDao;

import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import java.util.Arrays;

import com.yjh.platform.module.user.entity.TCameraScreenDetail;
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



}

