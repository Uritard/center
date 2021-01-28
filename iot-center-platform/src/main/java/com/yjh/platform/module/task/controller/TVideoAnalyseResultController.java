package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.service.TVideoAnalyseResultService;
import com.yjh.platform.module.task.entity.TVideoAnalyseResult;
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
@RequestMapping("/tVideoAnalyseResult/v1")
@Api(value = "/tVideoAnalyseResult", description = "算法结果表操作接口")
public class TVideoAnalyseResultController {

    @Autowired
    private final TVideoAnalyseResultService tVideoAnalyseResultService;

    private Logger log = LoggerFactory.getLogger(TVideoAnalyseResultController.class);

    public TVideoAnalyseResultController(TVideoAnalyseResultService tVideoAnalyseResultService) {
        this.tVideoAnalyseResultService = tVideoAnalyseResultService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增",content = "根据用户传递的参数新增算法结果数据",logType = 2)
    public Result insert(@RequestBody TVideoAnalyseResult tVideoAnalyseResult) {
        Result result = new Result();
        try {
            result.setData(tVideoAnalyseResultService.insert(tVideoAnalyseResult));
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
    @Logs(title = "删除",content = "根据用户传递的参数删除算法结果数据",logType = 4)
    public Result delete(@RequestParam(value = "algorithmResultId", required = true) Long algorithmResultId) {
        Result result = new Result();
        try {
            result.setData(tVideoAnalyseResultService.deleteByPrimaryId(algorithmResultId));
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
    @Logs(title = "修改",content = "根据用户传递的参数修改算法结果数据",logType = 3)
    public Result update(@RequestBody TVideoAnalyseResult tVideoAnalyseResult) {
        Result result = new Result();
        try {
            result.setData(tVideoAnalyseResultService.update(tVideoAnalyseResult));
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
    @Logs(title = "查询",content = "根据用户传递的参数查询算法结果信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "algorithmResultId", required = true) Long algorithmResultId) {
        Result result = new Result();
        try {
            TVideoAnalyseResult tVideoAnalyseResult = tVideoAnalyseResultService.selectByPrimaryId(algorithmResultId);
            result.setData(tVideoAnalyseResult);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询算法结果信息",logType = 1)
    public Result select(@RequestParam(value = "algorithmResultId", required = false) Long algorithmResultId,
                            @RequestParam(value = "analyseTime", required = false) Date analyseTime,
                            @RequestParam(value = "analyseConfId", required = false) String analyseConfId,
                            @RequestParam(value = "algorithmResult", required = false) String algorithmResult,
                            @RequestParam(value = "algorithmPicture", required = false) String algorithmPicture,
                            @RequestParam(value = "algorithmStatus", required = false) Integer algorithmStatus,
                            @RequestParam(value = "resultRate", required = false) String resultRate,
                            @RequestParam(value = "resultDescribe", required = false) String resultDescribe,
                            @RequestParam(value = "reserver", required = false) Integer reserver) {
        Result result = new Result();
        try {
            List<TVideoAnalyseResult> list = tVideoAnalyseResultService.select(algorithmResultId, analyseTime, analyseConfId, algorithmResult, algorithmPicture, algorithmStatus, resultRate, resultDescribe, reserver);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询",content = "根据用户传递的参数分页查询算法结果信息",logType = 1)
    public Result selectByPage(@RequestBody TVideoAnalyseResult tVideoAnalyseResult,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TVideoAnalyseResult> list = tVideoAnalyseResultService.selectByPage(tVideoAnalyseResult);
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
    @Logs(title = "批量插入",content = "根据用户传递的参数批量插入算法结果数据",logType = 2)
    public Result batchInsert(@RequestBody List<TVideoAnalyseResult> list) {
        Result result = new Result();
        try {
        result.setData(tVideoAnalyseResultService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
