package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.Statistics;
import com.yjh.platform.module.task.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/statistics/v1")
@Api(value = "/statistics", description = "统计信息接口")
public class StatisticsController {

  @Autowired private final StatisticsService statisticsService;

  private final Logger log = LoggerFactory.getLogger(StatisticsController.class);

  public StatisticsController(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

  @ApiOperation(value = "机器人/无人机可靠性")
  @GetMapping(value = "/robot")
  public Result robot(
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "type") String type) {
    Result result = new Result();
    try {
      result.setData(statisticsService.selectStatisticsRobot(id, type));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("查询失败：", e);
    }
    return result;
  }

  @ApiOperation(value = "摄像机可靠性")
  @GetMapping(value = "/camera")
  public Result camera(@RequestParam(value = "id", required = false) Long id) {
    Result result = new Result();
    try {
      result.setData(statisticsService.countCamera(id));

    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("查询失败：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视点位漏检率")
  @RequestMapping(value = "/instance", method = RequestMethod.GET)
  public Result instance(
      @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();
    try {
      result.setData(statisticsService.countInstanceLoss(type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "人工审核完成率")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "type",
        required = true,
        value = "查询类型：1:日历 2:周历 3:月历",
        dataType = "Integer"),
    @ApiImplicitParam(
        name = "month",
        required = true,
        value = "日历周历月份:202204，月历年份",
        dataType = "String")
  })
  @RequestMapping(value = "/warnCheck", method = RequestMethod.GET)
  public Result countWarnCheck(
      @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();

    try {
      List<Statistics> list = statisticsService.countWarnCheck(type, year, month);
      result.setData(list);
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视告警准确率")
  @RequestMapping(value = "/warnAccuracy", method = RequestMethod.GET)
  public Result countWarnAccuracy(
      @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();

    try {
      result.setData(statisticsService.countWarnAccuracy(type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视结果人工审核完成率")
  @RequestMapping(value = "/resultCheck", method = RequestMethod.GET)
  public Result countResultCheck(
      @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();

    try {
      result.setData(statisticsService.countResultCheck(type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视任务闭环率")
  @RequestMapping(value = "/taskCheck", method = RequestMethod.GET)
  public Result taskCheck(
          @RequestParam(value = "type") Integer type,
          @RequestParam(value = "year") Integer year,
          @RequestParam(value = "month") Integer month) {
    Result result = new Result();
    try {
      result.setData(statisticsService.countTask(type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("失败查询描述：", e);
    }
    return result;
  }
  
}
