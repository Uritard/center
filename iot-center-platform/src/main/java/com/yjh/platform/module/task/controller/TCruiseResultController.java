package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TCruiseResultService;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
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
@RequestMapping("/tCruiseResult/v1")
@Api(value = "/tCruiseResult", description = "巡检任务结果表操作接口")
public class TCruiseResultController {

    @Autowired
    private final TCruiseResultService tCruiseResultService;

    private Logger log = LoggerFactory.getLogger(TCruiseResultController.class);

    public TCruiseResultController(TCruiseResultService tCruiseResultService) {
        this.tCruiseResultService = tCruiseResultService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
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
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
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
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
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
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
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
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                         @RequestParam(value = "taskId", required = false) String taskId,
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
            List<TCruiseResult> list = tCruiseResultService.select(taskResultId, taskId, areaId, cType, cState, modifyState, taskCount, taskWait, checkUser, checkDate, weather, createTime, executeTime, taskCode, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询--巡视结果确认")
    @RequestMapping(value = "/selectTaskByPage", method = RequestMethod.GET)
    public Result selectTaskByPage(@RequestParam(value = "taskName", required = false) String taskName,
                                   @RequestParam(value = "cState", required = false) Integer cState,
                                   @RequestParam(value = "cType", required = false) Integer cType,

                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {

        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
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
    @RequestMapping(value = "/taskStatistical", method = RequestMethod.GET)
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
    @RequestMapping(value = "/cruiseStatistical", method = RequestMethod.GET)
    public Result cruiseStatistical() {
        Result result = new Result();
        try {
            List<StatisticalResult> cruiseStatisticalList = tCruiseResultService.cruiseStatistical();
            result.setData(cruiseStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "分页查询--任务结果详细")
    @RequestMapping(value = "/selectCruiseByPage", method = RequestMethod.GET)
    public Result selectCruiseByPage(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                                     @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                     @RequestParam(value = "state", required = false) Integer state,
                                     @RequestParam(value = "deviceName", required = false) String deviceName,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<CruiseResultDetail> list = tCruiseResultService.selectCruiseByPage(taskResultId,cruiseType,state,deviceName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "人工复核")
    @RequestMapping(value = "/manualReview", method = RequestMethod.PUT)
    public Result manualReview(@RequestBody CruiseManualReview cruiseManualReview,HttpServletRequest request) {

        String userId = request.getHeader("userId");
        System.out.println("userId是："+userId);

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
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TCruiseResult> list) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "A-查询正在执行的任务")
    @RequestMapping(value = "/selectTaskIsRunning",method = RequestMethod.GET)
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
