package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.service.TCruiseTaskResultDetailService;
import com.yjh.platform.module.task.entity.TCruiseTaskResultDetail;
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
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCruiseTaskResultDetail/v1")
@Api(value = "/tCruiseTaskResultDetail", description = "任务点状态详细表操作接口")
public class TCruiseTaskResultDetailController {

    @Autowired
    private final TCruiseTaskResultDetailService tCruiseTaskResultDetailService;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskResultDetailController.class);

    public TCruiseTaskResultDetailController(TCruiseTaskResultDetailService tCruiseTaskResultDetailService) {
        this.tCruiseTaskResultDetailService = tCruiseTaskResultDetailService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增任务点状态详细数据",content = "根据用户传递的参数新增任务点状态详细数据",logType = 2)
    public Result insert(@Validated @RequestBody TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskResultDetailService.insert(tCruiseTaskResultDetail));
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
    @Logs(title = "删除任务点状态详细数据",content = "根据用户传递的参数删除任务点状态详细数据",logType = 4)
    public Result delete(@RequestParam(value = "cruiseResultId", required = true) String cruiseResultId) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskResultDetailService.deleteByPrimaryId(cruiseResultId));
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改任务点状态详细数据",content = "根据用户传递的参数修改任务点状态详细数据",logType = 3)
    public Result update(@Validated @RequestBody TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskResultDetailService.update(tCruiseTaskResultDetail));
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
    @Logs(title = "查询任务点状态详细数据",content = "根据用户传递的参数查询任务点状态详细信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "cruiseResultId", required = true) String cruiseResultId) {
        Result result = new Result();
        try {
            TCruiseTaskResultDetail tCruiseTaskResultDetail = tCruiseTaskResultDetailService.selectByPrimaryId(cruiseResultId);
            result.setData(tCruiseTaskResultDetail);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询任务点状态详细数据",content = "根据用户传递的参数查询任务点状态详细信息",logType = 1)
    public Result select(@RequestParam(value = "cruiseResultId", required = false) String cruiseResultId,
                            @RequestParam(value = "taskResultId", required = false) String taskResultId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                         @RequestParam(value = "deviceName", required = false) String deviceName,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                         @RequestParam(value = "instanceName", required = false) String instanceName,
                            @RequestParam(value = "cruiseTime", required = false) Date cruiseTime,
                            @RequestParam(value = "endTime", required = false) Date endTime,
                            @RequestParam(value = "cruiseStatus", required = false) Integer cruiseStatus,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TCruiseTaskResultDetail> list = tCruiseTaskResultDetailService.select(cruiseResultId, taskResultId, deviceId,deviceName, instanceId,instanceName, cruiseTime, endTime, cruiseStatus, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询任务点状态详细数据",content = "根据用户传递的参数分页查询任务点状态详细信息",logType = 1)
    public Result selectByPage(@RequestBody TCruiseTaskResultDetail tCruiseTaskResultDetail
                                ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruiseTaskResultDetail.getPageNum()!=null?tCruiseTaskResultDetail.getPageNum():1, tCruiseTaskResultDetail.getPageSize()!=null?tCruiseTaskResultDetail.getPageSize():0,true,null,true);
            List<TCruiseTaskResultDetail> list = tCruiseTaskResultDetailService.selectByPage(tCruiseTaskResultDetail);
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
    @Logs(title = "批量插入任务点状态详细数据",content = "根据用户传递的参数批量插入任务点状态详细数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCruiseTaskResultDetail> list) {
        Result result = new Result();
        try {
        result.setData(tCruiseTaskResultDetailService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
