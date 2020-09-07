package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.entity.TCfgTeleadjust;
import com.yjh.platform.module.device.service.TCfgTeleadjustService;
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
@RequestMapping("/tCfgTeleadjust/v1")
@Api(value = "/tCfgTeleadjust", description = "遥调量表操作接口")
public class TCfgTeleadjustController {

    @Autowired
    private final TCfgTeleadjustService tCfgTeleadjustService;

    private Logger log = LoggerFactory.getLogger(TCfgTeleadjustController.class);

    public TCfgTeleadjustController(TCfgTeleadjustService tCfgTeleadjustService) {
        this.tCfgTeleadjustService = tCfgTeleadjustService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TCfgTeleadjust tCfgTeleadjust) {
        Result result = new Result();
        try {
            result.setData(tCfgTeleadjustService.insert(tCfgTeleadjust));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥调量表操作添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            result.setData(tCfgTeleadjustService.deleteByPrimaryId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("遥调量表操作删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥调量表操作删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCfgTeleadjust tCfgTeleadjust) {
        Result result = new Result();
        try {
            result.setData(tCfgTeleadjustService.update(tCfgTeleadjust));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("遥调量表操作更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥调量表操作更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            TCfgTeleadjust tCfgTeleadjust = tCfgTeleadjustService.selectByPrimaryId(deviceId);
            result.setData(tCfgTeleadjust);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥调量表操作失败描述：", e);
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
                            @RequestParam(value = "adjustKind", required = false) Integer adjustKind,
                            @RequestParam(value = "lastValue", required = false) Float lastValue,
                            @RequestParam(value = "lastTime", required = false) Date lastTime,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "deviceType", required = false) String deviceType,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "stander", required = false) Float stander,
                            @RequestParam(value = "controlenable", required = false) Integer controlenable) {
        Result result = new Result();
        try {
            List<TCfgTeleadjust> list = tCfgTeleadjustService.select(deviceId, meteId, meteName, upEffect, downEffect, metePrecision, unit, meteIndex, meteCid, adjustKind, lastValue, lastTime, meteCode, deviceType, description, stander, controlenable);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥调量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCfgTeleadjust tCfgTeleadjust,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCfgTeleadjust> list = tCfgTeleadjustService.selectByPage(tCfgTeleadjust);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥调量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TCfgTeleadjust> list) {
        Result result = new Result();
        try {
        result.setData(tCfgTeleadjustService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("遥调量表操作批量插入失败：" + e);
        }
        return result;
    }

}
