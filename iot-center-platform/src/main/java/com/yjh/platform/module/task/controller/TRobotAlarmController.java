package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.handler.JurisdictionException;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.entity.TRobotAlarm;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.service.TRobotAlarmService;
import io.swagger.annotations.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


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

    @Resource
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TRobotAlarmController.class);

    public TRobotAlarmController(TRobotAlarmService tRobotAlarmService) {
        this.tRobotAlarmService = tRobotAlarmService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增机器人本体告警表",content = "根据用户传递的参数新增机器人本体告警数据",logType = 2)
    public Result insert(@Validated @RequestBody TRobotAlarm tRobotAlarm, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
          //  Integer roleId = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "roleId")));
            if (userId != 10001) {
                if (tRobotAlarm.getAlarmState()!=null) {
                    throw new JurisdictionException();
                }
            }
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
    @Logs(title = "删除机器人本体告警表",content = "根据用户传递的参数删除机器人本体告警数据",logType = 4)
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
    @Logs(title = "修改机器人本体告警表",content = "根据用户传递的参数修改机器人本体告警数据",logType = 3)
    public Result update(@RequestBody TRobotAlarm tRobotAlarm, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
          //  Integer roleId = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "roleId")));
            if (userId != 10001) {
                TRobotAlarm list = tRobotAlarmService.selectByPrimaryId(tRobotAlarm.getRobotAlarmId());
                if (list.getAlarmState()!=tRobotAlarm.getAlarmState()) {
                    throw new JurisdictionException();
                }
            }
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
    @Logs(title = "查询机器人本体告警表",content = "根据用户传递的参数查询机器人本体告警信息",logType = 1)
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
    @Logs(title = "查询机器人本体告警表",content = "根据用户传递的参数查询机器人本体告警信息",logType = 1)
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
    @Logs(title = "查询机器人本体告警表",content = "根据用户传递的参数分页查询机器人本体告警信息",logType = 1)
    public Result selectByPage(@RequestBody TRobotAlarm tRobotAlarm
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tRobotAlarm.getPageNum()!=null?tRobotAlarm.getPageNum():1, tRobotAlarm.getPageSize()!=null?tRobotAlarm.getPageSize():0,true,null,true);
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
    @Logs(title = "批量插入机器人本体告警表",content = "根据用户传递的参数批量插入机器人本体告警数据",logType = 2)
    public Result batchInsert( @RequestBody List<TRobotAlarm> list, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            //Integer roleId = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "roleId")));
            if (userId != 10001) {
                for (TRobotAlarm e : list) {
                    if (e.getAlarmState()!=null) {
                        throw new JurisdictionException();
                    }
                }
            }
        result.setData(tRobotAlarmService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
