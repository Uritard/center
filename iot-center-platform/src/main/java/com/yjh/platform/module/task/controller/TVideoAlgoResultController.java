package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.service.TVideoAlgoResultService;
import com.yjh.platform.module.task.entity.TVideoAlgoResult;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
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
 * @since 2020-08-24
 */
@RestController
@RequestMapping("/tVideoAlgoResult/v1")
@Api(value = "/tVideoAlgoResult", description = "视频轮训任务结果表操作接口")
public class TVideoAlgoResultController {

    @Autowired
    private final TVideoAlgoResultService tVideoAlgoResultService;

    private Logger log = LoggerFactory.getLogger(TVideoAlgoResultController.class);

    public TVideoAlgoResultController(TVideoAlgoResultService tVideoAlgoResultService) {
        this.tVideoAlgoResultService = tVideoAlgoResultService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增视频轮训任务结果",content = "根据用户传递的参数新增视频轮训任务结果数据",logType = 2)
    public Result insert( @Validated @RequestBody TVideoAlgoResult tVideoAlgoResult) {
        Result result = new Result();
        try {
            result.setData(tVideoAlgoResultService.insert(tVideoAlgoResult));
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
    @Logs(title = "删除视频轮训任务结果",content = "根据用户传递的参数删除视频轮训任务结果数据",logType = 4)
    public Result delete(@RequestParam(value = "id")Long id) {
        Result result = new Result();
        try {
            result.setData(tVideoAlgoResultService.deleteByPrimaryId(id));
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
    @Logs(title = "修改视频轮训任务结果",content = "根据用户传递的参数修改视频轮训任务结果数据",logType = 3)
    public Result update(@Validated @RequestBody TVideoAlgoResult tVideoAlgoResult) {
        Result result = new Result();
        try {
            result.setData(tVideoAlgoResultService.update(tVideoAlgoResult));
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
    @Logs(title = "查询视频轮训任务结果",content = "根据用户传递的参数查询视频轮训任务结果信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "id")Long id) {
        Result result = new Result();
        try {
            TVideoAlgoResult tVideoAlgoResult = tVideoAlgoResultService.selectByPrimaryId(id);
            result.setData(tVideoAlgoResult);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询视频轮训任务结果",content = "根据用户传递的参数查询视频轮训任务结果信息",logType = 1)
    public Result select(@RequestParam(value = "pointId", required = false) Long pointId,
                            @RequestParam(value = "taskId", required = false) String taskId,
                            @RequestParam(value = "planId", required = false) Long planId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "presetId", required = false) Long presetId,
                            @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                            @RequestParam(value = "picUrl", required = false) String picUrl,
                            @RequestParam(value = "cusId", required = false) String cusId,
                            @RequestParam(value = "algorithmId", required = false) Long algorithmId,
                            @RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "analyseResult", required = false) String analyseResult,
                            @RequestParam(value = "picOrignal", required = false) String picOrignal,
                            @RequestParam(value = "evaluationState", required = false) Integer evaluationState,
                            @RequestParam(value = "signpic", required = false) String signpic,
                            @RequestParam(value = "algorithmType", required = false) String algorithmType,
                            @RequestParam(value = "algorithmSonType", required = false) String algorithmSonType,
                            @RequestParam(value = "executeTime", required = false) Date executeTime,
                            @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TVideoAlgoResult> list = tVideoAlgoResultService.select(pointId, taskId, planId, deviceId, presetId, deviceMeteId, picUrl, cusId, algorithmId, status, analyseResult, picOrignal, evaluationState, signpic, algorithmType, algorithmSonType, executeTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询视频轮训任务结果",content = "根据用户传递的参数分页查询视频轮训任务结果信息",logType = 1)
    public Result selectByPage(@RequestBody TVideoAlgoResult tVideoAlgoResult
                                ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tVideoAlgoResult.getPageNum()!=null?tVideoAlgoResult.getPageNum():1, tVideoAlgoResult.getPageSize()!=null?tVideoAlgoResult.getPageSize():0,true,null,true);
            List<TVideoAlgoResult> list = tVideoAlgoResultService.selectByPage(tVideoAlgoResult);
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
    @Logs(title = "批量插入视频轮训任务结果",content = "根据用户传递的参数批量插入数据",logType = 2)
    public Result batchInsert(@RequestBody List<TVideoAlgoResult> list) {
        Result result = new Result();
        try {
        result.setData(tVideoAlgoResultService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
