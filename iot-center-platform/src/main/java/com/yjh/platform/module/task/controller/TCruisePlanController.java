package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.module.patrol.service.UPatrolPlanAttrService;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.entity.input.CruisePlanReq;
import com.yjh.platform.module.task.service.TCruisePlanAttrService;
import com.yjh.platform.module.task.service.TCruisePlanService;

import java.util.*;
import java.util.stream.Collectors;

import io.swagger.annotations.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


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
    @Autowired
    private UPatrolPlanAttrService uPatrolPlanAttrService;
    @Autowired
    private LogsRecord logsRecord;

    private Logger log = LoggerFactory.getLogger(TCruisePlanController.class);

    public TCruisePlanController(TCruisePlanService tCruisePlanService) {
        this.tCruisePlanService = tCruisePlanService;
    }

    @ApiOperation(value = "新增预案")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增预案",content = "根据用户传递的参数新增巡检预案属性数据",logType = 2,authority = "1235")
    public Result insert(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        Result result = new Result();
        try {
//            result.setData(tCruisePlanService.insert(map));
            logsRecord.LogsSend(request, "2", "新增预案", "新增预案-" + map.get("planName"));
            result.setData(uPatrolPlanAttrService.insert(map));
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
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除预案",content = "根据用户传递的参数删除巡检预案属性数据",logType = 4,authority = "1235")
    public Result delete(HttpServletRequest request, @RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
//            result.setData(tCruisePlanService.deleteByPrimaryId(planId));
            result.setData(uPatrolPlanAttrService.deleteByPrimaryId(planId, request));
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改预案",content = "根据用户传递的参数修改巡检预案属性数据",logType = 3,authority = "1235")
    public Result update(HttpServletRequest request, @RequestBody Map<String, Object> planDetailMap) {
        Result result = new Result();
        try {
//            result.setData(tCruisePlanService.update(planDetailMap));
            logsRecord.LogsSend(request, "3", "修改预案", "修改预案-" + planDetailMap.get("planName"));
            result.setData(uPatrolPlanAttrService.update(planDetailMap));
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
    @Logs(title = "查询预案",content = "根据用户传递的参数查询巡检预案属性信息",logType = 1,authority = "1234")
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
    @Logs(title = "查询预案",content = "根据用户传递的参数查询巡检预案属性信息",logType = 1,authority = "1234")
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

    @ApiOperation(value = "根据robotId查询预案")
    @RequestMapping(value = "/selectByRobotId", method = RequestMethod.GET)
    @Logs(title = "查询预案",content = "根据用户传递的参数查询巡检预案属性信息",logType = 1,authority = "1234")
    public Result selectByRobotId(@RequestParam(value = "robotId", required = false) Long robotId) {
        Result result = new Result();
        try {
            List<String> List = tCruisePlanService.selectByRobotId(robotId);
            result.setData(List);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据planCode删除")
    @RequestMapping(value = "/deleteByPlanCode", method = RequestMethod.POST)
    @Logs(title = "删除操作票",content = "根据用户传递的参数删除巡检预案属性数据",logType = 4,authority = "1235")
    public Result deleteByPlanCode(@RequestParam(value = "planCode", required = true) String planCode) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.deleteByPlanCode(planCode));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除操作票异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询预案",content = "根据用户传递的参数分页查询巡检预案属性信息",logType = 1,authority = "1235")
    public Result selectByPage(@RequestBody TCruisePlan tCruisePlan
                                ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruisePlan.getPageNum()!=null?tCruisePlan.getPageNum():1, tCruisePlan.getPageSize()!=null?tCruisePlan.getPageSize():0,true,null,true);
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
    @Logs(title = "查询预案",content = "根据用户传递的参数分页查询巡检预案属性信息",logType = 1,authority = "1234,1235")
    public Result selectByPlanPage(@RequestBody TCruisePlan tCruisePlan
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruisePlan.getPageNum()!=null?tCruisePlan.getPageNum():1, tCruisePlan.getPageSize()!=null?tCruisePlan.getPageSize():0,true,null,true);
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

    @ApiOperation(value = "分页查询操作模型(操作票)")
    @RequestMapping(value = "/selectTicketPlanPage", method = RequestMethod.POST)
    @Logs(title = "查询操作票", content = "根据用户传递的参数分页查询操作票信息", logType = 1, authority = "1234,1235")
    public Result selectTicketPlanPage(@RequestBody Map<String, Object> ticketMap) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Long upRegionId = ticketMap.get("upRegionId") == null ? null : Long.parseLong(ticketMap.get("upRegionId").toString());
            Long deviceId = ticketMap.get("deviceId") == null ? null : Long.parseLong(ticketMap.get("deviceId").toString());
            Integer flag = Integer.parseInt(ticketMap.get("flag").toString());
            int pageNum = Integer.parseInt(ticketMap.get("pageNum").toString());
            int pageSize = Integer.parseInt(ticketMap.get("pageSize").toString());
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TCruisePlanCountByPage> list = tCruisePlanService.selectTicketPlanPage(deviceId, upRegionId, flag);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "绑定/解绑操作票")
    @RequestMapping(value = "/updateTicket", method = RequestMethod.POST)
    @Logs(title = "操作票", content = "根据用户传递的参数绑定/解绑操作票信息", logType = 1, authority = "1234")
    public Result updateTicket(@RequestBody Map<String, Object> ticketMap){
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.updateTicket(ticketMap));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量插入预案",content = "根据用户传递的参数批量插入巡检预案属性数据",logType = 2)
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
    @Logs(title = "查询预案",content = "查询标准设备下的巡检点 ",logType = 1 ,authority = "1234,1235")
    public Result findInstances(@RequestParam(value = "deviceIds", required = false) String deviceIds,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
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
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
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

    @ApiOperation(value = "查询单设备操作测点接口")
    @RequestMapping(value = "/queryOperationInstances", method = RequestMethod.GET)
    @Logs(title = "查询操作测点",content = "查询单设备操作测点 ",logType = 1 ,authority = "1235")
    public Result queryOperationInstances(@RequestParam(value = "deviceId", required = false) String deviceId,
                                @RequestParam(value = "robotId", required = false) Long robotId,
                                @RequestParam(value = "type", required = false) Integer type,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<InstanceTree> list = tCruisePlanService.queryOperationInstances(deviceId, robotId, type);
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
    @Logs(title = "查询预案详情",content = "查询预案详情",logType = 1)
    public Result selectPlanDetail(@RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
            Map<String, Object> planDetailMap = new HashMap<>();
            TCruisePlanCount tCruisePlanCount = tCruisePlanService.selectByPrimaryId(planId);
            planDetailMap.put("planName", tCruisePlanCount.getPlanName());
            planDetailMap.put("typeName", tCruisePlanCount.getPlanTypeName());
            planDetailMap.put("type", tCruisePlanCount.getType());
            planDetailMap.put("subType", tCruisePlanCount.getSubType());
            List<TCruisePlanAttrDetail> tCruisePlanAttrDetailList = tCruisePlanAttrService.selectByPrimaryId(planId);
            List<Long> deviceIds = new ArrayList<>();
            for (TCruisePlanAttrDetail tCruisePlanAttrDetail:tCruisePlanAttrDetailList) {
                deviceIds.add(tCruisePlanAttrDetail.getDeviceId());
            }
            deviceIds = deviceIds.stream().distinct().collect(Collectors.toList());
            if (tCruisePlanAttrDetailList.size()>0) {
                if (Objects.nonNull(tCruisePlanAttrDetailList.get(0).getSubType())) planDetailMap.put("subType", tCruisePlanAttrDetailList.get(0).getSubType());
            } else {planDetailMap.put("subType", "");}
            if (tCruisePlanAttrDetailList.size()>0) {
                if (Objects.nonNull(tCruisePlanAttrDetailList.get(0).getSubTypeName())) planDetailMap.put("subTypeName", tCruisePlanAttrDetailList.get(0).getSubTypeName());
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

    @ApiOperation(value = "查询预案详情")
    @RequestMapping(value = "/selectPlanDetailByPage", method = RequestMethod.POST)
    @Logs(title = "查询预案详情", content = "查询预案详情", logType = 1)
    public Result selectPlanDetailByPage(@RequestBody CruisePlanReq cruisePlanReq) {
        Result result = new Result();
        try {
            Map<String, Object> planDetailMap = new HashMap<>();
            TCruisePlanCount tCruisePlanCount = tCruisePlanService.selectByPrimaryId(cruisePlanReq.getPlanId());
            planDetailMap.put("planName", tCruisePlanCount.getPlanName());
            planDetailMap.put("typeName", tCruisePlanCount.getPlanTypeName());
            planDetailMap.put("type", tCruisePlanCount.getType());
            Map<String, Object> resultMap = tCruisePlanAttrService
                .selectPageByPrimaryId(cruisePlanReq.getPageSize(), cruisePlanReq.getPageNum(), cruisePlanReq.getPlanId());
            if ((Long) resultMap.get("count") > 0) {
                List<TCruisePlanAttrDetail> tCruisePlanAttrDetailList = (List<TCruisePlanAttrDetail>)resultMap.get("list");
                List<Long> deviceIds = tCruisePlanAttrDetailList.stream()
                    .map(TCruisePlanAttrDetail::getDeviceId)
                    .distinct()
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(tCruisePlanAttrDetailList)) {
                    if (Objects.nonNull(tCruisePlanAttrDetailList.get(0)) && Objects.nonNull(tCruisePlanAttrDetailList.get(0).getSubType())) {
                        planDetailMap.put("subType", tCruisePlanAttrDetailList.get(0).getSubType());
                    }
                    if (Objects.nonNull(tCruisePlanAttrDetailList.get(0)) && Objects.nonNull(tCruisePlanAttrDetailList.get(0).getSubTypeName())) {
                        planDetailMap.put("subTypeName", tCruisePlanAttrDetailList.get(0).getSubTypeName());
                    }
                }
                planDetailMap.put("instanceList", resultMap.get("list"));
                planDetailMap.put("count", resultMap.get("count"));
                planDetailMap.put("deviceIds", deviceIds);
            }else {
                planDetailMap.put("subType", "");
                planDetailMap.put("count", 0);
                planDetailMap.put("subTypeName", "");
            }
            result.setData(planDetailMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡检类型树")
    @RequestMapping(value = "/cruiseTypeTree", method = RequestMethod.GET)
    @Logs(title = "巡检类型树",content = "查询巡检类型树",logType = 1)
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
