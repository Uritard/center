package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.task.service.TCruiseTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wf
 * @since 2020-08-19
 */
@RestController
@RequestMapping("/tCruiseTask/v1")
@Api(value = "/tCruiseTask", description = "巡检任务表操作接口")
public class TCruiseTaskController {

    @Autowired
    private final TCruiseTaskService tCruiseTaskService;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskController.class);

    public TCruiseTaskController(TCruiseTaskService tCruiseTaskService) {
        this.tCruiseTaskService = tCruiseTaskService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Result insert(@RequestBody TCruiseTask tCruiseTask) {
        Result result = new Result();

        try {
            result.setData(tCruiseTaskService.insert(tCruiseTask));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            log.error("添加巡检任务错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "TaskId", required = true) String TaskId) {
        Result result = new Result();

        try {
            result.setData(tCruiseTaskService.deleteByPrimaryId(TaskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.DELETEERROR.getCode(), e.getMessage());
            log.error("巡检任务删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.DELETEERROR.getCode(), ResultCodeEnum.DELETEERROR.getName());
            log.error("巡检任务删除错误:", e);
        }
        return result;
    }

    //更新
    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCruiseTask tCruiseTask) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskService.update(tCruiseTask));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新巡检任务参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新巡检任务参数错误:", e);
        }
        return result;
    }

    //查询 根据主键ID查询
    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "TaskId", required = true) String TaskId){
        Result result = new Result();

        try {
            result.setData(tCruiseTaskService.selectByPrimaryId(TaskId));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //查询
    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "TaskId", required = false) String TaskId,
                         @RequestParam(value = "PlanId", required = false) Long PlanId,
                         @RequestParam(value = "AreaId", required = false) String AreaId,
                         @RequestParam(value = "Name", required = false) String Name,
                         @RequestParam(value = "Type", required = false) Integer Type,
                         @RequestParam(value = "IfRun", required = false) Integer IfRun,
                         @RequestParam(value = "RobotId", required = false) Long RobotId,
                         @RequestParam(value = "Datetype", required = false) Integer Datetype,
                         @RequestParam(value = "Remark1", required = false) Integer Remark1,
                         @RequestParam(value = "TaskType", required = false) Integer TaskType,
                         @RequestParam(value = "StartTime", required = false) Date StartTime,
                         @RequestParam(value = "CreateTime", required = false) Date CreateTime
    ){
        Result result = new Result();
        try {
            List<TCruiseTask> list = this.tCruiseTaskService.select(TaskId,PlanId,AreaId,Name,Type,IfRun,RobotId,
                                                 Datetype,Remark1,TaskType,StartTime,CreateTime);
            result.setData(list);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //分页查询
    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruiseTask tCruiseTask,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruiseTask> list = tCruiseTaskService.selectByPage(tCruiseTask);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
