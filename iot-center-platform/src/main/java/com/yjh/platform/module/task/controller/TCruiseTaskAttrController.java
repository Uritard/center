package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import com.yjh.platform.module.task.service.TCruiseTaskAttrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wf
 * @since 2020-08-19
 */
@RestController
@RequestMapping("/tCruiseTaskAttr/v1")
@Api(value = "/tCruiseTaskAttr", description = "任务关联操作接口")
public class TCruiseTaskAttrController {

    @Autowired
    private final TCruiseTaskAttrService tCruiseTaskAttrService;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskAttrController.class);

    public TCruiseTaskAttrController(TCruiseTaskAttrService tCruiseTaskAttrService) {
        this.tCruiseTaskAttrService = tCruiseTaskAttrService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Result insert(@RequestBody TCruiseTaskAttr tCruiseTaskAttr) {
        Result result = new Result();

        try {
            result.setData(tCruiseTaskAttrService.insert(tCruiseTaskAttr));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            log.error("添加任务关联错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "TaskId", required = true) String TaskId,
                         @RequestParam(value = "InstanceId", required = true) Long InstanceId) {
        Result result = new Result();
        Map<String, Object> map = new HashMap<>();
        map.put("TaskId", TaskId);
        map.put("InstanceId", InstanceId);

        try {
            result.setData(tCruiseTaskAttrService.deleteByPrimaryId(map));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.DELETEERROR.getCode(), e.getMessage());
            log.error("任务关联删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.DELETEERROR.getCode(), ResultCodeEnum.DELETEERROR.getName());
            log.error("任务关联删除错误:", e);
        }
        return result;
    }

    //更新
    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCruiseTaskAttr tCruiseTaskAttr) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskAttrService.update(tCruiseTaskAttr));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新任务关联参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新任务关联参数错误:", e);
        }
        return result;
    }

    //查询 根据主键ID查询
    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "TaskId", required = true) String TaskId){
        Result result = new Result();

        try {
            result.setData(tCruiseTaskAttrService.selectByPrimaryId(TaskId));
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
                         @RequestParam(value = "InstanceId", required = false) Long InstanceId,
                         @RequestParam(value = "DeviceMeteId", required = false) Long DeviceMeteId,
                         @RequestParam(value = "DeviceId", required = false) Long DeviceId,
                         @RequestParam(value = "CustomId", required = false) String CustomId,
                         @RequestParam(value = "PointTaskId", required = false) String PointTaskId,
                         @RequestParam(value = "IfRobot", required = false) Integer IfRobot,
                         @RequestParam(value = "IfVideo", required = false) Integer IfVideo,
                         @RequestParam(value = "IfInferad", required = false) Integer IfInferad,
                         @RequestParam(value = "IfArtificial", required = false) Integer IfArtificial
    ){
        Result result = new Result();
        try {
            List<TCruiseTaskAttr> list = this.tCruiseTaskAttrService.select(TaskId,InstanceId,DeviceMeteId,DeviceId,CustomId,
                    PointTaskId,IfRobot,IfVideo,IfInferad,IfArtificial);
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
    public Result selectByPage(@RequestBody TCruiseTaskAttr tCruiseTaskAttr,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCruiseTaskAttr> list = tCruiseTaskAttrService.selectByPage(tCruiseTaskAttr);
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
