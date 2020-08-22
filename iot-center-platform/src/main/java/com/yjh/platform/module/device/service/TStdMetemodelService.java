package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.TStdMeteModel;
import com.yjh.platform.module.device.dao.TStdMetemodelDao;

import java.util.List;

import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-07
*/
@Service
public class TStdMetemodelService{

    @Autowired
    private TStdMetemodelDao tStdMetemodelDao;

    @Autowired
    private TStdMetemodelDetailDao tStdMetemodelDetailDao;

    @Autowired
    private TStdDeviceDao tStdDeviceDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.add(tStdMeteModel);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.deleteByPrimaryId(modelId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.update(tStdMeteModel);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdMeteModel selectByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.selectByPrimaryId(modelId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> select(Long modelId, String modelName, Integer deviceType, String remark) { return tStdMetemodelDao.select(modelId, modelName, deviceType, remark); }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> selectByPage(TStdMeteModel tStdMeteModel) { return tStdMetemodelDao.selectByPage(tStdMeteModel); }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdMeteModel> list) {
        return this.tStdMetemodelDao.batchAdd(list);
    }

    @Logs(title = "根据模版的设备类型查询对应的初始测点信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectMeteByDeviceType(Integer deviceType) { return tStdMetemodelDao.selectMeteByDeviceType(deviceType); }

    @Logs(title = "批量插入模版测点", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAddModelMete(List<TStdMeteModelDetail> list) {
        Long modelId = list.get(0).getModelId();
        List<String> deviceList = tStdDeviceDao.selectByModelId(modelId);
        if (deviceList.size()>0) {return 206;} else {
            tStdMetemodelDetailDao.deleteByPrimaryId(modelId);
            return this.tStdMetemodelDetailDao.batchAdd(list);
        }
    }

    @Logs(title = "批量删除",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<String> list){
        return this.tStdMetemodelDao.batchDelete(list);
    }

}

