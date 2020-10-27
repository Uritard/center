package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TWarnInfoService;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
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


/**
 * @author tt
 * @since 2020-10-15
 */
@RestController
@RequestMapping("/tWarnInfo/v1")
@Api(value = "/tWarnInfo", description = "告警信息表操作接口")
public class TWarnInfoController {

    @Autowired
    private final TWarnInfoService tWarnInfoService;

    private Logger log = LoggerFactory.getLogger(TWarnInfoController.class);

    public TWarnInfoController(TWarnInfoService tWarnInfoService) {
        this.tWarnInfoService = tWarnInfoService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TWarnInfo tWarnInfo) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.insert(tWarnInfo));
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
    public Result delete(@RequestParam(value = "warnId", required = true) Long warnId) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.deleteByPrimaryId(warnId));
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
    public Result update(@RequestBody TWarnInfo tWarnInfo) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.update(tWarnInfo));
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
    public Result selectByPrimaryId(@RequestParam(value = "warnId", required = true) Long warnId) {
        Result result = new Result();
        try {
            TWarnInfo tWarnInfo = tWarnInfoService.selectByPrimaryId(warnId);
            result.setData(tWarnInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "warnId", required = false) Long warnId,
                            @RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                            @RequestParam(value = "warnTime", required = false) Date warnTime,
                            @RequestParam(value = "warnType", required = false) Integer warnType,
                            @RequestParam(value = "warnName", required = false) String warnName,
                            @RequestParam(value = "warnContent", required = false) String warnContent,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "cunstomId", required = false) String cunstomId,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "stdMeteId", required = false) Long stdMeteId,
                            @RequestParam(value = "confMode", required = false) Integer confMode,
                            @RequestParam(value = "isWarn", required = false) Integer isWarn,
                            @RequestParam(value = "dealType", required = false) Integer dealType,
                            @RequestParam(value = "dealInfo", required = false) String dealInfo,
                            @RequestParam(value = "dealPersonId", required = false) String dealPersonId,
                            @RequestParam(value = "dealTime", required = false) Date dealTime,
                            @RequestParam(value = "ifWarnDisable", required = false) Integer ifWarnDisable,
                            @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
                            @RequestParam(value = "warnSubtype", required = false) Integer warnSubtype,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "imagePath", required = false) String imagePath,
                            @RequestParam(value = "videoPath", required = false) String videoPath,
                            @RequestParam(value = "value", required = false) String value,
                            @RequestParam(value = "outRange", required = false) String outRange,
                            @RequestParam(value = "linkMessage", required = false) String linkMessage) {
        Result result = new Result();
        try {
            List<TWarnInfo> list = tWarnInfoService.select(warnId, warnLevel, warnTime, warnType, warnName, warnContent, deviceId, cunstomId, instanceId, stdMeteId, confMode, isWarn, dealType, dealInfo, dealPersonId, dealTime, ifWarnDisable, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, outRange, linkMessage);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TWarnInfo tWarnInfo,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TWarnInfo> list = tWarnInfoService.selectByPage(tWarnInfo);
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
    public Result batchInsert(@RequestBody List<TWarnInfo> list) {
        Result result = new Result();
        try {
        result.setData(tWarnInfoService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }
    @ApiOperation(value = "查询所有告警")
    @RequestMapping(value = "/selectWarnByPage", method = RequestMethod.GET)
    public Result selectWarnByPage(@RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                                   @RequestParam(value = "confMode", required = false) Integer confMode,
                                   @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
                                   @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                                   @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                   @RequestParam(value = "deviceName", required = false) String deviceName,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TWarnInfoDetail> list = tWarnInfoService.selectWarnByPage(warnLevel, confMode, alarmSource,startTime,endTime,deviceName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询所有告警失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据告警来源统计告警个数")
    @RequestMapping(value = "/countByAlarmSource", method = RequestMethod.GET)
    public Result countByAlarmSource(){
        Result result = new Result();
        try {
            List<TJContentInfo> list = tWarnInfoService.countByAlarmSource();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据设备类型统计告警个数-柱图")
    @RequestMapping(value = "/countByDeviceType", method = RequestMethod.GET)
    public Result countByDeviceType(){
        Result result = new Result();
        try {
            List<TJContentInfoDetail> list = tWarnInfoService.countByDeviceType();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "统计近一月的所有告警个数-折线图")
    @RequestMapping(value = "/countWarnOnMonth", method = RequestMethod.GET)
    public Result countWarnOnMonth(){
        Result result = new Result();
        try {
            List<WarnStatistical> list = tWarnInfoService.countWarnOnMonth();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据告警处理状态统计告警个数-饼图")
    @RequestMapping(value = "/countWarnConfMode", method = RequestMethod.GET)
    public Result countWarnConfMode(){
        Result result = new Result();
        try {
            List<TJContentInfo> list = tWarnInfoService.countWarnConfMode();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "查看告警处理情况")
    @RequestMapping(value = "/selectAlarmProcess", method = RequestMethod.GET)
    public Result selectAlarmProcess(@RequestParam(value = "warnId") Long warnId){
        Result result = new Result();
        try {
            List<TWarnInfoDetail> list = tWarnInfoService.selectAlarmProcess(warnId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "进行告警处理")
    @RequestMapping(value = "/alarmProcess", method = RequestMethod.PUT)
    public Result alarmProcess(@RequestParam(value = "warnId") Long warnId,
                               @RequestParam(value = "alarmSource") Integer alarmSource,
                               @RequestParam(value = "dealType") Integer dealType,
                               @RequestParam(value = "dealInfo") String dealInfo){
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.alarmProcess(warnId,alarmSource,dealType,dealInfo));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

}
