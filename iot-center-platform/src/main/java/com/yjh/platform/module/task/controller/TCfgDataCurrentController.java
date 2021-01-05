package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.TCfgUnionRule;
import com.yjh.platform.module.task.service.TCfgDataCurrentService;
import com.yjh.platform.module.task.entity.TCfgDataCurrent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import io.swagger.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCfgDataCurrent/v1")
@Api(value = "/tCfgDataCurrent", description = "实时数据表操作接口")
public class TCfgDataCurrentController {

    @Autowired
    private final TCfgDataCurrentService tCfgDataCurrentService;

    private Logger log = LoggerFactory.getLogger(TCfgDataCurrentController.class);

    public TCfgDataCurrentController(TCfgDataCurrentService tCfgDataCurrentService) {
        this.tCfgDataCurrentService = tCfgDataCurrentService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCfgDataCurrent tCfgDataCurrent) {
        Result result = new Result();
        try {
            result.setData(tCfgDataCurrentService.insert(tCfgDataCurrent));
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
    public Result delete(@RequestParam(value = "meteId", required = true) Long meteId) {
        Result result = new Result();
        try {
            result.setData(tCfgDataCurrentService.deleteByPrimaryId(meteId));
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
    public Result update(@RequestBody TCfgDataCurrent tCfgDataCurrent) {
        Result result = new Result();
        try {
            result.setData(tCfgDataCurrentService.update(tCfgDataCurrent));
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
    public Result selectByPrimaryId(@RequestParam(value = "meteId", required = true) Long meteId) {
        Result result = new Result();
        try {
            TCfgDataCurrent tCfgDataCurrent = tCfgDataCurrentService.selectByPrimaryId(meteId);
            result.setData(tCfgDataCurrent);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "cunstomId", required = false) String cunstomId,
                            @RequestParam(value = "recordTime", required = false) Date recordTime,
                            @RequestParam(value = "meteKind", required = false) Integer meteKind,
                            @RequestParam(value = "regionId", required = false) String regionId,
                            @RequestParam(value = "meteValue", required = false) String meteValue,
                            @RequestParam(value = "lastMeteValue", required = false) String lastMeteValue) {
        Result result = new Result();
        try {
            List<TCfgDataCurrent> list = tCfgDataCurrentService.select(meteId, deviceId, cunstomId, recordTime, meteKind, regionId, meteValue, lastMeteValue);
            result.setData(list);
            Log cLogger = LogFactory.getLog(this.getClass());
            LocalDate localDate=LocalDate.now();
            LocalTime localTime=LocalTime.now();
            cLogger.info(localDate);
            cLogger.info(localTime);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCfgDataCurrent tCfgDataCurrent,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCfgDataCurrent> list = tCfgDataCurrentService.selectByPage(tCfgDataCurrent);
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
    public Result batchInsert(@RequestBody List<TCfgDataCurrent> list) {
        Result result = new Result();
        try {
        result.setData(tCfgDataCurrentService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "联动控制--测试接口")
    @RequestMapping(value = "/unionTest",method = RequestMethod.GET)
    public Result unionTest(@RequestParam Map<String,String> meteId){
        Result result=new Result();
        try {
            result.setData(tCfgDataCurrentService.unionRulesMatchAndCalculate(meteId.get("meteId")));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
}
