package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.entity.TCruiseTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author wf
 * @since 2020-08-19
 */
@Service
public class TCruiseTaskService {

    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTask tCruiseTask) {
        return this.tCruiseTaskDao.insert(tCruiseTask);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public String deleteByPrimaryId(String TaskId) {
        return this.tCruiseTaskDao.deleteByPrimaryId(TaskId);
    }

    //修改
    @Logs(title = "修改",code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTask tCruiseTask){
        return this.tCruiseTaskDao.update(tCruiseTask);
    }

    //主键查询
    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectByPrimaryId(String TaskId){
        return this.tCruiseTaskDao.selectByPrimaryId(TaskId);
    }

    //查询
    @Logs(title = "查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> select(String TaskId,Long PlanId,String AreaId,String Name,Integer Type,Integer IfRun, Long RobotId,
                                    Integer Datetype,Integer Remark1,Integer TaskType,Date StartTime,Date CreateTime){
        List<TCruiseTask> list = this.tCruiseTaskDao.select(TaskId,PlanId,AreaId,Name,Type,IfRun,RobotId,
                Datetype,Remark1,TaskType,StartTime,CreateTime);
        return list;
    }

    //分页查询
    @Logs(title = "分页查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> selectByPage(TCruiseTask tCruiseTask){
        return this.tCruiseTaskDao.select(tCruiseTask);
    }
}

