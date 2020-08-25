package com.yjh.platform.module.task.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.service.TCruisePlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wf
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
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Result insert(@RequestBody TCruisePlan tCruisePlan) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.insert(tCruisePlan));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            log.error("添加巡检预案错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "插入计划")
    @RequestMapping(value = "/addplan", method = RequestMethod.POST)
    public Result insertTCruisePlan(@RequestBody TCruisePlan tCruisePlan) {
        Result result = new Result();
        result.setData(tCruisePlanService.insertTCruisePlan(tCruisePlan));
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "PlanId", required = true) Long PlanId,
                         @RequestParam(value = "InstanceId", required = true) Long InstanceId) {
        Result result = new Result();
        Map<String, Object> map = new HashMap<>();
        map.put("PlanId", PlanId);
        map.put("InstanceId", InstanceId);

        try {
            result.setData(tCruisePlanService.delete(map));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.DELETEERROR.getCode(), e.getMessage());
            log.error("巡检预案删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.DELETEERROR.getCode(), ResultCodeEnum.DELETEERROR.getName());
            log.error("巡检预案删除错误:", e);
        }
        return result;
    }

    //更新
    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCruisePlan tCruisePlan) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanService.update(tCruisePlan));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新巡检预案参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新巡检预案参数错误:", e);
        }
        return result;
    }

    //查询 根据主键ID查询
    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "PlanId", required = true) Long PlanId){
        Result result = new Result();

        try {
            result.setData(tCruisePlanService.selectByPrimaryId(PlanId));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //查询
    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "PlanId", required = false) Long PlanId,
                         @RequestParam(value = "InstanceId", required = false) Long InstanceId,
                         @RequestParam(value = "PlanName", required = false) String PlanName,
                         @RequestParam(value = "PointType", required = false) Integer PointType,
                         @RequestParam(value = "AreaId", required = false) String AreaId,
                         @RequestParam(value = "CruiseRegionIds", required = false) String CruiseRegionIds,
                         @RequestParam(value = "ExceptionType", required = false) Integer ExceptionType,
                         @RequestParam(value = "RobotId", required = false) Long RobotId,
                         @RequestParam(value = "Position", required = false) String Position,
                         @RequestParam(value = "AlgorithmId", required = false) Integer AlgorithmId,
                         @RequestParam(value = "AlgorithmName", required = false) String AlgorithmName,
                         @RequestParam(value = "InferadAnalyze", required = false) String InferadAnalyze,
                         @RequestParam(value = "IrTempBox", required = false) String IrTempBox,
                         @RequestParam(value = "CreateTime", required = false) Date CreateTime,
                         @RequestParam(value = "UpdateTime", required = false) Date UpdateTime
    ){
        Result result = new Result();
        try {
            List<TCruisePlan> list = this.tCruisePlanService.select(PlanId,InstanceId,PlanName,PointType,AreaId,CruiseRegionIds,
                    ExceptionType,RobotId,Position,AlgorithmId,AlgorithmName,InferadAnalyze,IrTempBox,CreateTime,UpdateTime);
            result.setData(list);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //分页查询
    @ApiOperation(value = "分页查询智能巡检预案")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody JSONObject obj,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruisePlan> list = tCruisePlanService.selectByPage(obj);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @RequestMapping(value = "/TaskList")
    @ResponseBody
    public Map<String, Object> TaskList(String year, String month, String date) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("year", year);
        param.put("month", month);
        param.put("date", date);
        Map<String, Object> result = tCruisePlanService.TaskListByPage(param);
        return result;
    }
}