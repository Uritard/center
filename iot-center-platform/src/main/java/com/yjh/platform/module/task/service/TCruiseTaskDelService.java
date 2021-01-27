package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.entity.TCruiseTaskDel;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author tt
* @since 2020-09-14
*/
@Service
public class TCruiseTaskDelService {

    @Autowired
    private TCruiseTaskDelDao tCruiseTaskDelDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskDel tCruiseTaskDel) {
        return this.tCruiseTaskDelDao.insert(tCruiseTaskDel);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId) {
        return this.tCruiseTaskDelDao.deleteByPrimaryId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskDel tCruiseTaskDel) {
        return this.tCruiseTaskDelDao.update(tCruiseTaskDel);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskDel selectByPrimaryId(String taskId) {
        return this.tCruiseTaskDelDao.selectByPrimaryId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskDel> select(String taskId, Date delTime, Date createTime) {
        List<TCruiseTaskDel> tCruiseTaskDelList = tCruiseTaskDelDao.select(taskId, delTime, createTime);
        return tCruiseTaskDelList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskDel> selectByPage(TCruiseTaskDel tCruiseTaskDel) {
        List<TCruiseTaskDel> tCruiseTaskDelList = tCruiseTaskDelDao.selectByPage(tCruiseTaskDel);
        return tCruiseTaskDelList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskDel> list) {
        return this.tCruiseTaskDelDao.batchInsert(list);
    }

}

