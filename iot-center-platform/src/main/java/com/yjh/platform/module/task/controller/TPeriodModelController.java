package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.entity.TPeriodModelAdd;
import com.yjh.platform.module.task.service.TPeriodModelService;
import com.yjh.platform.module.task.entity.TPeriodModel;

import java.util.*;

import io.swagger.annotations.*;
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
 * @author tt
 * @since 2020-09-16
 */
@RestController
@RequestMapping("/tPeriodModel/v1")
@Api(value = "/tPeriodModel", description = "周期任务模版表操作接口")
public class TPeriodModelController {

    @Autowired
    private final TPeriodModelService tPeriodModelService;

    private Logger log = LoggerFactory.getLogger(TPeriodModelController.class);

    public TPeriodModelController(TPeriodModelService tPeriodModelService) {
        this.tPeriodModelService = tPeriodModelService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TPeriodModelAdd tPeriodModelAdd) {
        Result result = new Result();
        try {
            // 秒  分  时  天  月  星期  年
            Map<String, String> mapTime = new HashMap<>();
            if (Objects.isNull(tPeriodModelAdd.getMin())) {mapTime.put("min", "");} else {mapTime.put("min", tPeriodModelAdd.getMin());}
            if (Objects.isNull(tPeriodModelAdd.getHour())) {mapTime.put("hour", "");} else {mapTime.put("hour", tPeriodModelAdd.getHour());}
            if (Objects.isNull(tPeriodModelAdd.getDayOfMonth())) {mapTime.put("dayOfMonth", "");} else {mapTime.put("dayOfMonth", tPeriodModelAdd.getDayOfMonth());}
            if (Objects.isNull(tPeriodModelAdd.getMonth())) {mapTime.put("month", "");} else {mapTime.put("month", tPeriodModelAdd.getMonth());}
            if (Objects.isNull(tPeriodModelAdd.getDayOfWeek())) {mapTime.put("dayOfWeek", "");} else {mapTime.put("dayOfWeek", tPeriodModelAdd.getDayOfWeek());}
            if (Objects.isNull(tPeriodModelAdd.getYear())) {mapTime.put("year", "");} else {mapTime.put("year", tPeriodModelAdd.getYear());}

            String cronExpressionDate = DateTimeUtil.createCronExpression(mapTime);
            System.out.println("cronExpressionDate: "+cronExpressionDate);
            tPeriodModelAdd.setCronExpression(cronExpressionDate);
            TPeriodModel tPeriodModel = new TPeriodModel();
            tPeriodModel.setCronExpression(cronExpressionDate);
            tPeriodModel.setRemark(tPeriodModelAdd.getRemark());
            result.setData(tPeriodModelService.insert(tPeriodModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加周期任务模版错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "periodId", required = true) Long periodId) {
        Result result = new Result();
        try {
            result.setData(tPeriodModelService.deleteByPrimaryId(periodId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除周期任务模版异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TPeriodModelAdd tPeriodModelAdd) {
        Result result = new Result();
        try {
            // 秒  分  时  天  月  星期  年
            Map<String, String> mapTime = new HashMap<>();
            if (Objects.isNull(tPeriodModelAdd.getMin())) {mapTime.put("min", "");} else {mapTime.put("min", tPeriodModelAdd.getMin());}
            if (Objects.isNull(tPeriodModelAdd.getHour())) {mapTime.put("hour", "");} else {mapTime.put("hour", tPeriodModelAdd.getHour());}
            if (Objects.isNull(tPeriodModelAdd.getDayOfMonth())) {mapTime.put("dayOfMonth", "");} else {mapTime.put("dayOfMonth", tPeriodModelAdd.getDayOfMonth());}
            if (Objects.isNull(tPeriodModelAdd.getMonth())) {mapTime.put("month", "");} else {mapTime.put("month", tPeriodModelAdd.getMonth());}
            if (Objects.isNull(tPeriodModelAdd.getDayOfWeek())) {mapTime.put("dayOfWeek", "");} else {mapTime.put("dayOfWeek", tPeriodModelAdd.getDayOfWeek());}
            if (Objects.isNull(tPeriodModelAdd.getYear())) {mapTime.put("year", "");} else {mapTime.put("year", tPeriodModelAdd.getYear());}

            String cronExpressionDate = DateTimeUtil.createCronExpression(mapTime);
            System.out.println("cronExpressionDate: "+cronExpressionDate);
            tPeriodModelAdd.setCronExpression(cronExpressionDate);
            TPeriodModel tPeriodModel = new TPeriodModel();
            tPeriodModel.setCronExpression(cronExpressionDate);
            tPeriodModel.setRemark(tPeriodModelAdd.getRemark());
            result.setData(tPeriodModelService.update(tPeriodModel));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新周期任务模版异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "periodId", required = true) Long periodId) {
        Result result = new Result();
        try {
            TPeriodModel tPeriodModel = tPeriodModelService.selectByPrimaryId(periodId);
            result.setData(tPeriodModel);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "periodId", required = false) Long periodId,
                            @RequestParam(value = "cronExpression", required = false) String cronExpression,
                            @RequestParam(value = "remark", required = false) String remark,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                            @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TPeriodModel> list = tPeriodModelService.select(periodId, cronExpression, remark, updateTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TPeriodModel tPeriodModel,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TPeriodModel> list = tPeriodModelService.selectByPage(tPeriodModel);
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
    public Result batchInsert(@RequestBody List<TPeriodModel> list) {
        Result result = new Result();
        try {
        result.setData(tPeriodModelService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
