package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.service.TCruiseTaskAttrService;
import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import java.util.HashMap;
import java.util.List;

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
 * @author tt
 * @since 2020-09-04
 */
@RestController
@RequestMapping("/tCruiseTaskAttr/v1")
@Api(value = "/tCruiseTaskAttr", description = "任务关联表操作接口")
public class TCruiseTaskAttrController {

    @Autowired
    private final TCruiseTaskAttrService tCruiseTaskAttrService;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskAttrController.class);

    public TCruiseTaskAttrController(TCruiseTaskAttrService tCruiseTaskAttrService) {
        this.tCruiseTaskAttrService = tCruiseTaskAttrService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增任务关联数据",content = "根据用户传递的参数新增任务关联数据",logType = 2)
    public Result insert(@Validated @RequestBody TCruiseTaskAttr tCruiseTaskAttr) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskAttrService.insert(tCruiseTaskAttr));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加任务关联表错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除任务关联数据",content = "根据用户传递的参数删除任务关联数据",logType = 4)
    public Result delete(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskAttrService.deleteByPrimaryId(taskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除任务关联表异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改任务关联数据",content = "根据用户传递的参数修改任务关联数据",logType = 3)
    public Result update(@Validated @RequestBody TCruiseTaskAttr tCruiseTaskAttr) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskAttrService.update(tCruiseTaskAttr));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新任务关联表异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询任务关联数据",content = "根据用户传递的参数查询任务关联信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            TCruiseTaskAttr tCruiseTaskAttr = tCruiseTaskAttrService.selectByPrimaryId(taskId);
            result.setData(tCruiseTaskAttr);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询任务关联数据",content = "根据用户传递的参数查询任务关联信息",logType = 1)
    public Result select(@RequestParam(value = "taskId", required = false) String taskId,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "customId", required = false) String customId,
                            @RequestParam(value = "pointTaskId", required = false) String pointTaskId,
                            @RequestParam(value = "ifRobot", required = false) Integer ifRobot,
                            @RequestParam(value = "ifVideo", required = false) Integer ifVideo,
                            @RequestParam(value = "ifInferad", required = false) Integer ifInferad,
                            @RequestParam(value = "ifArtificial", required = false) Integer ifArtificial) {
        Result result = new Result();
        try {
            List<TCruiseTaskAttr> list = tCruiseTaskAttrService.select(taskId, instanceId, deviceMeteId, deviceId, customId, pointTaskId, ifRobot, ifVideo, ifInferad, ifArtificial);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询任务关联数据",content = "根据用户传递的参数分页查询任务关联信息",logType = 1)
    public Result selectByPage(@RequestBody TCruiseTaskAttr tCruiseTaskAttr
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruiseTaskAttr.getPageNum()!=null?tCruiseTaskAttr.getPageNum():1, tCruiseTaskAttr.getPageSize()!=null?tCruiseTaskAttr.getPageSize():0,true,null,true);
            List<TCruiseTaskAttr> list = tCruiseTaskAttrService.selectByPage(tCruiseTaskAttr);
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
    @Logs(title = "批量插入关联数据",content = "根据用户传递的参数批量插入任务关联数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCruiseTaskAttr> list) {
        Result result = new Result();
        try {
        result.setData(tCruiseTaskAttrService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
