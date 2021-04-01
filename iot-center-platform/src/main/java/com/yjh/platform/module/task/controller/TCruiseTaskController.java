package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TPeriodModelDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TCruiseTaskAttrService;
import com.yjh.platform.module.task.service.TCruiseTaskService;
import com.yjh.platform.module.user.entity.TCameraScreen;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
                    tCruiseTask.setTaskLevel(tCruiseTaskAdd.getTaskLevel());
                    TCruisePlanCount tCruisePlanCount = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
                    tCruiseTask.setTaskType(tCruiseTaskAdd.getTaskType());
                    tCruiseTask.setType(tCruisePlanCount.getType());
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
                tCruiseTask.setTaskLevel(tCruiseTaskAdd.getTaskLevel());
                tCruiseTask.setTaskType(tCruiseTaskAdd.getTaskType());
                TCruisePlanCount tCruisePlanCount = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
                tCruiseTask.setType(tCruisePlanCount.getType());
                if (Objects.nonNull(tCruiseTaskAdd.getTaskId()))tCruiseTask.setTaskId(tCruiseTaskAdd.getTaskId());
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
    @Logs(title = "删除巡检任务",content = "根据用户传递的参数删除巡检任务数据",logType = 4)
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
    @Logs(title = "修改巡检任务",content = "根据用户传递的参数修改巡检任务数据",logType = 3)
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
    @Logs(title = "查询巡检任务",content = "根据用户传递的参数查询巡检任务信息",logType = 1)
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
    @Logs(title = "查询巡检任务",content = "根据用户传递的参数查询巡检任务信息",logType = 1)
    public Result select(@RequestParam(value = "taskId", required = false) String taskId,
                         @RequestParam(value = "taskName", required = false) String taskName,
                         @RequestParam(value = "planId", required = false) Long planId,
                         @RequestParam(value = "areaId", required = false) String areaId,
                         @RequestParam(value = "type", required = false) Integer type,
                         @RequestParam(value = "ifRun", required = false) Integer ifRun,
                         @RequestParam(value = "robotId", required = false) Long robotId,
                         @RequestParam(value = "dateType", required = false) String dateType,
                         @RequestParam(value = "taskType", required = false) Integer taskType,
                         @RequestParam(value = "taskLevel", required = false) Integer taskLevel,
                         @RequestParam(value = "startTime", required = false) Date startTime,
                         @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TCruiseTask> list = tCruiseTaskService.select(taskId, taskName, planId, areaId, type, ifRun, robotId, dateType, taskType, taskLevel, startTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询巡检任务",content = "根据用户传递的参数分页查询巡检任务信息",logType = 1)
    public Result selectByPage(@RequestBody TCruiseTask tCruiseTask
                             ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruiseTask.getPageNum()!=null?tCruiseTask.getPageNum():1, tCruiseTask.getPageSize()!=null?tCruiseTask.getPageSize():0,true,null,true);
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
    @Logs(title = "批量插入巡检任务",content = "根据用户传递的参数批量插入巡检任务信息",logType = 2)
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
    @Logs(title = "查询巡检任务",content = "根据用户传递的参数统计任务",logType = 1)
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
    @Logs(title = "查询任务巡检点状态信息",content = "根据用户传递的参数查询任务巡检点状态信息",logType = 1)
    public Result selectPointStatus(@RequestParam(value = "taskId", required = false) String taskId,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
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

    @ApiOperation(value = "任务暂停")
    @RequestMapping(value = "/taskPause", method = RequestMethod.GET)
    @Logs(title = "任务暂停",content = "任务暂停",logType = 5)
    public Result taskPause(@RequestParam(value = "taskId") String taskId) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskService.taskPause(taskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("任务暂停异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务暂停错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务继续")
    @RequestMapping(value = "/taskGoOn", method = RequestMethod.GET)
    @Logs(title = "任务继续",content = "任务继续",logType = 5)
    public Result taskGoOn(@RequestParam(value = "taskId") String taskId) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskService.taskGoOn(taskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("任务继续异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务继续错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务终止")
    @RequestMapping(value = "/taskShutDown", method = RequestMethod.GET)
    @Logs(title = "任务终止",content = "任务终止",logType = 5)
    public Result taskShutDown(@RequestParam(value = "taskId") String taskId) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskService.taskShutDown(taskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("任务终止异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务终止错误:", e);
        }
        return result;
    }

    //任务统计
    @ApiOperation(value = "任务查询")
    @RequestMapping(value = "/taskCountByCondition", method = RequestMethod.GET)
    @Logs(title = "任务查询",content = "根据用户传递的参数查询任务",logType = 1)
    public Result taskCountByCondition(@RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
                                       @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                       @RequestParam(value = "taskState", required = false)  String taskState,
                                       @RequestParam(value = "taskName", required = false)  String taskName){
        Result result = new Result();
        try {
            List<Map<String, Object>> list = this.tCruiseTaskService.taskCountByCondition(startTime,endTime,taskState,taskName);
            result.setData(list);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    //@Logs(title = "新增任务",content = "根据用户传递的参数新增数据",logType = 2)
    public Result taskConfirmation(HttpServletRequest request,@RequestBody TCruiseTaskAdd tCruiseTaskAdd) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            int i = tCruiseTaskService.taskConfirmation(userId,tCruiseTaskAdd.getPassword());
            if(i == 1){
//                TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
//                tCruiseTaskAdd.setTaskId(taskId);
//                tCruiseTaskAdd.setTaskName(taskName);
//                tCruiseTaskAdd.setPlanId(planId);
//                tCruiseTaskAdd.setAreaId(areaId);
//                tCruiseTaskAdd.setType(type);
//                tCruiseTaskAdd.setIfRun(ifRun);
//                tCruiseTaskAdd.setRobotId(robotId);
//                tCruiseTaskAdd.setDateType(dateType);
//                tCruiseTaskAdd.setTaskType(taskType);
//                tCruiseTaskAdd.setStartTime(startTime);
//                tCruiseTaskAdd.setCreateTime(createTime);
//                tCruiseTaskAdd.setMin(min);
//                tCruiseTaskAdd.setHour(hour);
//                tCruiseTaskAdd.setDayOfMonth(dayOfMonth);
//                tCruiseTaskAdd.setMonth(month);
//                tCruiseTaskAdd.setDayOfWeek(dayOfWeek);
//                tCruiseTaskAdd.setYear(year);
//                tCruiseTaskAdd.setPeriodId(periodId);
                result = this.insert(tCruiseTaskAdd);
            }else {
                result.setCode(209);
                result.setMessage("密码错误");
            }
            //result.setData(tCameraScreenService.update(tCameraScreen,userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.CODE10106.getCode(), e.getMessage());
            log.error("密码错误:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "站端任务控制")
    @RequestMapping(value = "/upSystemCtrl", method = RequestMethod.POST)
    public Result upSystemCtrl(@RequestBody Map<String,List<XMLBaseModel>> map) {
        Result result = new Result();
        try {
            XMLBaseModel xmlBaseModel = map.get("list").get(0);
            result.setData(tCruiseTaskService.upSystemCtrl(xmlBaseModel));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("站端任务控制异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("站端任务控制错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "站端任务下发")
    @RequestMapping(value = "/upSystemIssuedTask", method = RequestMethod.POST)
    public Result upSystemIssuedTask(@RequestBody Map<String,List<TCruiseTaskAdd>> map) {
        Result result = new Result();
        try {
            TCruiseTaskAdd TCruiseTaskAdd = map.get("list").get(0);
            log.info("--站端任务下发--"+TCruiseTaskAdd);
            result.setData(tCruiseTaskService.upSystemIssuedTask(TCruiseTaskAdd));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("站端任务控制异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("站端任务控制错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "图片判别任务下发")
    @RequestMapping(value = "executeDifferentiateTask",method = RequestMethod.POST)
    public Result executeDifferentiateTask(@RequestBody List<String> images){
        Result result=new Result();
        try {
            result.setData(tCruiseTaskService.executeDifferentiateTasks(images));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("任务下发失败");
        }
        return  result;
    }

    @ApiOperation(value = "同步到websocket")
    @RequestMapping(value = "/syncWebsocket", method = RequestMethod.POST)
    public Result syncWebsocketInfo(@RequestParam(value = "json") String json) {
        Result result = new Result();
        try {
            WebSocketServer.sendMsg(json);
            result.setData("同步到websocket");
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("同步到websocket异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("同步到websocket错误:", e);
        }
        return result;
    }

}
