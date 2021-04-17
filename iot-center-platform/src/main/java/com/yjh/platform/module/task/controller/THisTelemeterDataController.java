package com.yjh.platform.module.task.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.entity.TUnionInfo;
import com.yjh.platform.module.task.entity.UnionTaskInfo;
import com.yjh.platform.module.task.service.THisTelemeterDataService;
import com.yjh.platform.module.task.entity.THisTelemeterData;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/tHisTelemeterData/v1")
@Api(value = "/tHisTelemeterData", description = "遥测历史数据表操作接口")
public class THisTelemeterDataController {

    @Autowired
    private final THisTelemeterDataService tHisTelemeterDataService;

    private Logger log = LoggerFactory.getLogger(THisTelemeterDataController.class);

    public THisTelemeterDataController(THisTelemeterDataService tHisTelemeterDataService) {
        this.tHisTelemeterDataService = tHisTelemeterDataService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增遥测历史数据",content = "根据用户传递的参数新增遥测历史数据",logType = 2)
    public Result insert( @Validated @RequestBody THisTelemeterData tHisTelemeterData) {
        Result result = new Result();
        try {
            result.setData(tHisTelemeterDataService.insert(tHisTelemeterData));
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
    @Logs(title = "删除遥测历史数据",content = "根据用户传递的参数删除遥测历史数据",logType = 4)
    public Result delete(@RequestParam(value = "id")Long id) {
        Result result = new Result();
        try {
            result.setData(tHisTelemeterDataService.deleteByPrimaryId(id));
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
    @Logs(title = "修改遥测历史数据",content = "根据用户传递的参数修改遥测历史数据",logType = 3)
    public Result update(@RequestBody THisTelemeterData tHisTelemeterData) {
        Result result = new Result();
        try {
            result.setData(tHisTelemeterDataService.update(tHisTelemeterData));
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
    @Logs(title = "查询遥测历史数据",content = "根据用户传递的参数查询信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "id")Long id) {
        Result result = new Result();
        try {
            THisTelemeterData tHisTelemeterData = tHisTelemeterDataService.selectByPrimaryId(id);
            result.setData(tHisTelemeterData);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询遥测历史数据",content = "根据用户传递的参数查询信息",logType = 1)
    public Result select(@RequestParam(value = "id",required = false)Long id,
                            @RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "recordTime", required = false) Date recordTime,
                            @RequestParam(value = "meteKind", required = false) Integer meteKind,
                            @RequestParam(value = "meteValue", required = false) String meteValue,
                            @RequestParam(value = "lastMeteValue", required = false) String lastMeteValue) {
        Result result = new Result();
        try {
            List<THisTelemeterData> list = tHisTelemeterDataService.select(id, meteId, deviceId, recordTime, meteKind, meteValue, lastMeteValue);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    @Logs(title = "查询遥测历史数据",content = "根据用户传递的参数分页查询信息",logType = 1,authority = "1235")
    public Result selectByPage(@RequestParam(value = "startDate", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                               @RequestParam(value = "endDate", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,
                               @RequestParam(value = "meteKind", required = false) Integer meteKind,
                               @RequestParam(value = "deviceName", required = false) String deviceName,
                               @RequestParam(value = "meteName", required = false) String meteName,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TUnionInfo> list = tHisTelemeterDataService.selectAll(startDate, endDate, meteKind, deviceName,meteName);
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
    @Logs(title = "批量插入遥测历史数据",content = "根据用户传递的参数批量插入遥测历史数据",logType = 2)
    public Result batchInsert(@RequestBody List<THisTelemeterData> list) {
        Result result = new Result();
        try {
        result.setData(tHisTelemeterDataService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询联动任务信息")
    @RequestMapping(value = "/selectUnionTask", method = RequestMethod.GET)
    @Logs(title = "查询联动任务信息",content = "根据用户传递的参数查询联动任务信息",logType = 1,authority = "1235")
    public Result selectUnionTask(@RequestParam(value = "startDate", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                  @RequestParam(value = "endDate", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,
                                  @RequestParam(value = "deviceName", required = false) String deviceName,
                                  @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        //@RequestParam(value = "meteKind", required = false) Integer meteKind,
        //@RequestParam(value = "meteName", required = false) String meteName,
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<UnionTaskInfo> list = tHisTelemeterDataService.selectUnionTask(startDate, endDate,deviceName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询联动任务信息失败描述：", e);
        }
        return result;
    }

}
