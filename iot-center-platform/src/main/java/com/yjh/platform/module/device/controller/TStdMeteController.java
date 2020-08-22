package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.service.TStdMeteService;
import com.yjh.platform.module.device.entity.TStdMete;

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
 * @since 2020-08-07
 */
@RestController
@RequestMapping("/tStdMete/v1")
@Api(value = "/tStdMete", description = "系统测点属性接口")
public class TStdMeteController {

    @Autowired
    private final TStdMeteService tStdMeteService;

    private Logger log = LoggerFactory.getLogger(TStdMeteController.class);

    public TStdMeteController(TStdMeteService tStdMeteService) {
        this.tStdMeteService = tStdMeteService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TStdMete tStdMete) {
        Result result = new Result();
        try {
            result.setData(tStdMeteService.add(tStdMete));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "stdMeteId", required = true) Long stdMeteId) {
        Result result = new Result();
        try {
            result.setData(tStdMeteService.deleteByPrimaryId(stdMeteId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("系统测点删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TStdMete tStdMete) {
        Result result = new Result();
        try {
            result.setData(tStdMeteService.update(tStdMete));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("系统测点更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "stdMeteId", required = true) Long stdMeteId) {
        Result result = new Result();
        try {
            TStdMete tStdMete = tStdMeteService.selectByPrimaryId(stdMeteId);
            result.setData(tStdMete);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询，名称模糊查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "stdMeteId", required = false) Long stdMeteId,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "meteType", required = false) String meteType,
                            @RequestParam(value = "meteName", required = false) String meteName,
                            @RequestParam(value = "alarmNote", required = false) String alarmNote,
                            @RequestParam(value = "alarmExplain", required = false) String alarmExplain,
                            @RequestParam(value = "alarmType", required = false) String alarmType,
                            @RequestParam(value = "unit", required = false) String unit,
                            @RequestParam(value = "upEffect", required = false) Float upEffect,
                            @RequestParam(value = "lowEffect", required = false) Float lowEffect,
                            @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                            @RequestParam(value = "alarmLimit", required = false) Integer alarmLimit,
                            @RequestParam(value = "alarmDelay", required = false) Integer alarmDelay,
                            @RequestParam(value = "alarmCnt", required = false) Integer alarmCnt,
                            @RequestParam(value = "thresholdAbs", required = false) BigDecimal thresholdAbs,
                            @RequestParam(value = "thresholdPer", required = false) BigDecimal thresholdPer,
                            @RequestParam(value = "modulus", required = false) Integer modulus,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TStdMete> list = tStdMeteService.select(stdMeteId, deviceType, meteCode, meteType, meteName, alarmNote, alarmExplain, alarmType, unit, upEffect, lowEffect, alarmLevel, alarmLimit, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询，名称模糊查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TStdMete tStdMete,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TStdMete> list = tStdMeteService.selectByPage(tStdMete);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TStdMete> list) {
        Result result = new Result();
        try {
        result.setData(tStdMeteService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("系统测点批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备类型查询mete")
    @RequestMapping(value = "/selectByDeviceType",method = RequestMethod.GET)
    public Result selectByDeviceType(@RequestParam (value = "deviceType",required = true) Integer deviceType){
        Result result =new Result();
        try{
            List<TStdMete> mete=tStdMeteService.selectByDeviceType(deviceType);
            result.setData(mete);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点失败描述：", e);
        }

        return result;
    }

}
