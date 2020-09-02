package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.service.TCruiseTaskResultService;
import com.yjh.platform.module.task.entity.TCruiseTaskResult;
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
@RequestMapping("/tCruiseTaskResult/v1")
@Api(value = "/tCruiseTaskResult", description = "任务点状态表操作接口")
public class TCruiseTaskResultController {

    @Autowired
    private final TCruiseTaskResultService tCruiseTaskResultService;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskResultController.class);

    public TCruiseTaskResultController(TCruiseTaskResultService tCruiseTaskResultService) {
        this.tCruiseTaskResultService = tCruiseTaskResultService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCruiseTaskResult tCruiseTaskResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskResultService.insert(tCruiseTaskResult));
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
            result.setData(tCruiseTaskResultService.deleteByPrimaryId(taskResultId));
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
    public Result update(@RequestBody TCruiseTaskResult tCruiseTaskResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskResultService.update(tCruiseTaskResult));
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
            TCruiseTaskResult tCruiseTaskResult = tCruiseTaskResultService.selectByPrimaryId(taskResultId);
            result.setData(tCruiseTaskResult);
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
                            @RequestParam(value = "taskAbnormal", required = false) Integer taskAbnormal,
                            @RequestParam(value = "taskAlarm", required = false) Integer taskAlarm,
                            @RequestParam(value = "runExecute", required = false) String runExecute,
                            @RequestParam(value = "cruiseTaskTime", required = false) Date cruiseTaskTime,
                            @RequestParam(value = "taskStatus", required = false) Integer taskStatus,
                            @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TCruiseTaskResult> list = tCruiseTaskResultService.select(taskResultId, taskId, taskAbnormal, taskAlarm, runExecute, cruiseTaskTime, taskStatus, cruiseResult, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruiseTaskResult tCruiseTaskResult,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruiseTaskResult> list = tCruiseTaskResultService.selectByPage(tCruiseTaskResult);
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
    public Result batchInsert(@RequestBody List<TCruiseTaskResult> list) {
        Result result = new Result();
        try {
        result.setData(tCruiseTaskResultService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "A-获取当前任务下的巡检点的执行信息")
    @RequestMapping(value = "/selectCruiseTaskResult",method = RequestMethod.GET)
    public Result selectCruiseTaskResult(@RequestParam Long taskId){
        Result result=new Result();
        try {
            result.setData(tCruiseTaskResultService.selectCruiseTaskResult(taskId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }

        return result;

    }
}
