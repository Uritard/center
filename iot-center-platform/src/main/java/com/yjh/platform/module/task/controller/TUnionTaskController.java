package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.TUnionTaskExpand;
import com.yjh.platform.module.task.service.TUnionTaskService;
import com.yjh.platform.module.task.entity.TUnionTask;

import java.text.SimpleDateFormat;
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
 * @author tt
 * @since 2020-09-04
 */
@RestController
@RequestMapping("/tUnionTask/v1")
@Api(value = "/tUnionTask", description = "巡检任务表操作接口")
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
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加巡检任务表错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "unionId", required = true) String unionId) {
        Result result = new Result();
        try {
            result.setData(tUnionTaskService.deleteByPrimaryId(unionId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除巡检任务表异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TUnionTask tUnionTask) {
        Result result = new Result();
        try {
            result.setData(tUnionTaskService.update(tUnionTask));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新巡检任务表异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "unionId", required = true) String unionId) {
        Result result = new Result();
        try {
            TUnionTask tUnionTask = tUnionTaskService.selectByPrimaryId(unionId);
            result.setData(tUnionTask);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "unionId", required = false) String unionId,
                            @RequestParam(value = "ruleId", required = false) Long ruleId,
                            @RequestParam(value = "unionName", required = false) String unionName,
                            @RequestParam(value = "ruleDelay", required = false) Integer ruleDelay,
                            @RequestParam(value = "isFinish", required = false) Integer isFinish,
                            @RequestParam(value = "robotId", required = false) Long robotId,
                            @RequestParam(value = "remark1", required = false) Integer remark1,
                            @RequestParam(value = "remark2", required = false) Integer remark2,
                            @RequestParam(value = "remark3", required = false) String remark3,
                            @RequestParam(value = "paramValues", required = false) String paramValues,
                            @RequestParam(value = "startTime", required = false) Date startTime,
                            @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TUnionTask> list = tUnionTaskService.select(unionId, ruleId, unionName, ruleDelay, isFinish, robotId, remark1,
                    remark2, remark3, paramValues, startTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TUnionTask tUnionTask,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TUnionTask> list = tUnionTaskService.selectByPage(tUnionTask);
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
    public Result batchInsert(@RequestBody List<TUnionTask> list) {
        Result result = new Result();
        try {
        result.setData(tUnionTaskService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }
    @ApiOperation(value = "查看联动历史记录")
    @RequestMapping(value = "/selectHistory", method = RequestMethod.GET)
    public Result selectHistory(@RequestParam(value = "ruleName", required = false) String ruleName,
                                @RequestParam(value = "endDate", required = false) String endDate,
                                @RequestParam(value = "startDate", required = false) String startDate,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date endDateTemp;
        Date startDateTemp;
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            if ("".equals(endDate) && "".equals(startDate)) {
                endDateTemp = null;
                startDateTemp = null;
            } else {
                endDateTemp = simpleDateFormat.parse(endDate);
                startDateTemp = simpleDateFormat.parse(startDate);
            }
            List<TUnionTaskExpand> list = tUnionTaskService.selectHistory(ruleName,endDateTemp,startDateTemp);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "联动历史记录统计")
    @RequestMapping(value = "/historyStatistical", method = RequestMethod.GET)
    public Result historyStatistical() {
        Result result = new Result();
        try {
            result.setData(tUnionTaskService.historyStatistical());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }
}
