package com.yjh.accessvideo.module.device.service;

import com.yjh.accessvideo.module.device.entity.TStdDevicemete;
import com.yjh.accessvideo.module.device.dao.TStdDevicemeteDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-10-19
*/
@Service
public class TStdDevicemeteService{

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdDevicemete tStdDevicemete) {
        return this.tStdDevicemeteDao.insert(tStdDevicemete);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDevicemete tStdDevicemete) {
        return this.tStdDevicemeteDao.update(tStdDevicemete);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevicemete selectByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevicemete> selectByPage(TStdDevicemete tStdDevicemete) {
        List<TStdDevicemete> tStdDevicemeteList = tStdDevicemeteDao.selectByPage(tStdDevicemete);
        return tStdDevicemeteList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TStdDevicemete> list) {
        return this.tStdDevicemeteDao.batchInsert(list);
    }

}

