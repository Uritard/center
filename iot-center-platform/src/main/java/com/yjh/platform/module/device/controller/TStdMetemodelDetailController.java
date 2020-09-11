package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.service.TStdMetemodelDetailService;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;

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
@RequestMapping("/tStdMeteModelDetail/v1")
@Api(value = "/tStdMeteModelDetail", description = "系统测点模版详细表操作接口")
public class TStdMetemodelDetailController {

    @Autowired
    private final TStdMetemodelDetailService tStdMetemodelDetailService;

    private Logger log = LoggerFactory.getLogger(TStdMetemodelDetailController.class);

    public TStdMetemodelDetailController(TStdMetemodelDetailService tStdMetemodelDetailService) {
        this.tStdMetemodelDetailService = tStdMetemodelDetailService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TStdMeteModelDetail tStdMeteModelDetail) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelDetailService.add(tStdMeteModelDetail));
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
    public Result delete(@RequestParam(value = "modelId", required = true) Long modelId,
                         @RequestParam(value = "meteId",required = true) Long meteId) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelDetailService.deleteByPrimaryId(modelId,meteId));
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
    public Result update(@RequestBody TStdMeteModelDetail tStdMeteModelDetail) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelDetailService.update(tStdMeteModelDetail));
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
    public Result selectByPrimaryId(@RequestParam(value = "modelId", required = true) Long modelId) {
        Result result = new Result();
        try {
            List<TStdMeteModelDetail> tStdMeteModelDetail = tStdMetemodelDetailService.selectByPrimaryId(modelId);
            result.setData(tStdMeteModelDetail);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "modelId", required = false) Long modelId,
                            @RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "customId", required = false) String customId,
                            @RequestParam(value = "customTypeName",required = false)String customTypeName,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "meteName", required = false) String meteName,
                            @RequestParam(value = "meteType", required = false) String meteType,
                            @RequestParam(value = "meteKind", required = false) Integer meteKind,
                            @RequestParam(value = "unit", required = false) String unit,
                            @RequestParam(value = "alarmNote", required = false) String alarmNote,
                            @RequestParam(value = "alarmExplain", required = false) String alarmExplain,
                            @RequestParam(value = "alarmType", required = false) String alarmType,
                            @RequestParam(value = "upEffect", required = false) Float upEffect,
                            @RequestParam(value = "downEffect", required = false) Float downEffect,
                            @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                            @RequestParam(value = "highLimit1", required = false) Float highLimit1,
                            @RequestParam(value = "lowLimit1", required = false) Float lowLimit1,
                            @RequestParam(value = "highLimit2", required = false) Float highLimit2,
                            @RequestParam(value = "lowLimit2", required = false) Float lowLimit2,
                           @RequestParam(value = "highLimit2", required = false) Float highLimit3,
                           @RequestParam(value = "lowLimit2", required = false) Float lowLimit3,
                           @RequestParam(value = "highLimit2", required = false) Float highLimit4,
                           @RequestParam(value = "lowLimit2", required = false) Float lowLimit4,
                            @RequestParam(value = "alarmDelay", required = false) Integer alarmDelay,
                            @RequestParam(value = "alarmCnt", required = false) Integer alarmCnt,
                            @RequestParam(value = "thresholdAbs", required = false) BigDecimal thresholdAbs,
                            @RequestParam(value = "thresholdPer", required = false) BigDecimal thresholdPer,
                            @RequestParam(value = "modulus", required = false) Integer modulus) {
        Result result = new Result();
        try {
            List<TStdMeteModelDetail> list = tStdMetemodelDetailService.select(modelId, meteId, customId,customTypeName, meteCode, meteName, meteType,meteKind, unit, alarmNote, alarmExplain, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2,highLimit3, lowLimit3,highLimit4, lowLimit4, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TStdMeteModelDetail tStdMeteModelDetail,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TStdMeteModelDetail> list = tStdMetemodelDetailService.selectByPage(tStdMeteModelDetail);
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
    @RequestMapping(value = "/batchadd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TStdMeteModelDetail> list) {
        Result result = new Result();
        try {
        result.setData(tStdMetemodelDetailService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }


}
