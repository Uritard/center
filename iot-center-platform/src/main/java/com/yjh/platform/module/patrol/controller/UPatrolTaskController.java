/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskAlarm;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskStatus;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.TPeriodModelDao;
import com.yjh.platform.module.task.entity.TCruiseTaskAdd;
import com.yjh.platform.module.task.entity.TCruiseTaskList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/11
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/uPatrolTask/v1")
@Api(value = "/uPatrolTask", tags = {"新的巡检任务接口"})
public class UPatrolTaskController {

    private final UPatrolTaskService uPatrolTaskService;
    private final PatrolResultHandler patrolResultHandler;

    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private TPeriodModelDao tPeriodModelDao;

    private Logger log = LoggerFactory.getLogger(UPatrolTaskController.class);

    public UPatrolTaskController(UPatrolTaskService uPatrolTaskService, PatrolResultHandler patrolResultHandler) {
        this.uPatrolTaskService = uPatrolTaskService;
        this.patrolResultHandler = patrolResultHandler;
    }

    @ApiOperation(value = "机器人/无人机巡视结果")
    @PostMapping(value = "/robotPatrolTaskResult")
    public Result robotPatrolTaskResult(@RequestBody List<RobotPatrolTaskResult> resultList) {
        Result result = new Result();
        try {
            log.info("The resultList from accessRobot is=={}", resultList);
            patrolResultHandler.robotPatrolTaskResult(resultList);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("接收并处理机器人/无人机巡视结果错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "机器人/无人机任务状态")
    @PostMapping(value = "/robotPatrolTaskStatus")
    public Result robotPatrolTaskStatus(@RequestBody List<RobotPatrolTaskStatus> statusList) {
        Result result = new Result();
        try {
            log.info("The statusList from accessRobot is=={}", statusList);
            uPatrolTaskService.robotPatrolTaskStatus(statusList);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("接收并处理机器人/无人机任务状态错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "机器人/无人机测点告警")
    @PostMapping(value = "/robotPatrolTaskAlarm")
    public Result robotPatrolTaskAlarm(@RequestBody List<RobotPatrolTaskAlarm> alarmList) {
        Result result = new Result();
        try {
            log.info("The alarmList from accessRobot is=={}", alarmList);
            patrolResultHandler.robotPatrolTaskAlarm(alarmList);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("接收并处理机器人/无人机测点告警错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增任务", content = "根据用户传递的参数新增数据", logType = 2, authority = "1235")
    public Result taskConfirmation(HttpServletRequest request, @RequestBody TCruiseTaskAdd tCruiseTaskAdd) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            tCruiseTaskAdd.setCreateUserId(Optional.ofNullable(userId).isPresent() ? Long.parseLong(userId) : null);
            int i = uPatrolTaskService.taskConfirmation(userId, tCruiseTaskAdd.getpCode(), request, tCruiseTaskAdd.getIdentifier());
            if (i == 1) {
                result.setData(uPatrolTaskService.insert(tCruiseTaskAdd));
            } else {
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

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除巡检任务", content = "根据用户传递的参数删除巡检任务数据", logType = 4)
    public Result delete(@RequestParam(value = "taskId", required = true) String taskId,
                         @RequestParam(value = "startTime", required = false) String startTime) {
        Result result = new Result();
        try {
            result.setData("");
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除任务异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务启动")
    @RequestMapping(value = "/taskStart", method = RequestMethod.GET)
    @Logs(title = "任务启动",content = "任务启动",logType = 29,authority = "1235")
    public Result taskStart(@RequestParam(value = "taskId") String taskId) {
        Result result = new Result();
        try {
            result.setData(uPatrolTaskService.taskStart(taskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("任务启动异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务启动错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "任务暂停")
    @RequestMapping(value = "/taskPause", method = RequestMethod.GET)
    @Logs(title = "任务暂停",content = "任务暂停",logType = 11,authority = "1235")
    public Result taskPause(@RequestParam(value = "taskId") String taskId) {
        Result result = new Result();
        try {
            result.setData(uPatrolTaskService.taskPause(taskId));
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
    @Logs(title = "任务恢复",content = "任务恢复",logType = 12,authority = "1235")
    public Result taskGoOn(@RequestParam(value = "taskId") String taskId) {
        Result result = new Result();
        try {
            result.setData(uPatrolTaskService.taskGoOn(taskId));
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
    @Logs(title = "任务终止",content = "任务终止",logType = 13,authority = "1235")
    public Result taskShutDown(@RequestParam(value = "taskId") String taskId) {
        Result result = new Result();
        try {
            result.setData(uPatrolTaskService.taskShutDown(taskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("任务终止异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务终止错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务统计")
    @RequestMapping(value = "/taskCount", method = RequestMethod.GET)
    @Logs(title = "查询巡检任务",content = "根据用户传递的参数统计任务",logType = 1,authority = "1235")
    public Result taskCount(@RequestParam(value = "taskDate", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date taskStartDate,
                            @RequestParam(value = "flag") int flag){
        Result result = new Result();
        try {
            List<Map<String, Object>> list = uPatrolTaskService.taskCount(taskStartDate, flag);
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
            List<TCruiseTaskList> list = uPatrolTaskService.selectPointStatus(taskId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //任务统计
    @ApiOperation(value = "任务查询")
    @RequestMapping(value = "/taskCountByCondition", method = RequestMethod.GET)
    @Logs(title = "任务查询",content = "根据用户传递的参数查询任务",logType = 1,authority = "1235")
    public Result taskCountByCondition(@RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
                                       @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                       @RequestParam(value = "taskState", required = false)  String taskState,
                                       @RequestParam(value = "taskName", required = false)  String taskName){
        Result result = new Result();
        try {
            List<Map<String, Object>> list = this.uPatrolTaskService.taskCountByCondition(startTime,endTime,taskState,taskName);
            result.setData(list);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "站端任务下发")
    @RequestMapping(value = "/upSystemIssuedTask", method = RequestMethod.POST)
    public Result upSystemIssuedTask(@RequestBody Map<String,List<TCruiseTaskAdd>> map) {
        Result result = new Result();
        try {
            TCruiseTaskAdd tCruiseTaskAdd = map.get("list").get(0);
            log.info("--站端任务下发--"+tCruiseTaskAdd);
            tCruiseTaskAdd.setTaskCode(tCruiseTaskAdd.getTaskId());
            tCruiseTaskAdd.setTaskId(null);
            result.setData(uPatrolTaskService.insert(tCruiseTaskAdd));
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
    @RequestMapping(value = "/executeDifferentiateTask",method = RequestMethod.POST)
    public Result executeDifferentiateTask(@RequestBody List<String> images){
        Result result=new Result();
        try {
            result.setData(uPatrolTaskService.executeDifferentiateTasks(images));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("任务下发失败");
        }
        return  result;
    }


    @ApiOperation(value = "低优先任务继续")
    @RequestMapping(value = "/lowTaskGoOn", method = RequestMethod.POST)
    public Result lowTaskGoOn(@RequestBody List<String> lowTaskIdList) {
        Result result = new Result();
        try {
            lowTaskIdList.forEach(taskId->{
                try {
                    uPatrolTaskService.taskGoOn(taskId);
                }catch (Exception e){
                    log.info("其他服务调低优先级任务继续出错:{}",e);
                }
            });
            result.setData(1);
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("低优先任务继续异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("低优先任务继续错误:", e);
        }
        return result;
    }

}
