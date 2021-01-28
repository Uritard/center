package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.entity.CruiseResultCounter;
import com.yjh.platform.module.task.service.TCruiseTaskResultService;
import com.yjh.platform.module.task.entity.TCruiseTaskResult;

import java.text.SimpleDateFormat;
import java.util.*;

import io.swagger.annotations.*;


import org.jboss.netty.util.internal.ReusableIterator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

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
    @Logs(title = "新增",content = "根据用户传递的参数新增任务点状态数据",logType = 2)
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
    @Logs(title = "删除",content = "根据用户传递的参数删除任务点状态数据",logType = 4)
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
    @Logs(title = "修改",content = "根据用户传递的参数修改任务点状态数据",logType = 3)
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
    @Logs(title = "查询",content = "根据用户传递的参数查询任务点状态信息",logType = 1)
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
    @Logs(title = "查询",content = "根据用户传递的参数查询任务点状态信息",logType = 1)
    public Result select(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                            @RequestParam(value = "taskId", required = false) String taskId,
                         @RequestParam(value = "taskName", required = false) String taskName,
                            @RequestParam(value = "taskAbnormal", required = false) Integer taskAbnormal,
                            @RequestParam(value = "taskAlarm", required = false) Integer taskAlarm,
                            @RequestParam(value = "runExecute", required = false) String runExecute,
                            @RequestParam(value = "cruiseTaskTime", required = false) Date cruiseTaskTime,
                            @RequestParam(value = "taskStatus", required = false) Integer taskStatus,
                            @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TCruiseTaskResult> list = tCruiseTaskResultService.select(taskResultId, taskId,taskName, taskAbnormal, taskAlarm, runExecute, cruiseTaskTime, taskStatus, cruiseResult, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询",content = "根据用户传递的参数分页查询任务点状态信息",logType = 1)
    public Result selectByPage(@RequestBody TCruiseTaskResult tCruiseTaskResult,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
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
    @Logs(title = "批量插入",content = "根据用户传递的参数批量插入任务点状态数据",logType = 2)
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
    @RequestMapping(value = "/selectCurrentCruiseTaskResult",method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数获取当前任务下巡检点的执行信息",logType = 1)
    public Result selectCurrentCruiseTaskResult(@RequestParam String taskId){
        Result result=new Result();
        try {
            result.setData(tCruiseTaskResultService.selectCruiseTaskResult(taskId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }

        return result;

    }

    @ApiOperation(value = "A-获取当前执行任务的实时告警信息")
    @RequestMapping(value = "/selectRealTimeWarnInfo",method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数获取当前执行任务的实时告警信息",logType = 1)
    public Result selectRealTimeWarnInfo(@RequestParam String taskId){
        Result result=new Result();
        try {
            result.setData(tCruiseTaskResultService.realTimeWarnInfo(taskId));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败：", e);
        }
       return result;
    }


    @ApiOperation(value = "A-获取巡检任务进度")
    @RequestMapping(value = "/selectCruiseAdvance",method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数获取巡视任务进度",logType = 1)
    public Result selectCruiseAdvance(@RequestParam String taskId){
        Result result=new Result();
       try{
           result.setData(tCruiseTaskResultService.selectCruiseAdvance(taskId));
       }catch (Exception e){
           result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
           log.error("失败描述",e);
       }

        return  result;
    }

//    @ApiOperation(value = "B-展示任务采集图片阵列")
//    @RequestMapping(value = "/selectImagePosition",method = RequestMethod.GET)
//    public Result selectImagePosition(@RequestParam String taskId){
//        Result result=new Result();
//        try {
//            result.setData(tCruiseTaskResultService.selectImagePosition(taskId));
//        }catch (Exception e){
//
//        }
//        return result;
//    }

    @ApiOperation(value = "C-查询当前任务异常巡检点、未巡视巡检点、已巡视巡检点个数、运行时间")
    @RequestMapping(value = "selectCruiseStatusCount",method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询当前任务异常巡视点、为巡视巡视点、已巡视巡视点",logType = 1)
    public Result selectCruiseStatusCount(@RequestParam String taskId){
        Result result=new Result();
        try{
            CruiseResultCounter cruiseResultCounter = tCruiseTaskResultService.selectCruiseStatusCount(taskId);
            if (Objects.nonNull(cruiseResultCounter)) {
                result.setData(cruiseResultCounter);
            } else {result.setMessage("巡检结果还未返回结果");}
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述",e);
        }
        return result;

    }



    @ApiOperation(value = "C-获取当前任务下的摄像头/机器人信息以及其工作的巡检点结果状态")
    @RequestMapping(value = "/selectCruiseDeviceAndCruiseAdvance",method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数获取当前任务下的摄像头、机器人信息",logType = 1)
    public Result selectCruiseDeviceAndCruiseAdvance(@RequestParam String taskId){
        Result result=new Result();
        try{
            result.setData(tCruiseTaskResultService.selectCruiseDeviceAndCruiseAdvance(taskId));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述",e);
        }
        return  result;
    }

    @ApiOperation(value = "A-巡视结果图片比对")
    @RequestMapping(value = "/PictureCompare",method = RequestMethod.GET)
    @Logs(title = "操作",content = "根据用户传递的参数对巡视结果图片对比",logType = 5)
    public Result PictureCompare(@RequestParam(value = "taskId")String taskId,
                                 @RequestParam(value = "instanceId")Long instanceId){
        Result result=new Result();
        try{
            result.setData(tCruiseTaskResultService.PictureCompare(taskId, instanceId));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述",e);
        }
        return  result;
    }


    @ApiOperation(value = "读取缓存中的任务下巡视点绑定的摄像头信息")
    @RequestMapping(value = "/cameraInfoByRedis",method = RequestMethod.GET)
    @Logs(title = "查询",content = "读取缓存中任务下的巡视点绑定摄像头信息",logType = 1)
    public Result cameraInfoByRedis(){
        Result result=new Result();
        try{
            result.setData(tCruiseTaskResultService.cameraInfoByRedis());
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
//            String taskDate = simpleDateFormat.format(new Date());
//            Date date = null;
//            try {
//                date = simpleDateFormat.parse(taskDate);
//            } catch (Exception e) { e.getMessage(); }
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述",e);
        }
        return  result;

    }
}
