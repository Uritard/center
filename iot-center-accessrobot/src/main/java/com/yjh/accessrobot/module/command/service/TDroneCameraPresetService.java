package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TDroneCameraPresetDao;
import com.yjh.accessrobot.module.command.entity.AreaInfoDetail;
import com.yjh.accessrobot.module.command.entity.TDroneCameraPreset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
* @author lqh
* @since 2021-03-15
*/
@Service
public class TDroneCameraPresetService{

    @Autowired
    private TDroneCameraPresetDao tDroneCameraPresetDao;

    //@Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TDroneCameraPreset tDroneCameraPreset) {
        TDroneCameraPreset tDroneCameraPreset1 = tDroneCameraPresetDao.selectByPresetNum(tDroneCameraPreset.getPresetNum());
        if(tDroneCameraPreset1 == null){
            return this.tDroneCameraPresetDao.add(tDroneCameraPreset);
        }else {
            return -1;
        }

    }

    //@Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long presetId) {
        return this.tDroneCameraPresetDao.deleteByPrimaryId(presetId);
    }

    //@Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TDroneCameraPreset tDroneCameraPreset) {
        return this.tDroneCameraPresetDao.update(tDroneCameraPreset);
    }

    //@Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TDroneCameraPreset selectByPrimaryId(Long presetId) {
        return this.tDroneCameraPresetDao.selectByPrimaryId(presetId);
    }

    //@Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDroneCameraPreset> select(Long presetId,Integer presetNum, Long droneId, String droneCode, String presetName, Date creatorTime, Integer cameraType) {
        List<TDroneCameraPreset> tDroneCameraPresetList = tDroneCameraPresetDao.select(presetId, presetNum, droneId, droneCode, presetName, creatorTime, cameraType);
        return tDroneCameraPresetList;
    }

    //@Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDroneCameraPreset> selectByPage(Integer presetNum, String presetName,Long droneId) {
        List<TDroneCameraPreset> tDroneCameraPresetList = tDroneCameraPresetDao.selectByPage( presetNum,presetName,droneId);
        return tDroneCameraPresetList;
    }

    //@Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TDroneCameraPreset> list) {
        return this.tDroneCameraPresetDao.batchAdd(list);
    }

    //@Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String presetId) {
    List<String> list1= Arrays.asList(presetId.split(","));
    return this.tDroneCameraPresetDao.batchDelete(list1);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDetail> dronePresetTree(Long droneId){
        List<AreaInfoDetail> re =new ArrayList<>();
        AreaInfoDetail tree = new AreaInfoDetail();
        tree.setId(1L);
        tree.setLabel("无人机预置位树");
        tree.setInfoType("tree");
        List<AreaInfoDetail> child = new ArrayList<>();
        List<TDroneCameraPreset> list = selectByPage(null,null,droneId);
        for(TDroneCameraPreset item:list){
            AreaInfoDetail pre = new AreaInfoDetail();
            pre.setId(Long.valueOf(item.getPresetNum()));
            pre.setLabel(item.getPresetName());
            pre.setUpId(1L);
            pre.setUpName("无人机预置位树");
            pre.setInfoType("preset");
            child.add(pre);
        }
        tree.setChildren(child);
        re.add(tree);
        return re;
    }

}

