package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TUnionTask;
import com.yjh.platform.module.task.service.TUnionTaskService;
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
@RequestMapping("/tUnionTask/v1")
@Api(value = "/tUnionTask", description = "联合巡视预案操作接口")
public class TUnionTaskController {

    @Autowired
    private final TUnionTaskService tUnionTaskService;

    private Logger log = LoggerFactory.getLogger(TUnionTaskController.class);

    public TUnionTaskController(TUnionTaskService tUnionTaskService) {
        this.tUnionTaskService = tUnionTaskService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TUnionTask tUnionTask) {
        Result result = new Result();

        try {
            result.setData(tUnionTaskService.insert(tUnionTask));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            log.error("添加巡检预案错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "UnionId", required = true) String UnionId) {
        Result result = new Result();

        try {
            result.setData(tUnionTaskService.deleteByPrimaryId(UnionId));
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
    public Result update(@RequestBody TUnionTask tUnionTask) {
        Result result = new Result();
        try {
            result.setData(tUnionTaskService.update(tUnionTask));
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
    public Result selectByPrimaryId(@RequestParam(value = "TaskId", required = true) String TaskId){
        Result result = new Result();

        try {
            result.setData(tUnionTaskService.selectByPrimaryId(TaskId));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //查询
    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "UnionId", required = false) String UnionId,
                         @RequestParam(value = "PlanId", required = false) Long PlanId,
                         @RequestParam(value = "AreaId", required = false) String AreaId,
                         @RequestParam(value = "Name", required = false) String Name,
                         @RequestParam(value = "Type", required = false) Integer Type,
                         @RequestParam(value = "IfRun", required = false) Integer IfRun,
                         @RequestParam(value = "RobotId", required = false) Long RobotId,
                         @RequestParam(value = "Datetype", required = false) Integer Datetype,
                         @RequestParam(value = "Remark1", required = false) Integer Remark1,
                         @RequestParam(value = "Remark2", required = false) Integer Remark2,
                         @RequestParam(value = "Remark3", required = false) Integer Remark3,
                         @RequestParam(value = "TaskType", required = false) Integer TaskType,
                         @RequestParam(value = "StartTime", required = false) Date StartTime,
                         @RequestParam(value = "CreateTime", required = false) Date CreateTime
    ){
        Result result = new Result();
        try {
            List<TUnionTask> list = this.tUnionTaskService.select(UnionId,PlanId,AreaId,Name,Type,IfRun,RobotId,
                                                 Datetype,Remark1,Remark2,Remark3,TaskType,StartTime,CreateTime);
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
    public Result selectByPage(@RequestBody TUnionTask tUnionTask,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TUnionTask> list = tUnionTaskService.selectByPage(tUnionTask);
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
