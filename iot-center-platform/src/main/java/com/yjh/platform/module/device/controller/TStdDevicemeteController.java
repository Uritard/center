package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.service.TStdDevicemeteService;
import com.yjh.platform.module.device.entity.TStdDeviceMete;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
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
 * @since 2020-08-08
 */
@RestController
@RequestMapping("/tStdDeviceMete/v1")
@Api(value = "/tStdDeviceMete", description = "标准设备测点表操作接口")
public class TStdDevicemeteController {

    @Autowired
    private final TStdDevicemeteService tStdDevicemeteService;

    private Logger log = LoggerFactory.getLogger(TStdDevicemeteController.class);

    public TStdDevicemeteController(TStdDevicemeteService tStdDevicemeteService) {
        this.tStdDevicemeteService = tStdDevicemeteService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TStdDeviceMete tStdDeviceMete) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.insert(tStdDeviceMete));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准设备测点添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceMeteId") Long deviceMeteId) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.deleteByPrimaryId(deviceMeteId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("标准设备测点删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准设备测点删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TStdDeviceMete tStdDeviceMete) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.update(tStdDeviceMete));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("标准设备测点更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceMeteId") Long deviceMeteId) {
        Result result = new Result();
        try {
            TStdDeviceMete tStdDeviceMete = tStdDevicemeteService.selectByPrimaryId(deviceMeteId);
            result.setData(tStdDeviceMete);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "customId", required = false) String customId,
                            @RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "meteType", required = false) Integer meteType,
                            @RequestParam(value = "meteName", required = false) String meteName,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "customType", required = false) Integer customType,
                            @RequestParam(value = "positionType", required = false) String positionType,
                            @RequestParam(value = "unit", required = false) String unit,
                            @RequestParam(value = "alarmNote", required = false) String alarmNote,
                            @RequestParam(value = "alarmType", required = false) String alarmType,
                            @RequestParam(value = "upEffect", required = false) Float upEffect,
                            @RequestParam(value = "lowEffect", required = false) Float lowEffect,
                            @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                            @RequestParam(value = "highLimit1", required = false) Float highLimit1,
                            @RequestParam(value = "lowLimit1", required = false) Float lowLimit1,
                            @RequestParam(value = "highLimit2", required = false) Float highLimit2,
                            @RequestParam(value = "lowLimit2", required = false) Float lowLimit2,
                            @RequestParam(value = "alarmDelay", required = false) Integer alarmDelay,
                            @RequestParam(value = "alarmCnt", required = false) Integer alarmCnt,
                            @RequestParam(value = "thresholdAbs", required = false) BigDecimal thresholdAbs,
                            @RequestParam(value = "thresholdPer", required = false) BigDecimal thresholdPer,
                            @RequestParam(value = "modulus", required = false) Integer modulus,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TStdDeviceMete> list = tStdDevicemeteService.select(deviceMeteId, deviceId, customId, meteId, meteType, meteName, deviceType, customType, positionType, unit, alarmNote, alarmType, upEffect, lowEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TStdDeviceMete tStdDeviceMete,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TStdDeviceMete> list = tStdDevicemeteService.selectByPage(tStdDeviceMete);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TStdDeviceMete> list) {
        Result result = new Result();
        try {
        result.setData(tStdDevicemeteService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("标准设备测点批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备模版ID查询对应测点")
    @RequestMapping(value = "/selectDevMeteByModelId", method = RequestMethod.GET)
    public Result selectDevMeteByModelId(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();
        try {
            List<TStdDeviceMete> list = tStdDevicemeteService.selectDevMeteByModelId(modelId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "新增/修改设备测点")
    @RequestMapping(value = "/batchUpdateDevMete", method = RequestMethod.POST)
    public Result batchUpdateDevMete(@RequestBody List<TStdDeviceMete> list) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.batchUpdateDevMete(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增/修改设备测点失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备Id删除设备测点")
    @RequestMapping(value = "/deleteByDevId", method = RequestMethod.DELETE)
    public Result deleteByDevId(@RequestParam(value = "deviceId") Long deviceId) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.deleteByDevId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("根据设备Id删除设备测点异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("根据设备Id删除设备测点错误:", e);
        }
        return result;
    }

}
