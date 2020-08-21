package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.TCruisePlan;
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
public class TCruisePlanService {

    @Autowired
    private TCruisePlanDao tCruisePlanDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePlan tCruisePlan) {
        return this.tCruisePlanDao.insert(tCruisePlan);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public String delete(Map<String, Object> map) {
        return this.tCruisePlanDao.delete(map);
    }

    //修改
    @Logs(title = "修改",code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePlan tCruisePlan){
        return this.tCruisePlanDao.update(tCruisePlan);
    }

    //主键查询
    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePlan selectByPrimaryId(Long PlanId){
        return this.tCruisePlanDao.selectByPrimaryId(PlanId);
    }

    //查询
    @Logs(title = "查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> select(Long PlanId,Long InstanceId,Integer PointType,String AreaId,String CruiseRegionIds,
                                    Integer ExceptionType,Long RobotId,String Position,Integer AlgorithmId,String AlgorithmName,
                                    String InferadAnalyze,String IrTempBox,Date CreateTime,Date UpdateTime){
        List<TCruisePlan> list = this.tCruisePlanDao.select(PlanId,InstanceId,PointType,AreaId,CruiseRegionIds,
                ExceptionType,RobotId,Position,AlgorithmId,AlgorithmName,InferadAnalyze,IrTempBox,CreateTime,UpdateTime);
        return list;
    }

    //分页查询
    @Logs(title = "分页查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> selectByPage(TCruisePlan tCruisePlan){
        return this.tCruisePlanDao.select(tCruisePlan);
    }

    //分页查询
    @Logs(title = "任务管理查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> SelectPlanQuery(JSONObject jsonObject) {
        return tCruisePlanDao.SelectPlanQuery(jsonObject);
    }
}

