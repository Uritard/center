package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.SYAllInfo;
import com.yjh.platform.module.device.service.TCfgMeteService;
import com.yjh.platform.module.device.entity.TCfgMete;
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


/**
 * @author lqh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCfgMete/v1")
@Api(value = "/tCfgMete", description = "系统测点信息表操作接口")
public class TCfgMeteController {

    @Autowired
    private final TCfgMeteService tCfgMeteService;

    private Logger log = LoggerFactory.getLogger(TCfgMeteController.class);

    public TCfgMeteController(TCfgMeteService tCfgMeteService) {
        this.tCfgMeteService = tCfgMeteService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增系统测点数据",content = "根据用户传递的参数新增系统测点数据",logType = 2)
    public Result add(@RequestBody TCfgMete tCfgMete) {
        Result result = new Result();
        try {
            result.setData(tCfgMeteService.insert(tCfgMete));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点信息表操作添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除系统测点数据",content = "根据用户传递的参数删除系统测点数据",logType = 4)
    public Result delete(@RequestParam(value = "meteId", required = true) String meteId) {
        Result result = new Result();
        try {
            result.setData(tCfgMeteService.deleteByPrimaryId(meteId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("系统测点信息表操作删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点信息表操作删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "更新系统测点数据",content = "根据用户传递的参数更新系统测点数据",logType = 3)
    public Result update(@RequestBody TCfgMete tCfgMete) {
        Result result = new Result();
        try {
            result.setData(tCfgMeteService.update(tCfgMete));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("系统测点信息表操作更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点信息表操作更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询系统测点数据",content = "根据用户传递的参数查询系统测点信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "meteId", required = true) String meteId) {
        Result result = new Result();
        try {
            TCfgMete tCfgMete = tCfgMeteService.selectByPrimaryId(meteId);
            result.setData(tCfgMete);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点信息表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询系统测点数据",content = "根据用户传递的参数查询系统测点信息",logType = 1)
    public Result select(@RequestParam(value = "meteId", required = false) String meteId,
                            @RequestParam(value = "meteType", required = false) String meteType,
                            @RequestParam(value = "meteKind", required = false) Integer meteKind,
                            @RequestParam(value = "meteName", required = false) String meteName,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "unit", required = false) String unit,
                            @RequestParam(value = "meteExplainType", required = false) String meteExplainType,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                            @RequestParam(value = "modulus", required = false) Integer modulus,
                            @RequestParam(value = "stationName", required = false) String stationName,
                            @RequestParam(value = "stationId", required = false) String stationId,
                            @RequestParam(value = "upEffect", required = false) Integer upEffect,
                            @RequestParam(value = "downEffect", required = false) Integer downEffect,
                            @RequestParam(value = "alarmlevel", required = false) Integer alarmlevel,
                            @RequestParam(value = "alarmthresbhold", required = false) Integer alarmthresbhold,
                            @RequestParam(value = "describer", required = false) String describer,
                            @RequestParam(value = "metePrecision", required = false) Integer metePrecision,
                            @RequestParam(value = "changeLimit", required = false) Float changeLimit,
                            @RequestParam(value = "hilimit1", required = false) Float hilimit1,
                            @RequestParam(value = "lolimit1", required = false) Float lolimit1,
                            @RequestParam(value = "hilimit2", required = false) Float hilimit2,
                            @RequestParam(value = "lolimit2", required = false) Float lolimit2,
                            @RequestParam(value = "hilimit3", required = false) Float hilimit3,
                            @RequestParam(value = "lolimit3", required = false) Float lolimit3,
                            @RequestParam(value = "hilimit4", required = false) Float hilimit4,
                            @RequestParam(value = "stander", required = false) Float stander,
                            @RequestParam(value = "controlenable", required = false) Integer controlenable) {
        Result result = new Result();
        try {
            List<TCfgMete> list = tCfgMeteService.select(meteId, meteType, meteKind, meteName, meteCode, unit, meteExplainType, createTime, updateTime, modulus, stationName, stationId, upEffect, downEffect, alarmlevel, alarmthresbhold, describer, metePrecision, changeLimit, hilimit1, lolimit1, hilimit2, lolimit2, hilimit3, lolimit3, hilimit4, stander, controlenable);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点信息表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询系统测点数据",content = "根据用户传递的参数查询系统测点信息",logType = 1)
    public Result selectByPage(@RequestBody TCfgMete tCfgMete
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCfgMete.getPageNum()!=null?tCfgMete.getPageNum():1, tCfgMete.getPageSize()!=null?tCfgMete.getPageSize():0,true,null,true);
            List<TCfgMete> list = tCfgMeteService.selectByPage(tCfgMete);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点信息表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入系统测点数据",content = "根据用户传递的参数批量插入系统测点信息",logType = 2)
    public Result batchAdd(@RequestBody List<TCfgMete> list) {
        Result result = new Result();
        try {
        result.setData(tCfgMeteService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("系统测点信息表操作批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "插入四遥信息")
    @RequestMapping(value = "/addForAll", method = RequestMethod.POST)
    @Logs(title = "插入四遥信息",content = "根据用户传递的参数插入四遥信息",logType = 2)
    public Result addForAll(@RequestBody Map<String, List<SYAllInfo>> syAllInfoMap) {
        Result result = new Result();
        try {
            result.setData(tCfgMeteService.insertForAll(syAllInfoMap));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("插入四遥信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新四遥信息")
    @RequestMapping(value = "/updateForAll", method = RequestMethod.POST)
    @Logs(title = "更新四遥信息",content = "根据用户传递的参数修改数据",logType = 3)
    public Result updateForAll(@RequestBody Map<String, List<SYAllInfo>> syAllInfoMap) {
        Result result = new Result();
        try {
            result.setData(tCfgMeteService.updateForAll(syAllInfoMap));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新四遥信息错误:", e);
        }
        return result;
    }

}
