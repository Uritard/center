package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.service.TCfgTelemeterService;
import com.yjh.platform.module.device.entity.TCfgTelemeter;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
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
 * @author lqh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCfgTelemeter/v1")
@Api(value = "/tCfgTelemeter", description = "遥测量表操作接口")
public class TCfgTelemeterController {

    @Autowired
    private final TCfgTelemeterService tCfgTelemeterService;

    private Logger log = LoggerFactory.getLogger(TCfgTelemeterController.class);

    public TCfgTelemeterController(TCfgTelemeterService tCfgTelemeterService) {
        this.tCfgTelemeterService = tCfgTelemeterService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TCfgTelemeter tCfgTelemeter) {
        Result result = new Result();
        try {
            result.setData(tCfgTelemeterService.insert(tCfgTelemeter));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥测量表操作添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            result.setData(tCfgTelemeterService.deleteByPrimaryId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("遥测量表操作删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥测量表操作删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCfgTelemeter tCfgTelemeter) {
        Result result = new Result();
        try {
            result.setData(tCfgTelemeterService.update(tCfgTelemeter));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("遥测量表操作更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥测量表操作更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            TCfgTelemeter tCfgTelemeter = tCfgTelemeterService.selectByPrimaryId(deviceId);
            result.setData(tCfgTelemeter);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥测量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "deviceId", required = false) String deviceId,
                            @RequestParam(value = "meteId", required = false) String meteId,
                            @RequestParam(value = "meteName", required = false) String meteName,
                            @RequestParam(value = "upEffect", required = false) Float upEffect,
                            @RequestParam(value = "downEffect", required = false) Float downEffect,
                            @RequestParam(value = "metePrecision", required = false) Integer metePrecision,
                            @RequestParam(value = "unit", required = false) String unit,
                            @RequestParam(value = "meteIndex", required = false) Integer meteIndex,
                            @RequestParam(value = "meteCid", required = false) Integer meteCid,
                            @RequestParam(value = "limitBand", required = false) Float limitBand,
                            @RequestParam(value = "changeLimit", required = false) Float changeLimit,
                            @RequestParam(value = "validMid", required = false) String validMid,
                            @RequestParam(value = "validString", required = false) String validString,
                            @RequestParam(value = "invalidValue", required = false) Float invalidValue,
                            @RequestParam(value = "lastValue", required = false) Float lastValue,
                            @RequestParam(value = "lastTime", required = false) Date lastTime,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "deviceType", required = false) String deviceType,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "hilimit1", required = false) Float hilimit1,
                            @RequestParam(value = "lolimit1", required = false) Float lolimit1,
                            @RequestParam(value = "hilimit2", required = false) Float hilimit2,
                            @RequestParam(value = "lolimit2", required = false) Float lolimit2,
                            @RequestParam(value = "hilimit3", required = false) Float hilimit3,
                            @RequestParam(value = "lolimit3", required = false) Float lolimit3,
                            @RequestParam(value = "hilimit4", required = false) Float hilimit4,
                            @RequestParam(value = "lolimit4", required = false) Float lolimit4,
                            @RequestParam(value = "stander", required = false) Float stander,
                            @RequestParam(value = "isshield", required = false) Integer isshield,
                            @RequestParam(value = "storageperiod", required = false) Long storageperiod,
                            @RequestParam(value = "linkMeteId", required = false) String linkMeteId) {
        Result result = new Result();
        try {
            List<TCfgTelemeter> list = tCfgTelemeterService.select(deviceId, meteId, meteName, upEffect, downEffect, metePrecision, unit, meteIndex, meteCid, limitBand, changeLimit, validMid, validString, invalidValue, lastValue, lastTime, meteCode, deviceType, description, hilimit1, lolimit1, hilimit2, lolimit2, hilimit3, lolimit3, hilimit4, lolimit4, stander, isshield, storageperiod, linkMeteId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥测量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCfgTelemeter tCfgTelemeter,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCfgTelemeter> list = tCfgTelemeterService.selectByPage(tCfgTelemeter);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥测量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TCfgTelemeter> list) {
        Result result = new Result();
        try {
        result.setData(tCfgTelemeterService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("遥测量表操作批量插入失败：" + e);
        }
        return result;
    }

}
