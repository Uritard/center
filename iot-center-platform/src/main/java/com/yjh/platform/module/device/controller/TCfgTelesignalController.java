package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.service.TCfgTelesignalService;
import com.yjh.platform.module.device.entity.TCfgTelesignal;
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
@RequestMapping("/tCfgTelesignal/v1")
@Api(value = "/tCfgTelesignal", description = "遥信量表操作接口")
public class TCfgTelesignalController {

    @Autowired
    private final TCfgTelesignalService tCfgTelesignalService;

    private Logger log = LoggerFactory.getLogger(TCfgTelesignalController.class);

    public TCfgTelesignalController(TCfgTelesignalService tCfgTelesignalService) {
        this.tCfgTelesignalService = tCfgTelesignalService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCfgTelesignal tCfgTelesignal) {
        Result result = new Result();
        try {
            result.setData(tCfgTelesignalService.insert(tCfgTelesignal));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥信量表操作添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            result.setData(tCfgTelesignalService.deleteByPrimaryId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("遥信量表操作删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥信量表操作删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCfgTelesignal tCfgTelesignal) {
        Result result = new Result();
        try {
            result.setData(tCfgTelesignalService.update(tCfgTelesignal));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("遥信量表操作更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥信量表操作更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            TCfgTelesignal tCfgTelesignal = tCfgTelesignalService.selectByPrimaryId(deviceId);
            result.setData(tCfgTelesignal);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥信量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "deviceId", required = false) String deviceId,
                            @RequestParam(value = "meteId", required = false) String meteId,
                            @RequestParam(value = "meteName", required = false) String meteName,
                            @RequestParam(value = "upEffect", required = false) Integer upEffect,
                            @RequestParam(value = "lowEffect", required = false) Integer lowEffect,
                            @RequestParam(value = "meteIndex", required = false) Integer meteIndex,
                            @RequestParam(value = "meteCid", required = false) Integer meteCid,
                            @RequestParam(value = "signalKind", required = false) Integer signalKind,
                            @RequestParam(value = "lastValue", required = false) Integer lastValue,
                            @RequestParam(value = "lastTime", required = false) Date lastTime,
                            @RequestParam(value = "explainType", required = false) Integer explainType,
                            @RequestParam(value = "reportType", required = false) Integer reportType,
                            @RequestParam(value = "reportLevel", required = false) Integer reportLevel,
                            @RequestParam(value = "maskType", required = false) Integer maskType,
                            @RequestParam(value = "maskMid", required = false) String maskMid,
                            @RequestParam(value = "maskString", required = false) String maskString,
                            @RequestParam(value = "maskValue", required = false) Integer maskValue,
                            @RequestParam(value = "delayTime", required = false) Integer delayTime,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "deviceType", required = false) String deviceType,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "isshield", required = false) Integer isshield,
                            @RequestParam(value = "storageperiod", required = false) Long storageperiod,
                            @RequestParam(value = "describer", required = false) String describer,
                            @RequestParam(value = "alarmthresbhold", required = false) Integer alarmthresbhold,
                            @RequestParam(value = "alarmlevel", required = false) Integer alarmlevel,
                            @RequestParam(value = "linkMeteId", required = false) String linkMeteId) {
        Result result = new Result();
        try {
            List<TCfgTelesignal> list = tCfgTelesignalService.select(deviceId, meteId, meteName, upEffect, lowEffect, meteIndex, meteCid, signalKind, lastValue, lastTime, explainType, reportType, reportLevel, maskType, maskMid, maskString, maskValue, delayTime, meteCode, deviceType, description, isshield, storageperiod, describer, alarmthresbhold, alarmlevel, linkMeteId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥信量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCfgTelesignal tCfgTelesignal,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCfgTelesignal> list = tCfgTelesignalService.selectByPage(tCfgTelesignal);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥信量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TCfgTelesignal> list) {
        Result result = new Result();
        try {
        result.setData(tCfgTelesignalService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("遥信量表操作批量插入失败：" + e);
        }
        return result;
    }

}
