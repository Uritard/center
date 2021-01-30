package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.ReportManageService;
import com.yjh.platform.module.task.service.TCruiseResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCruiseResult/v1")
@Api(value = "/tCruiseResult")
public class TCruiseResultController {

    @Autowired
    private final TCruiseResultService tCruiseResultService;

    @Autowired
    private ReportManageService reportManageService;

    private Logger log = LoggerFactory.getLogger(TCruiseResultController.class);

    public TCruiseResultController(TCruiseResultService tCruiseResultService) {
        this.tCruiseResultService = tCruiseResultService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "新增",content = "根据用户传递的参数新增巡检任务结果数据",logType = 2)
    public Result insert(@RequestBody TCruiseResult tCruiseResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.insert(tCruiseResult));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @DeleteMapping(value = "/delete")
    @Logs(title = "删除",content = "根据用户传递的参数删除巡检任务结果数据",logType = 4)
    public Result delete(@RequestParam(value = "taskResultId", required = true) String taskResultId) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.deleteByPrimaryId(taskResultId));
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
    @PutMapping(value = "/update")
    @Logs(title = "修改",content = "根据用户传递的参数修改巡检任务结果数据",logType = 3)
    public Result update(@RequestBody TCruiseResult tCruiseResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.update(tCruiseResult));
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
    @GetMapping(value = "/selectByPrimaryId")
    @Logs(title = "查询",content = "根据用户传递的参数查询巡检任务结果信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "taskResultId", required = true) String taskResultId) {
        Result result = new Result();
        try {
            TCruiseResult tCruiseResult = tCruiseResultService.selectByPrimaryId(taskResultId);
            result.setData(tCruiseResult);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @GetMapping(value = "/select")
    @Logs(title = "查询",content = "根据用户传递的参数查询巡检任务结果信息",logType = 1)
    public Result select(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                         @RequestParam(value = "taskId", required = false) String taskId,
                         @RequestParam(value = "taskName", required = false) String taskName,
                         @RequestParam(value = "areaId", required = false) String areaId,
                         @RequestParam(value = "cType", required = false) Integer cType,
                         @RequestParam(value = "cState", required = false) Integer cState,
                         @RequestParam(value = "modifyState", required = false) Integer modifyState,
                         @RequestParam(value = "taskCount", required = false) Integer taskCount,
                         @RequestParam(value = "taskWait", required = false) Integer taskWait,
                         @RequestParam(value = "checkUser", required = false) String checkUser,
                         @RequestParam(value = "checkDate", required = false) Date checkDate,
                         @RequestParam(value = "weather", required = false) String weather,
                         @RequestParam(value = "createTime", required = false) Date createTime,
                         @RequestParam(value = "executeTime", required = false) Date executeTime,
                         @RequestParam(value = "taskCode", required = false) String taskCode,
                         @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TCruiseResult> list = tCruiseResultService.select(taskResultId, taskId,taskName, areaId, cType, cState, modifyState, taskCount, taskWait, checkUser, checkDate, weather, createTime, executeTime, taskCode, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询--巡视结果任务查询")
    @GetMapping(value = "/selectTaskByPage")
    @Logs(title = "查询",content = "根据用户传递的参数分页查询巡检任务结果信息",logType = 1)
    public Result selectTaskByPage(@RequestParam(value = "taskName", required = false) String taskName,
                                   @RequestParam(value = "cState", required = false) Integer cState,
                                   @RequestParam(value = "cType", required = false) Integer cType,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {

        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCruiseResultExpand> list = tCruiseResultService.selectTaskByPage(taskName,cState,cType);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视任务结果统计")
    @GetMapping(value = "/taskStatistical")
    @Logs(title = "查询",content = "根据用户传递的参数统计巡视任务结果",logType = 1)
    public Result taskStatistical() {
        Result result = new Result();
        try {
            List<StatisticalResult> taskStatisticalList = tCruiseResultService.taskStatistical();
            result.setData(taskStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视点结果统计")
    @GetMapping(value = "/cruiseStatistical")
    @Logs(title = "查询",content = "根据用户传递的参数统计巡视点结果",logType = 1)
    public Result cruiseStatistical() {
        Result result = new Result();
        try {
            List<CruiseStatistical> cruiseStatisticalList = tCruiseResultService.cruiseStatistical();
            result.setData(cruiseStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视点结果统计--根据正常异常状态统计")
    @GetMapping(value = "/cruiseStatisticalByStatus")
    @Logs(title = "查询",content = "根据用户传递的参数统计正异常的巡视点",logType = 1)
    public Result cruiseStatisticalByStatus() {
        Result result = new Result();
        try {
            List<StatisticalTools> statisticalToolsList = tCruiseResultService.cruiseStatisticalByStatus();
            result.setData(statisticalToolsList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视点结果统计--根据异常分类统计")
    @GetMapping(value = "/cruiseStatisticalByAbnormal")
    @Logs(title = "查询",content = "根据用户传递的参数根据异常的分类进行统计",logType = 1)
    public Result cruiseStatisticalByAbnormal() {
        Result result = new Result();
        try {
            List<CruiseStatistical> cruiseStatisticalList = tCruiseResultService.cruiseStatisticalByAbnormal();
            result.setData(cruiseStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "分页查询--巡视结果任务详情查询")
    @GetMapping(value = "/selectCruiseByPage")
    @Logs(title = "查询",content = "根据用户传递的参数查询巡视结果任务详情",logType = 1)
    public Result selectCruiseByPage(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                                     @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                     @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult,
                                     @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                     @RequestParam(value = "startTime",required = false) String startTime,
                                     @RequestParam(value = "endTime",required = false) String endTime,
                                     @RequestParam(value = "regionId",required = false) Long regionId,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
            resultMap = tCruiseResultService.selectCruiseByPage(taskResultId,cruiseType,cruiseResult,deviceType,startTime,endTime,regionId,pageNum,pageSize);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "人工复核")
    @PutMapping(value = "/manualReview")
    @Logs(title = "操作",content = "人工复核",logType = 5)
    public Result manualReview(@RequestBody CruiseManualReview cruiseManualReview,HttpServletRequest request) {

        String userId = request.getHeader("userId");
        log.info("userId是："+userId);

        Result result = new Result();
        try {
            result.setData(tCruiseResultService.manualReview(cruiseManualReview,userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "批量插入")
    @PostMapping(value = "/batchInsert")
    @Logs(title = "批量插入",content = "根据用户传递的参数批量插入数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCruiseResult> list) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：",e);
        }
        return result;
    }

    @ApiOperation(value = "A-查询正在执行的任务")
    @GetMapping(value = "/selectTaskIsRunning")
    @Logs(title = "查询",content = "根据用户传递的参数查询正在执行的任务",logType = 1)
    public Result selectTaskIsRunning(){
        Result result=new Result();
        try{
            result.setData(tCruiseResultService.selectTaskIsRunning());
        }catch(Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述",e);
        }

        return result;
    }
}
