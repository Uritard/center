package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.handler.JurisdictionException;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.entity.TCameraAlarm;
import com.yjh.platform.module.task.service.TCameraAlarmService;

import java.util.HashMap;
import java.util.List;
import java.util.Date;

import io.swagger.annotations.*;
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

import javax.servlet.http.HttpServletRequest;


/**
 * @author tt
 * @since 2020-10-15
 */
@RestController
@RequestMapping("/tCameraAlarm/v1")
@Api(value = "/tCameraAlarm", description = "可视设备本体告警表操作接口")
public class TCameraAlarmController {

    @Autowired
    private final TCameraAlarmService tCameraAlarmService;

    private Logger log = LoggerFactory.getLogger(TCameraAlarmController.class);

    public TCameraAlarmController(TCameraAlarmService tCameraAlarmService) {
        this.tCameraAlarmService = tCameraAlarmService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增可视设备本体告警数据",content = "根据用户传递的参数新增可视设备本体告警数据",logType = 2)
    public Result insert(@RequestBody TCameraAlarm tCameraAlarm, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            if (userId.intValue() != 10001) {
                if (tCameraAlarm.getAlarmState() != null) {
                    throw new JurisdictionException();
                }
            }
            result.setData(tCameraAlarmService.insert(tCameraAlarm));
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
    @Logs(title = "删除可视设备本体告警数据",content = "根据用户传递的参数删除可视设备本体告警数据",logType = 4)
    public Result delete(@RequestParam(value = "cameraAlarmId", required = true) Long cameraAlarmId) {
        Result result = new Result();
        try {
            result.setData(tCameraAlarmService.deleteByPrimaryId(cameraAlarmId));
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
    @Logs(title = "修改可视设备本体告警数据",content = "根据用户传递的参数修改可视设备本体告警数据",logType = 3)
    public Result update(@RequestBody TCameraAlarm tCameraAlarm, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            if (userId.intValue() != 10001) {
                TCameraAlarm list = tCameraAlarmService.selectByPrimaryId(tCameraAlarm.getCameraAlarmId());
                if (list.getAlarmState() != tCameraAlarm.getAlarmState()) {
                    throw new JurisdictionException();
                }
            }
            result.setData(tCameraAlarmService.update(tCameraAlarm));
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
    @Logs(title = "查询可视设备本体告警数据",content = "根据用户传递的参数查询可视设备本体告警",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "cameraAlarmId", required = true) Long cameraAlarmId) {
        Result result = new Result();
        try {
            TCameraAlarm tCameraAlarm = tCameraAlarmService.selectByPrimaryId(cameraAlarmId);
            result.setData(tCameraAlarm);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询可视设备本体告警数据",content = "根据用户传递的参数查询可视设备本体告警",logType = 1)
    public Result select(@RequestParam(value = "cameraAlarmId", required = false) Long cameraAlarmId,
                         @RequestParam(value = "alarmName", required = false) String alarmName,
                         @RequestParam(value = "cameraId", required = false) Long cameraId,
                         @RequestParam(value = "stationId", required = false) String stationId,
                         @RequestParam(value = "alarmType", required = false) Integer alarmType,
                         @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                         @RequestParam(value = "alarmInfo", required = false) String alarmInfo,
                         @RequestParam(value = "alarmTime", required = false) Date alarmTime,
                         @RequestParam(value = "isAlarm", required = false) Integer isAlarm,
                         @RequestParam(value = "dealType", required = false) Integer dealType,
                         @RequestParam(value = "dealInfo", required = false) String dealInfo,
                         @RequestParam(value = "dealPersonId", required = false) String dealPersonId,
                         @RequestParam(value = "dealTime", required = false) Date dealTime,
                         @RequestParam(value = "alarmState", required = false) Integer alarmState,
                         @RequestParam(value = "createTime", required = false) Date createTime,
                         @RequestParam(value = "endTime", required = false) Date endTime) {
        Result result = new Result();
        try {
            List<TCameraAlarm> list = tCameraAlarmService.select(cameraAlarmId, alarmName, cameraId, stationId, alarmType, alarmLevel, alarmInfo, alarmTime, isAlarm, dealType, dealInfo, dealPersonId, dealTime, alarmState, createTime, endTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询可视设备本体告警数据",content = "根据用户传递的参数分页查询可视设备本体告警",logType = 1)
    public Result selectByPage(@RequestBody TCameraAlarm tCameraAlarm
                              ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCameraAlarm.getPageNum()!=null?tCameraAlarm.getPageNum():1, tCameraAlarm.getPageSize()!=null?tCameraAlarm.getPageSize():0, true, null, true);
            List<TCameraAlarm> list = tCameraAlarmService.selectByPage(tCameraAlarm);
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
    @Logs(title = "批量新增可视设备本体告警数据",content = "根据用户传递的参数批量新增可视设备本体告警数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCameraAlarm> list, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            if (userId.intValue() != 10001) {
                for (TCameraAlarm e : list) {
                    if (e.getAlarmState() != null) {
                        throw new JurisdictionException();
                    }
                }
            }
            result.setData(tCameraAlarmService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

}
