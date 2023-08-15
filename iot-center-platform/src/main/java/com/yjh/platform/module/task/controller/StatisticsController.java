package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.Statistics;
import com.yjh.platform.module.task.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.mapping.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/statistics/v1")
@Api(value = "/statistics", tags = "统计信息接口")
public class StatisticsController {

  private final StatisticsService statisticsService;

  private final Logger log = LoggerFactory.getLogger(StatisticsController.class);

  public StatisticsController(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

  @ApiOperation(value = "机器人/无人机可靠性")
  @GetMapping(value = "/robot")
  @Logs(title = "机器人/无人机可靠性",content = "根据用户传递的参数查询机器人/无人机可靠性",logType = 1, authority = "1234")
  public Result robot(@RequestParam(value = "id", required = false) Long id,
                         @RequestParam(value = "type") String type,
                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                         @RequestParam(value = "pageSize", required = false, defaultValue = "8") int pageSize) {
    Result result = new Result();
    try {
      Map<String, Object> resultMap = statisticsService.selectStatisticsRobotForPage(id, type, pageNum, pageSize);
      result.setData(resultMap);
      result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("机器人/无人机可靠性查询失败：", e);
    }
    return result;
  }

  @ApiOperation(value = "摄像机可靠性")
  @GetMapping(value = "/camera")
  @Logs(title = "摄像机可靠性",content = "根据用户传递的参数查询摄像机可靠性",logType = 1, authority = "1234")
  public Result camera(@RequestParam(value = "id", required = false) Long id,
                       @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                       @RequestParam(value = "pageSize", required = false, defaultValue = "8") int pageSize) {
    Result result = new Result();
    try {
      Map<String, Object> resultMap = statisticsService.countCameraForPage(id, pageNum, pageSize);
      result.setData(resultMap);
      result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("摄像机可靠性查询失败：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视点位漏检率")
  @GetMapping(value = "/instance")
  @Logs(title = "巡视点位漏检率",content = "根据用户传递的参数查询巡视点位漏检率",logType = 1, authority = "1234")
  public Result instance(
          @RequestParam(value = "code",required = false)String regionCode,
      @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();
    try {
      result.setData(statisticsService.countInstanceLoss(regionCode,type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("巡视点位漏检率失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "告警审核完成率")
  @GetMapping(value = "/warnCheck")
  @Logs(title = "告警审核完成率",content = "根据用户传递的参数查询告警审核完成率",logType = 1, authority = "1234")
  public Result countWarnCheck(
          @RequestParam(value = "code",required = false)String regionCode,
          @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();

    try {
      List<Statistics> list = statisticsService.countWarnCheck(regionCode,type, year, month);
      result.setData(list);
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("告警审核完成率失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视告警准确率")
  @GetMapping(value = "/warnAccuracy")
  @Logs(title = "巡视告警准确率",content = "根据用户传递的参数查询巡视告警准确率",logType = 1, authority = "1234")
  public Result countWarnAccuracy(
          @RequestParam(value = "code",required = false)String regionCode,
          @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();

    try {
      result.setData(statisticsService.countWarnAccuracy(regionCode,type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("巡视告警准确率失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视结果人工审核完成率")
  @GetMapping(value = "/resultCheck")
  @Logs(title = "巡视结果人工审核完成率",content = "根据用户传递的参数查询巡视结果人工审核完成率",logType = 1, authority = "1234")
  public Result countResultCheck(
          @RequestParam(value = "code",required = false)String regionCode,
          @RequestParam(value = "type") Integer type,
      @RequestParam(value = "year") Integer year,
      @RequestParam(value = "month") Integer month) {
    Result result = new Result();

    try {
      result.setData(statisticsService.countResultCheck(regionCode,type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("巡视结果人工审核完成率失败查询描述：", e);
    }
    return result;
  }

  @ApiOperation(value = "巡视任务闭环率")
  @GetMapping(value = "/taskCheck")
  @Logs(title = "巡视任务闭环率",content = "根据用户传递的参数查询巡视任务闭环率",logType = 1, authority = "1234")
  public Result taskCheck(
          @RequestParam(value = "code",required = false)String regionCode,
          @RequestParam(value = "type") Integer type,
          @RequestParam(value = "year") Integer year,
          @RequestParam(value = "month") Integer month) {
    Result result = new Result();
    try {
      result.setData(statisticsService.countTask(regionCode,type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
      log.error("巡视任务闭环率失败查询描述：", e);
    }
    return result;
  }


  @ApiOperation(value = "执行巡视任务次数")
  @GetMapping(value = "/taskFrequency")
  @Logs(title = "巡视任务执行次数", content = "按日月周统计任务执行次数", logType = 1, authority = "1234")
  public Result taskFrequency(
          @RequestParam(value = "code",required = false)String regionCode,
          @RequestParam(value = "type") Integer type,
          @RequestParam(value = "year") Integer year,
          @RequestParam(value = "month") Integer month
  ) {
    Result result = new Result();
    try {
      result.setData(statisticsService.countTaskFrequency(type, year, month));
    } catch (Exception e) {
      result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
      log.error("执行巡视任务次数统计失败：", e);
    }
    return result;
  }


  @ApiOperation(value = "统计巡视任务时长")
  @GetMapping(value = "/taskExecutedPeriod")
  @Logs(title = "统计巡视任务执行时长", content = "按日月周统计任务执行总时长", logType = 1, authority = "1234")
  public Result taskExecutedPeriod(
          @RequestParam(value = "type") Integer type,
          @RequestParam(value = "year") Integer year,
          @RequestParam(value = "month") Integer month
  ) {
    Result result = new Result();
    try{
      result.setData(statisticsService.countTaskExecutedDuration(type, year, month));
    }catch (Exception e){
      result.setCode(ResultCodeEnum.QUERYERROR.getCode(),ResultCodeEnum.QUERYERROR.getName());
      log.info("统计巡视任务时长统计失败：",e);
    }
    return result;
  }

  @ApiOperation(value = "统计任务执行发现的缺陷")
  @GetMapping(value = "/taskFoundDefects")
  @Logs(title = "巡视任务执行次数", content = "按日月周统计任务执行次数", logType = 1, authority = "1234")
  public Result taskFoundDefects(
          @RequestParam(value = "type") Integer type,
          @RequestParam(value = "year") Integer year,
          @RequestParam(value = "month") Integer month
  ) {
    Result result = new Result();
    try {
      result.setData(statisticsService.countDefectsOfTask(type, year, month));
    }catch (Exception e){
      log.error("统计任务执行发现的缺陷查询失败：",e);
      result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
    }
    return result;
  }

  @ApiOperation(value = "导出任务执行可靠性报表")
  @GetMapping(value = "exportTaskExecutedReliableTable")
  @Logs(title = "导出任务执行可靠性报表",content = "按日月周导出任务执行次数、时长、发现缺陷的重量与数量", logType = 1, authority = "1234")
  public Result exportTaskExecutedReliableTable(
          @RequestParam(value = "type") Integer type,
          @RequestParam(value = "year") Integer year,
          @RequestParam(value = "month") Integer month
  ){
    Result result = new Result();
    try{
      result.setData(statisticsService.exportTable(type, year, month));
    }catch (Exception e){
      log.info("导出任务执行可靠性报表错误：",e);
      result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
    }
    return result;
  }



}
