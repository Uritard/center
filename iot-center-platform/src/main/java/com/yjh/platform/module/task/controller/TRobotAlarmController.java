package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.TRobotAlarm;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.service.TRobotAlarmService;
import io.swagger.annotations.*;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;


/**
 * @author tt
 * @since 2020-10-15
 */
@RestController
@RequestMapping("/t-robot-alarm/v1")
@Api(value = "/t-robot-alarm", description = "机器人本体告警表操作接口")
public class TRobotAlarmController {

    @Autowired
    private final TRobotAlarmService tRobotAlarmService;

    private Logger log = LoggerFactory.getLogger(TRobotAlarmController.class);

    public TRobotAlarmController(TRobotAlarmService tRobotAlarmService) {
        this.tRobotAlarmService = tRobotAlarmService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TRobotAlarm tRobotAlarm) {
        Result result = new Result();
        try {
            result.setData(tRobotAlarmService.insert(tRobotAlarm));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "robotAlarmId", required = true) Long robotAlarmId) {
        Result result = new Result();
        try {
            result.setData(tRobotAlarmService.deleteByPrimaryId(robotAlarmId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TRobotAlarm tRobotAlarm) {
        Result result = new Result();
        try {
            result.setData(tRobotAlarmService.update(tRobotAlarm));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "robotAlarmId", required = true) Long robotAlarmId) {
        Result result = new Result();
        try {
            TRobotAlarm tRobotAlarm = tRobotAlarmService.selectByPrimaryId(robotAlarmId);
            result.setData(tRobotAlarm);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "robotAlarmId", required = false) Long robotAlarmId,
                            @RequestParam(value = "alarmName", required = false) String alarmName,
                            @RequestParam(value = "robotId", required = false) Long robotId,
                            @RequestParam(value = "stationId", required = false) String stationId,
                            @RequestParam(value = "alarmType", required = false) Integer alarmType,
                            @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                            @RequestParam(value = "alarmInfo", required = false) String alarmInfo,
                            @RequestParam(value = "alarmTime", required = false) Date alarmTime,
                            @RequestParam(value = "dealType", required = false) Integer dealType,
                            @RequestParam(value = "dealInfo", required = false) String dealInfo,
                            @RequestParam(value = "dealPersonId", required = false) String dealPersonId,
                            @RequestParam(value = "dealTime", required = false) Date dealTime,
                            @RequestParam(value = "positionStationNum", required = false) String positionStationNum,
                            @RequestParam(value = "positionOffset", required = false) String positionOffset,
                            @RequestParam(value = "alarmState", required = false) Integer alarmState,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "endTime", required = false) Date endTime) {
        Result result = new Result();
        try {
            List<TRobotAlarm> list = tRobotAlarmService.select(robotAlarmId, alarmName, robotId, stationId, alarmType, alarmLevel, alarmInfo, alarmTime, dealType, dealInfo, dealPersonId, dealTime, positionStationNum, positionOffset, alarmState, createTime, endTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TRobotAlarm tRobotAlarm,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TRobotAlarm> list = tRobotAlarmService.selectByPage(tRobotAlarm);
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
    public Result batchInsert(@RequestBody List<TRobotAlarm> list) {
        Result result = new Result();
        try {
        result.setData(tRobotAlarmService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
