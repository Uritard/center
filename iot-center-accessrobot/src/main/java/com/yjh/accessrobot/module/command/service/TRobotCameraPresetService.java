package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.entity.TRobotCameraPreset;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yjh.accessrobot.module.command.dao.TRobotCameraPresetDao;
import com.yjh.accessrobot.module.command.service.TRobotCameraPresetService;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2021-03-15
*/
@Service
public class TRobotCameraPresetService{

    @Autowired
    private TRobotCameraPresetDao tRobotCameraPresetDao;

    //@Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TRobotCameraPreset tRobotCameraPreset) {
        TRobotCameraPreset tRobotCameraPreset1 = tRobotCameraPresetDao.selectByPresetNum(tRobotCameraPreset.getPresetNum());
        if(tRobotCameraPreset1 == null){
            return this.tRobotCameraPresetDao.add(tRobotCameraPreset);
        }else {
            return -1;
        }

    }

    //@Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long presetId) {
        return this.tRobotCameraPresetDao.deleteByPrimaryId(presetId);
    }

    //@Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotCameraPreset tRobotCameraPreset) {
        return this.tRobotCameraPresetDao.update(tRobotCameraPreset);
    }

    //@Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TRobotCameraPreset selectByPrimaryId(Long presetId) {
        return this.tRobotCameraPresetDao.selectByPrimaryId(presetId);
    }

    //@Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotCameraPreset> select(Long presetId,Integer presetNum, Long robotId, String robotCode, String presetName, Date creatorTime, String remark) {
        List<TRobotCameraPreset> tRobotCameraPresetList = tRobotCameraPresetDao.select(presetId, presetNum, robotId, robotCode, presetName, creatorTime, remark);
        return tRobotCameraPresetList;
    }

    //@Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotCameraPreset> selectByPage(Integer presetNum, String presetName) {
        List<TRobotCameraPreset> tRobotCameraPresetList = tRobotCameraPresetDao.selectByPage( presetNum,presetName);
        return tRobotCameraPresetList;
    }

    //@Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TRobotCameraPreset> list) {
        return this.tRobotCameraPresetDao.batchAdd(list);
    }

    //@Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String presetId) {
    List<String> list1= Arrays.asList(presetId.split(","));
    return this.tRobotCameraPresetDao.batchDelete(list1);
    }



}

