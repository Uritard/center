package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TPeriodModelDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TCruiseTaskAttrService;
import com.yjh.platform.module.task.service.TCruiseTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * @author tt
 * @since 2020-08-27
 */
@RestController
@RequestMapping("/tCruiseTask/v1")
@Api(value = "/tCruiseTask", description = "巡检任务表操作接口")
public class TCruiseTaskController {

    @Autowired
    private final TCruiseTaskService tCruiseTaskService;
    @Autowired
    private TCruiseTaskAttrService tCruiseTaskAttrService;
    @Autowired
    private TPeriodModelDao tPeriodModelDao;
    @Autowired
    private TCruisePlanDao tCruisePlanDao;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskController.class);

    public TCruiseTaskController(TCruiseTaskService tCruiseTaskService) {
        this.tCruiseTaskService = tCruiseTaskService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCruiseTaskAdd tCruiseTaskAdd) {
        Result result = new Result();
        try {
            TCruiseTask tCruiseTask = new TCruiseTask();
            if (tCruiseTaskAdd.getIfRun() == 172) {
                String cronExpressionDate = "";
                Long periodId = tCruiseTaskAdd.getPeriodId();
                if (Objects.nonNull(periodId)) {
                    TPeriodModel tPeriodModel = tPeriodModelDao.selectByPrimaryId(periodId);
                    cronExpressionDate = tPeriodModel.getCronExpression();
                } else {
                    // 秒  分  时  天  月  星期  年
                    Map<String, String> mapTime = new HashMap<>();
                    if (Objects.isNull(tCruiseTaskAdd.getMin())) {mapTime.put("min", "");} else {mapTime.put("min", tCruiseTaskAdd.getMin());}
                    if (Objects.isNull(tCruiseTaskAdd.getHour())) {mapTime.put("hour", "");} else {mapTime.put("hour", tCruiseTaskAdd.getHour());}
                    if (Objects.isNull(tCruiseTaskAdd.getDayOfMonth())) {mapTime.put("dayOfMonth", "");} else {mapTime.put("dayOfMonth", tCruiseTaskAdd.getDayOfMonth());}
                    if (Objects.isNull(tCruiseTaskAdd.getMonth())) {mapTime.put("month", "");} else {mapTime.put("month", tCruiseTaskAdd.getMonth());}
                    if (Objects.isNull(tCruiseTaskAdd.getDayOfWeek())) {mapTime.put("dayOfWeek", "");} else {mapTime.put("dayOfWeek", tCruiseTaskAdd.getDayOfWeek());}
                    if (Objects.isNull(tCruiseTaskAdd.getYear())) {mapTime.put("year", "");} else {mapTime.put("year", tCruiseTaskAdd.getYear());}
                    cronExpressionDate = DateTimeUtil.createCronExpression(mapTime);
                    System.out.println("cronExpressionDate: "+cronExpressionDate);
                }
                if (CronExpression.isValidExpression(cronExpressionDate)) {
                    tCruiseTaskAdd.setDateType(cronExpressionDate);
                    tCruiseTask.setDateType(cronExpressionDate);
                    tCruiseTask.setAreaId(tCruiseTaskAdd.getAreaId());
                    tCruiseTask.setIfRun(tCruiseTaskAdd.getIfRun());
                    tCruiseTask.setPlanId(tCruiseTaskAdd.getPlanId());
                    tCruiseTask.setRobotId(tCruiseTaskAdd.getRobotId());
                    tCruiseTask.setTaskName(tCruiseTaskAdd.getTaskName());
                    TCruisePlanCount tCruisePlanCount = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
                    tCruiseTask.setTaskType(tCruisePlanCount.getType());
                    tCruiseTask.setType(tCruiseTaskAdd.getType());
                    result.setData(tCruiseTaskService.insert(tCruiseTask));
                } else {
                    result.setData(ResultCodeEnum.CODE10005.getName());
                    return result;
                }
            } else {
                tCruiseTask.setAreaId(tCruiseTaskAdd.getAreaId());
                tCruiseTask.setIfRun(tCruiseTaskAdd.getIfRun());
                tCruiseTask.setPlanId(tCruiseTaskAdd.getPlanId());
                tCruiseTask.setRobotId(tCruiseTaskAdd.getRobotId());
                tCruiseTask.setTaskName(tCruiseTaskAdd.getTaskName());
                tCruiseTask.setTaskType(tCruiseTaskAdd.getTaskType());
                TCruisePlanCount tCruisePlanCount = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
                tCruiseTask.setType(tCruisePlanCount.getType());
                if (Objects.nonNull(tCruiseTaskAdd.getStartTime()) && !Objects.equals("",tCruiseTaskAdd.getStartTime())) {
                    tCruiseTask.setStartTime(tCruiseTaskAdd.getStartTime());
                } else { tCruiseTask.setStartTime(new Date()); }
                result.setData(tCruiseTaskService.insert(tCruiseTask));
            }


        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加任务错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "taskId", required = true) String taskId,
                         @RequestParam(value = "startTime", required = false) String startTime) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskService.deleteByPrimaryId(taskId, startTime));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除任务异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCruiseTask tCruiseTask) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskService.update(tCruiseTask));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新任务异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            TCruiseTask tCruiseTask = tCruiseTaskService.selectByPrimaryId(taskId);
            result.setData(tCruiseTask);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "taskId", required = false) String taskId,
                         @RequestParam(value = "taskName", required = false) String taskName,
                         @RequestParam(value = "planId", required = false) Long planId,
                         @RequestParam(value = "areaId", required = false) String areaId,
                         @RequestParam(value = "type", required = false) Integer type,
                         @RequestParam(value = "ifRun", required = false) Integer ifRun,
                         @RequestParam(value = "robotId", required = false) Long robotId,
                         @RequestParam(value = "dateType", required = false) String dateType,
                         @RequestParam(value = "taskType", required = false) Integer taskType,
                         @RequestParam(value = "startTime", required = false) Date startTime,
                         @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TCruiseTask> list = tCruiseTaskService.select(taskId, taskName, planId, areaId, type, ifRun, robotId, dateType, taskType, startTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruiseTask tCruiseTask,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruiseTask> list = tCruiseTaskService.selectByPage(tCruiseTask);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TCruiseTask> list) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

    //任务统计
    @ApiOperation(value = "任务统计")
    @RequestMapping(value = "/taskCount", method = RequestMethod.GET)
    public Result taskCount(@RequestParam(value = "taskDate", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date taskStartDate){
        Result result = new Result();
        try {
            List<Map<String, Object>> list = this.tCruiseTaskService.taskCount(taskStartDate);
            result.setData(list);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询任务巡检点状态信息")
    @RequestMapping(value = "/selectPointStatus", method = RequestMethod.POST)
    public Result selectPointStatus(@RequestParam(value = "taskId", required = false) String taskId,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruiseTaskList> list = tCruiseTaskService.selectPointStatus(taskId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


}
