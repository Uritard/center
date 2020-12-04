package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TCruisePlanAttrService;
import com.yjh.platform.module.task.service.TCruisePlanService;

import java.util.*;
import java.util.stream.Collectors;

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
 * @since 2020-09-07
 */
@RestController
@RequestMapping("/tCruisePlan/v1")
@Api(value = "/tCruisePlan", description = "巡检预案属性表操作接口")
public class TCruisePlanController {

    @Autowired
    private final TCruisePlanService tCruisePlanService;
    @Autowired
    private TCruisePlanAttrService tCruisePlanAttrService;

    private Logger log = LoggerFactory.getLogger(TCruisePlanController.class);

    public TCruisePlanController(TCruisePlanService tCruisePlanService) {
        this.tCruisePlanService = tCruisePlanService;
    }

    @ApiOperation(value = "新增预案")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody Map<String, Object> map) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.insert(map));
            if(result.getData().equals(ResultCodeEnum.CODE10010.getCode())){
                result.setCode(ResultCodeEnum.CODE10010.getCode(),ResultCodeEnum.CODE10010.getName());
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加预案错误:", e);
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
            log.error("删除预案异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody Map<String, Object> planDetailMap) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.update(planDetailMap));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新预案异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
            TCruisePlanCount tCruisePlanCount = tCruisePlanService.selectByPrimaryId(planId);
            result.setData(tCruisePlanCount);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询预案")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "planId", required = false) Long planId,
                            @RequestParam(value = "planName", required = false) String planName,
                            @RequestParam(value = "type", required = false) Integer type,
                            @RequestParam(value = "planPointTypes", required = false) String planPointTypes,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime) {
        Result result = new Result();
        try {
            List<TCruisePlan> list = tCruisePlanService.select(planId, planName, type, planPointTypes, createTime, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
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
            List<TCruisePlanCount> list = tCruisePlanService.selectByPage(tCruisePlan);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPlanPage", method = RequestMethod.POST)
    public Result selectByPlanPage(@RequestBody TCruisePlan tCruisePlan,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruisePlanCountByPage> list = tCruisePlanService.selectByPlanPage(tCruisePlan);
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
    public Result batchInsert(@RequestBody List<TCruisePlan> list) {
        Result result = new Result();
        try {
        result.setData(tCruisePlanService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询标准设备下挂巡检点")
    @RequestMapping(value = "/findInstances", method = RequestMethod.GET)
    public Result findInstances(@RequestParam(value = "deviceIds", required = false) String deviceIds,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        if (Objects.equals(null,deviceIds) || Objects.equals("",deviceIds)) {
            List<InstanceTree> list = new ArrayList<>();
            resultMap.put("count", 0);
            resultMap.put("list", list);
            result.setData(resultMap);
            return result;
        }
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            if (deviceIds.equals(-1)) deviceIds = "";
            List<InstanceTree> list = tCruisePlanService.findInstanceTree(deviceIds);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询预案详情")
    @RequestMapping(value = "/selectPlanDetail", method = RequestMethod.GET)
    public Result selectPlanDetail(@RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
            Map<String, Object> planDetailMap = new HashMap<>();
            TCruisePlanCount tCruisePlanCount = tCruisePlanService.selectByPrimaryId(planId);
            planDetailMap.put("planName", tCruisePlanCount.getPlanName());
            planDetailMap.put("typeName", tCruisePlanCount.getPlanTypeName());
            planDetailMap.put("type", tCruisePlanCount.getType());
            List<TCruisePlanAttrDetail> tCruisePlanAttrDetailList = tCruisePlanAttrService.selectByPrimaryId(planId);
            List<Long> deviceIds = new ArrayList<>();
            for (TCruisePlanAttrDetail tCruisePlanAttrDetail:tCruisePlanAttrDetailList) {
                deviceIds.add(tCruisePlanAttrDetail.getDeviceId());
            }
            deviceIds = deviceIds.stream().distinct().collect(Collectors.toList());
            if (Objects.nonNull(tCruisePlanAttrDetailList.get(0).getSubType())) {
                planDetailMap.put("subType", tCruisePlanAttrDetailList.get(0).getSubType());
            } else {planDetailMap.put("subType", "");}
            if (Objects.nonNull(tCruisePlanAttrDetailList.get(0).getSubTypeName())) {
                planDetailMap.put("subTypeName", tCruisePlanAttrDetailList.get(0).getSubTypeName());
            } else {planDetailMap.put("subTypeName", "");}
            planDetailMap.put("instanceList", tCruisePlanAttrDetailList);
            planDetailMap.put("deviceIds", deviceIds);
            result.setData(planDetailMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡检类型树")
    @RequestMapping(value = "/cruiseTypeTree", method = RequestMethod.GET)
    public Result cruiseTypeTree() {
        Result result = new Result();
        try{
            result.setData(tCruisePlanService.cruiseTypeTree());
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检类型树获取失败描述：", e);
        }
        return result;
    }

}
