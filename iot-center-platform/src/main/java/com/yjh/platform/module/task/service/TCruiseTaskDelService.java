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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskDel tCruiseTaskDel) {
        return this.tCruiseTaskDelDao.insert(tCruiseTaskDel);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId) {
        return this.tCruiseTaskDelDao.deleteByPrimaryId(taskId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskDel tCruiseTaskDel) {
        return this.tCruiseTaskDelDao.update(tCruiseTaskDel);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskDel selectByPrimaryId(String taskId) {
        return this.tCruiseTaskDelDao.selectByPrimaryId(taskId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskDel> select(String taskId, Date delTime, Date createTime) {
        List<TCruiseTaskDel> tCruiseTaskDelList = tCruiseTaskDelDao.select(taskId, delTime, createTime);
        return tCruiseTaskDelList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskDel> selectByPage(TCruiseTaskDel tCruiseTaskDel) {
        List<TCruiseTaskDel> tCruiseTaskDelList = tCruiseTaskDelDao.selectByPage(tCruiseTaskDel);
        return tCruiseTaskDelList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskDel> list) {
        return this.tCruiseTaskDelDao.batchInsert(list);
    }

}

