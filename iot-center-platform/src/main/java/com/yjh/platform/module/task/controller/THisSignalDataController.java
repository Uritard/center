package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.service.THisSignalDataService;
import com.yjh.platform.module.task.entity.THisSignalData;
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
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tHisSignalData/v1")
@Api(value = "/tHisSignalData", description = "遥信历史数据表操作接口")
public class THisSignalDataController {

    @Autowired
    private final THisSignalDataService tHisSignalDataService;

    private Logger log = LoggerFactory.getLogger(THisSignalDataController.class);

    public THisSignalDataController(THisSignalDataService tHisSignalDataService) {
        this.tHisSignalDataService = tHisSignalDataService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody THisSignalData tHisSignalData) {
        Result result = new Result();
        try {
            result.setData(tHisSignalDataService.insert(tHisSignalData));
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
    public Result delete(@RequestParam(value = "Id")Long Id) {
        Result result = new Result();
        try {
            result.setData(tHisSignalDataService.deleteByPrimaryId(Id));
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
    public Result update(@RequestBody THisSignalData tHisSignalData) {
        Result result = new Result();
        try {
            result.setData(tHisSignalDataService.update(tHisSignalData));
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
    public Result selectByPrimaryId(@RequestParam(value = "Id")Long Id) {
        Result result = new Result();
        try {
            THisSignalData tHisSignalData = tHisSignalDataService.selectByPrimaryId(Id);
            result.setData(tHisSignalData);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "Id", required = false) Long Id,
                            @RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "recordTime", required = false) Date recordTime,
                            @RequestParam(value = "meteKind", required = false) Integer meteKind,
                            @RequestParam(value = "meteValue", required = false) String meteValue,
                            @RequestParam(value = "lastMeteValue", required = false) String lastMeteValue) {
        Result result = new Result();
        try {
            List<THisSignalData> list = tHisSignalDataService.select(Id,meteId, deviceId, recordTime, meteKind, meteValue, lastMeteValue);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody THisSignalData tHisSignalData,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<THisSignalData> list = tHisSignalDataService.selectByPage(tHisSignalData);
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
    public Result batchInsert(@RequestBody List<THisSignalData> list) {
        Result result = new Result();
        try {
        result.setData(tHisSignalDataService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
