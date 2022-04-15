package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.RobotAlarm;
import com.yjh.platform.module.task.service.RobotAlarmService;
import com.yjh.platform.module.task.service.StatisticsService;
import com.yjh.platform.module.user.entity.TRobotInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/statistics/v1")
@Api(value = "/statistics", description = "统计信息接口")
public class StatisticsController {

    @Autowired
    private final StatisticsService statisticsService;

    private Logger log = LoggerFactory.getLogger(StatisticsController.class);

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @ApiOperation(value = "机器人/无人机可靠性")
    @GetMapping(value = "/robot")
    public Result robot(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(statisticsService.selectStatisticsRobot(robotId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "声纹可靠性")
    @GetMapping(value = "/voice")
    public Result voice(@RequestParam(value = "voiceId") String voiceId) {
        Result result = new Result();
        try {
            result.setData(null);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "摄像机可靠性")
    @GetMapping(value = "/camera")
    public Result camera(@RequestParam(value = "voiceId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(null);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视点位漏检率")
    @RequestMapping(value = "/instance", method = RequestMethod.GET)
    public Result instance( @RequestParam(value = "id") String taskId,
                            @RequestParam(value = "startTime") Date startTime,
                            @RequestParam(value = "endTime") Date endTime) {
        Result result = new Result();
        try {
            result.setData(statisticsService.countInstanceLoss(taskId,startTime,endTime));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "人工审核完成率")
    @RequestMapping(value = "/warnCheck", method = RequestMethod.GET)
    public Result countWarnCheck(
                           @RequestParam(value = "startTime") Date startTime,
                           @RequestParam(value = "endTime") Date endTime) {
        Result result = new Result();

        try {
            result.setData(statisticsService.countWarnCheck(startTime,endTime));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视告警准确率")
    @RequestMapping(value = "/warnAccuracy", method = RequestMethod.GET)
    public Result countWarnAccuracy(
            @RequestParam(value = "startTime") Date startTime,
            @RequestParam(value = "endTime") Date endTime) {
        Result result = new Result();

        try {
            result.setData(statisticsService.countWarnAccuracy(startTime,endTime));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "巡视结果人工审核完成率")
    @RequestMapping(value = "/resultCheck", method = RequestMethod.GET)
    public Result countResultCheck(
            @RequestParam(value = "startTime") Date startTime,
            @RequestParam(value = "endTime") Date endTime) {
        Result result = new Result();

        try {
            result.setData(statisticsService.countResultCheck(startTime,endTime));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }


}
