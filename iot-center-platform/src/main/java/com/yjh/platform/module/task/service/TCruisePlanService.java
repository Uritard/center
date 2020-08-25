package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.controller.TCruisePlanController;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.TCruisePlan;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author wf
 * @since 2020-08-19
 */
@Service
public class TCruisePlanService {

    @Autowired
    private TCruisePlanDao tCruisePlanDao;

    private Logger log = LoggerFactory.getLogger(TCruisePlanController.class);

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePlan tCruisePlan) {
        return this.tCruisePlanDao.insert(tCruisePlan);
    }

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> insertTCruisePlan(TCruisePlan tCruisePlan) {
        Map<String, Object> result = new HashMap<String, Object>();
        Long instanceId = tCruisePlan.getInstanceId();
        String PlanName = tCruisePlan.getPlanName();
        Map<String, Object> taskNameMap = new HashMap<String, Object>();
        try {
            // 判断任务名称是否已经存在
            taskNameMap.put("PlanName", PlanName);
            List<Map<String, Object>> nameList = tCruisePlanDao.TaskByName(taskNameMap);
            ;
            if (nameList != null && nameList.size() > 0) {
                result.put("result", false);
                result.put("message", "任务名称已存在，请检查！");
            } else {
                result.put("instanceId", instanceId);
                this.tCruisePlanDao.insertTCruisePlan(tCruisePlan);
                result.put("result", true);
                result.put("message", "新增智能巡检计划成功");
            }
        } catch (Exception e) {
            log.error("新增智能巡检计划失败", e);
            result.put("result", false);
            result.put("message", "新增智能巡检计划失败");
        }
        return result;
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public String delete(Map<String, Object> map) {
        return this.tCruisePlanDao.delete(map);
    }

    //修改
    @Logs(title = "修改", code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePlan tCruisePlan) {
        return this.tCruisePlanDao.update(tCruisePlan);
    }

    //主键查询
    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePlan selectByPrimaryId(Long PlanId) {
        return this.tCruisePlanDao.selectByPrimaryId(PlanId);
    }

    //查询
    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> select(Long PlanId, Long instanceId, String PlanName, Integer PointType, String AreaId, String CruiseRegionIds,
                                    Integer ExceptionType, Long RobotId, String Position, Integer AlgorithmId, String AlgorithmName,
                                    String InferadAnalyze, String IrTempBox, Date CreateTime, Date UpdateTime) {
        List<TCruisePlan> list = this.tCruisePlanDao.select(PlanId, instanceId, PlanName, PointType, AreaId, CruiseRegionIds,
                ExceptionType, RobotId, Position, AlgorithmId, AlgorithmName, InferadAnalyze, IrTempBox, CreateTime, UpdateTime);
        return list;
    }

    //分页查询
    @Logs(title = "分页查询智能巡检预案", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> selectByPage(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        return this.tCruisePlanDao.selectTCruisePlanResult(map);
    }


    @ApiOperation(value = "查询日程任务数量")
    public Map<String, Object> TaskListByPage(Map<String, Object> param) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            Map<String, Object> dayResult;
            //所有任务Map的集合
            List listAll = new ArrayList();
            Calendar rightNow = Calendar.getInstance();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss");

            int palnYear = Integer.parseInt((String) param.get("year"));
            int palnMonth = Integer.parseInt((String) param.get("month"));
            int startMonth = palnMonth - 1;
            int endMonth = palnMonth + 1;

            return result;
        } catch (Exception e) {
            log.error("分页查询任务日程信息失败", e);
            result.put("result", false);
            result.put("message", "查询失败");
            result.put("data", null);
            return result;
        }
    }
}



