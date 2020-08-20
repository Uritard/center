package com.yjh.platform.module.user.controller;

import com.yjh.platform.module.user.service.TCruisePlanService;
import com.yjh.platform.module.user.entity.TCruisePlan;
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
 * @since 2020-08-19
 */
@RestController
@RequestMapping("/tCruisePlan/v1")
@Api(value = "/tCruisePlan", description = "巡检预案表操作接口")
public class TCruisePlanController {

    @Autowired
    private final TCruisePlanService tCruisePlanService;

    private Logger log = LoggerFactory.getLogger(TCruisePlanController.class);

    public TCruisePlanController(TCruisePlanService tCruisePlanService) {
        this.tCruisePlanService = tCruisePlanService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCruisePlan tCruisePlan) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.insert(tCruisePlan));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡检预案添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.deleteByPrimaryId(planId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("巡检预案删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡检预案删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCruisePlan tCruisePlan) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.update(tCruisePlan));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("巡检预案更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检预案更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
            TCruisePlan tCruisePlan = tCruisePlanService.selectByPrimaryId(planId);
            result.setData(tCruisePlan);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检预案失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "planId", required = false) Long planId,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "pointType", required = false) Integer pointType,
                            @RequestParam(value = "cruiseRegionIds", required = false) String cruiseRegionIds,
                            @RequestParam(value = "exceptionType", required = false) Integer exceptionType,
                            @RequestParam(value = "robotId", required = false) Long robotId,
                            @RequestParam(value = "position", required = false) String position,
                            @RequestParam(value = "algorithmId", required = false) Integer algorithmId,
                            @RequestParam(value = "algorithmName", required = false) String algorithmName,
                            @RequestParam(value = "inferadAnalyze", required = false) String inferadAnalyze,
                            @RequestParam(value = "irTempBox", required = false) String irTempBox,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime) {
        Result result = new Result();
        try {
            List<TCruisePlan> list = tCruisePlanService.select(planId, instanceId, pointType, cruiseRegionIds, exceptionType, robotId, position, algorithmId, algorithmName, inferadAnalyze, irTempBox, createTime, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检预案失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruisePlan tCruisePlan,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruisePlan> list = tCruisePlanService.selectByPage(tCruisePlan);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检预案失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TCruisePlan> list) {
        Result result = new Result();
        try {
        result.setData(tCruisePlanService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("巡检预案批量插入失败：" + e);
        }
        return result;
    }

}
