package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TCameraScreen;
import com.yjh.platform.module.user.dao.TCameraScreenDao;

import java.util.List;
import java.util.Date;
import java.util.Arrays;

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
    public int update(TCameraScreen tCameraScreen) {
        TCameraScreen ifSave =this.tCameraScreenDao.selectByPrimaryId(tCameraScreen.getUserId());
        if(ifSave != null){
            return this.tCameraScreenDao.update(tCameraScreen);
        }else {
            return this.tCameraScreenDao.add(tCameraScreen);
        }
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCameraScreen selectByPrimaryId(Long userId) {
        return this.tCameraScreenDao.selectByPrimaryId(userId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraScreen> select(Long userId, Integer screenNum, String cameraIds, Date createTime) {
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

