/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TPeriodModelDao;
import com.yjh.platform.module.task.entity.TCruisePlanCount;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.task.entity.TCruiseTaskAdd;
import com.yjh.platform.module.task.entity.TPeriodModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
@Slf4j
@RestController
@RequestMapping("/uPatrolTask/v1")
@Api(value = "/uPatrolTask", tags = {"新的巡检任务接口"})
public class UPatrolTaskController {

    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private TPeriodModelDao tPeriodModelDao;
    @Autowired
    private TCruisePlanDao tCruisePlanDao;
    @Resource
    private UPatrolTaskService uPatrolTaskService;

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增任务",content = "根据用户传递的参数新增数据",logType = 2, authority = "1235")
    public Result taskConfirmation(HttpServletRequest request, @RequestBody TCruiseTaskAdd tCruiseTaskAdd) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            tCruiseTaskAdd.setCreateUserId(Optional.ofNullable(userId).isPresent() ? Long.parseLong(userId) : null);
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            int i = uPatrolTaskService.taskConfirmation(userId,tCruiseTaskAdd.getpCode(),request,tCruiseTaskAdd.getIdentifier());
            if(i == 1){
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


    public Result insert(@RequestBody TCruiseTaskAdd tCruiseTaskAdd) {
        Result result = new Result();
        try {
            UPatrolTask uPatrolTask = new UPatrolTask();
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
                    uPatrolTask.setDateType(cronExpressionDate);
                } else {
                    result.setData(ResultCodeEnum.CODE10005.getName());
                    return result;
                }
            } else {

                if (Objects.nonNull(tCruiseTaskAdd.getTaskId()))uPatrolTask.setTaskId(tCruiseTaskAdd.getTaskId());
                if (Objects.nonNull(tCruiseTaskAdd.getStartTime()) && !Objects.equals("",tCruiseTaskAdd.getStartTime())) {
                    uPatrolTask.setStartTime(tCruiseTaskAdd.getStartTime());
                } else {

                    uPatrolTask.setStartTime(new Date());
                    uPatrolTask.setEndTime(new Date());
                }
            }
            uPatrolTask.setTaskName(tCruiseTaskAdd.getTaskName())
                    .setPlanId(tCruiseTaskAdd.getPlanId())
                    .setAreaId(tCruiseTaskAdd.getAreaId())
                    .setTaskType(tCruiseTaskAdd.getTaskType())
                    .setExecuteType(tCruiseTaskAdd.getIfRun())
                    .setCreateUserId(tCruiseTaskAdd.getCreateUserId()) //用户
                    .setRobotId(tCruiseTaskAdd.getRobotId());

            String res = uPatrolTaskService.insert(uPatrolTask,tCruiseTaskAdd);
            if ("啥也不是".equals(res)){
                result.setCode(209,"任务间隔过短,机器人暂不支持");
            }else {
                result.setData(res);
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
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除巡检任务",content = "根据用户传递的参数删除巡检任务数据",logType = 4)
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
}
