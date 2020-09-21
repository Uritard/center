package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCfgDataCurrent;
import com.yjh.platform.module.task.dao.TCfgDataCurrentDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import javax.script.ScriptException;


/**
* @author czh
* @since 2020-08-25
*/
@Service
public class TCfgDataCurrentService{

    @Autowired
    private TCfgDataCurrentDao tCfgDataCurrentDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.insert(tCfgDataCurrent);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.deleteByPrimaryId(meteId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.update(tCfgDataCurrent);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgDataCurrent selectByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.selectByPrimaryId(meteId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> select(Long meteId, Long deviceId, String cunstomId, Date recordTime, Integer meteKind, String regionId, String meteValue, String lastMeteValue) throws ScriptException {
        List<TCfgDataCurrent> tCfgDataCurrentList = tCfgDataCurrentDao.select(meteId, deviceId, cunstomId, recordTime, meteKind, regionId, meteValue, lastMeteValue);

//        Log cLogger = LogFactory.getLog(this.getClass());
//        cLogger.info();

        return tCfgDataCurrentList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> selectByPage(TCfgDataCurrent tCfgDataCurrent) {
        List<TCfgDataCurrent> tCfgDataCurrentList = tCfgDataCurrentDao.selectByPage(tCfgDataCurrent);
        return tCfgDataCurrentList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgDataCurrent> list) {
        return this.tCfgDataCurrentDao.batchInsert(list);
    }

}

