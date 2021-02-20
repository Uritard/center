package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.LinkageMonitorData;
import com.yjh.platform.module.task.entity.TUnionTask;
import com.yjh.platform.module.task.entity.TUnionTaskExpand;
import com.yjh.platform.module.task.entity.LinkageInformation;
import com.yjh.platform.module.task.service.TUnionTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    @Logs(title = "新增巡检任务数据",content = "根据用户传递的参数新增巡检任务数据",logType = 2)
    public Result insert(@Validated  @RequestBody TUnionTask tUnionTask) {
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
    @Logs(title = "删除巡检任务数据",content = "根据用户传递的参数删除巡检任务数据",logType = 4)
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
    @Logs(title = "修改巡检任务数据",content = "根据用户传递的参数修改巡检任务数据",logType = 3)
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
    @Logs(title = "查询巡检任务数据",content = "根据用户传递的参数查询巡检任务信息",logType = 1)
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
    @Logs(title = "查询巡检任务数据",content = "根据用户传递的参数查询巡检任务信息",logType = 1)
    public Result select(@RequestParam(value = "unionId", required = false) String unionId,
                         @RequestParam(value = "ruleId", required = false) Long ruleId,
                         @RequestParam(value = "unionName", required = false) String unionName,
                         @RequestParam(value = "ruleDelay", required = false) Integer ruleDelay,
                         @RequestParam(value = "isFinish", required = false) Integer isFinish,
                         @RequestParam(value = "robotId", required = false) Long robotId,
                         @RequestParam(value = "meteId", required = false) Long meteId,
                         @RequestParam(value = "triggeringTime", required = false) Date triggeringTime,
                         @RequestParam(value = "ruleName", required = false) String ruleName,
                         @RequestParam(value = "paramValues", required = false) String paramValues,
                         @RequestParam(value = "startTime", required = false) Date startTime,
                         @RequestParam(value = "createTime", required = false) Date createTime,
                         @RequestParam(value = "planName", required = false) String planName,
                         @RequestParam(value = "ruleContent", required = false) String ruleContent) {
        Result result = new Result();
        try {
            List<TUnionTask> list = tUnionTaskService.select(unionId, ruleId, unionName, ruleDelay, isFinish, robotId, meteId,
                    triggeringTime, ruleName, paramValues, startTime, createTime,planName,ruleContent);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询巡检任务数据",content = "根据用户传递的参数分页查询巡检任务信息",logType = 1)
    public Result selectByPage(@RequestBody TUnionTask tUnionTask,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
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
    @Logs(title = "批量插入巡检任务数据",content = "根据用户传递的参数批量插入巡检任务数据",logType = 2)
    public Result batchInsert(@Validated @RequestBody List<TUnionTask> list) {
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
    @Logs(title = "查看联动历史记录",content = "根据用户传递的参数查询联动历史记录",logType = 1)
    public Result selectHistory(@RequestParam(value = "ruleName", required = false) String ruleName,
                                @RequestParam(value = "endDate", required = false) String endDate,
                                @RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date endDateTemp = new Date();
        Date startDateTemp = new Date();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            if (("".equals(endDate) && "".equals(startDate)) || (endDate == null && startDate == null) ) {
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
            log.error("查看失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "联动历史记录统计")
    @RequestMapping(value = "/historyStatistical", method = RequestMethod.GET)
    @Logs(title = "联动历史记录统计",content = "根据用户传递的参数统计联动历史记录",logType = 1)
    public Result historyStatistical(@RequestParam(value = "dateMarked")String dateMarked) {
        Result result = new Result();
        try {
            if (dateMarked.equals("recentWeek")){
                result.setData(tUnionTaskService.historyStatisticalByWeek());
            }else if (dateMarked.equals("recentYear")){
                result.setData(tUnionTaskService.historyStatisticalByYear());
            }else if (dateMarked.equals("recentMonth")){
                result.setData(tUnionTaskService.historyStatisticalByMonth());
            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("统计失败：" + e);
        }
        return result;
    }
    @ApiOperation(value = "联动弹框--联动信息")
    @RequestMapping(value = "/linkageInformation",method = RequestMethod.GET)
    @Logs(title = "联动弹框",content = "联动弹窗滚动联动信息",logType = 5)
    public Result linkageInformation(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            log.info("linkageInformation的taskId是==="+taskId);
            LinkageInformation linkageInformation = tUnionTaskService.linkageInformation(taskId);
            result.setData(linkageInformation);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询联动弹窗内容失败：", e);
        }
        return result;
    }
    @ApiOperation(value = "联动弹框--监测数据")
    @RequestMapping(value = "/linkageMonitorData",method = RequestMethod.GET)
    @Logs(title = "联动弹框",content = "联动弹窗滚动检测数据信息",logType = 5)
    public Result linkageMonitorData(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            log.info("linkageMonitorData的taskId是==="+taskId);
            List<LinkageMonitorData> linkageMonitorDataList = tUnionTaskService.linkageMonitorData(taskId);
            result.setData(linkageMonitorDataList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询联动弹窗内容失败：", e);
        }
        return result;
    }
}
