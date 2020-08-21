package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.dao.TUnionTaskDao;
import com.yjh.platform.module.task.entity.TUnionTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author wf
 * @since 2020-08-19
 */
@Service
public class TUnionTaskService {

    @Autowired
    private TUnionTaskDao tUnionTaskDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.insert(tUnionTask);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public String deleteByPrimaryId(String UnionId) {
        return this.tUnionTaskDao.deleteByPrimaryId(UnionId);
    }

    //修改
    @Logs(title = "修改",code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTask tUnionTask){
        return this.tUnionTaskDao.update(tUnionTask);
    }

    //主键查询
    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TUnionTask selectByPrimaryId(String UnionId){
        return this.tUnionTaskDao.selectByPrimaryId(UnionId);
    }

    //查询
    @Logs(title = "查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> select(String UnionId,Long PlanId,String AreaId,String Name,Integer Type,Integer IfRun, Long RobotId,
                                    Integer Datetype,Integer Remark1,Integer Remark2,Integer Remark3,Integer TaskType,Date StartTime,Date CreateTime){
        List<TUnionTask> list = this.tUnionTaskDao.select(UnionId,PlanId,AreaId,Name,Type,IfRun,RobotId,
                Datetype,Remark1,Remark2,Remark3,TaskType,StartTime,CreateTime);
        return list;
    }

    //分页查询
    @Logs(title = "分页查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> selectByPage(TUnionTask tUnionTask){
        return this.tUnionTaskDao.select(tUnionTask);
    }
}

