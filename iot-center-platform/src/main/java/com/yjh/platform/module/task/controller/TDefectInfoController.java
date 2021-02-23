package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TDefectInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author tt
 * @since 2020-10-15
 */
@RestController
@RequestMapping("/tDefectInfo/v1")
@Api(value = "/tDefectInfo", description = "缺陷信息表操作接口")
public class TDefectInfoController {

    @Autowired
    private final TDefectInfoService tDefectInfoService;

    private Logger log = LoggerFactory.getLogger(TDefectInfoController.class);

    public TDefectInfoController(TDefectInfoService tDefectInfoService) {
        this.tDefectInfoService = tDefectInfoService;
    }
    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增陷信息数据",content = "根据用户传递的参数新增缺陷信息数据",logType = 2)
    public Result insert(@Validated  @RequestBody TDefectInfo tDefectInfo) {
        Result result = new Result();
        try {
            result.setData(tDefectInfoService.insert(tDefectInfo));
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
    @Logs(title = "删除陷信息数据",content = "根据用户传递的参数删除缺陷信息数据",logType = 4)
    public Result delete(@RequestParam(value = "defectId", required = true) Long defectId) {
        Result result = new Result();
        try {
            result.setData(tDefectInfoService.deleteByPrimaryId(defectId));
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
    @Logs(title = "修改陷信息数据",content = "根据用户传递的参数修改缺陷信息数据",logType = 3)
    public Result update(@RequestBody TDefectInfo tDefectInfo) {
        Result result = new Result();
        try {
            result.setData(tDefectInfoService.update(tDefectInfo));
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
    @Logs(title = "查询陷信息数据",content = "根据用户传递的参数查询缺陷信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "defectId", required = true) Long defectId) {
        Result result = new Result();
        try {
            TDefectInfo tDefectInfo = tDefectInfoService.selectByPrimaryId(defectId);
            result.setData(tDefectInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询陷信息数据",content = "根据用户传递的参数查询缺陷信息",logType = 1)
    public Result select(@RequestParam(value = "defectId", required = false) Long defectId,
                            @RequestParam(value = "defectLevel", required = false) Integer defectLevel,
                            @RequestParam(value = "defectTime", required = false) Date defectTime,
                            @RequestParam(value = "defectType", required = false) Integer defectType,
                            @RequestParam(value = "defectName", required = false) String defectName,
                            @RequestParam(value = "defectContent", required = false) String defectContent,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "cunstomId", required = false) String cunstomId,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "stdMeteId", required = false) Long stdMeteId,
                            @RequestParam(value = "confMode", required = false) Integer confMode,
                            @RequestParam(value = "isDefect", required = false) Integer isDefect,
                            @RequestParam(value = "dealType", required = false) Integer dealType,
                            @RequestParam(value = "dealInfo", required = false) String dealInfo,
                            @RequestParam(value = "dealPersonId", required = false) String dealPersonId,
                            @RequestParam(value = "dealTime", required = false) Date dealTime,
                            @RequestParam(value = "ifDefectDisable", required = false) Integer ifDefectDisable,
                            @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
                            @RequestParam(value = "defectSubtype", required = false) Integer defectSubtype,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "imagePath", required = false) String imagePath,
                            @RequestParam(value = "videoPath", required = false) String videoPath,
                            @RequestParam(value = "value", required = false) String value,
                            @RequestParam(value = "outRange", required = false) String outRange,
                            @RequestParam(value = "linkMessage", required = false) String linkMessage) {
        Result result = new Result();
        try {
            List<TDefectInfo> list = tDefectInfoService.select(defectId, defectLevel, defectTime, defectType, defectName, defectContent, deviceId, cunstomId, instanceId, stdMeteId, confMode, isDefect, dealType, dealInfo, dealPersonId, dealTime, ifDefectDisable, alarmSource, defectSubtype, deviceCode, imagePath, videoPath, value, outRange, linkMessage);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询陷信息数据",content = "根据用户传递的参数分页查询缺陷信息",logType = 1)
    public Result selectByPage(@RequestBody TDefectInfo tDefectInfo
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tDefectInfo.getPageNum()!=null?tDefectInfo.getPageNum():1, tDefectInfo.getPageSize()!=null?tDefectInfo.getPageSize():0,true,null,true);
            List<TDefectInfo> list = tDefectInfoService.selectByPage(tDefectInfo);
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
    @Logs(title = "批量插入陷信息数据",content = "根据用户传递的参数批量插入缺陷信息数据",logType = 2)
    public Result batchInsert(@Validated @RequestBody List<TDefectInfo> list) {
        Result result = new Result();
        try {
        result.setData(tDefectInfoService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }
    @ApiOperation(value = "查询所有缺陷")
    @RequestMapping(value = "/selectDefectByPage", method = RequestMethod.GET)
    @Logs(title = "查询陷信息数据",content = "根据用户传递的参数查询所有缺陷",logType = 1)
    public Result selectDefectByPage(@RequestParam(value = "confMode", required = false) Integer confMode,
                                     @RequestParam(value = "defectModel", required = false) Integer defectModel,
                                     @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                                     @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                     @RequestParam(value = "deviceName", required = false) String deviceName,
                                     @RequestParam(value = "meteName", required = false) String meteName,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TDefectInfoDetail> list = tDefectInfoService.selectDefectByPage(confMode,defectModel,startTime,endTime,deviceName,meteName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询所有告警失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "统计近一月的所有缺陷个数-柱图")
    @RequestMapping(value = "/countDefectOnMonth", method = RequestMethod.GET)
    @Logs(title = "统计近一月的所有缺陷个数",content = "统计近一个月的所有缺陷",logType = 1)
    public Result countDefectOnMonth(){
        Result result = new Result();
        try {
            List<WarnStatistical> list = tDefectInfoService.countDefectOnMonth();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据缺陷类型统计缺陷个数-柱图")
    @RequestMapping(value = "/countByDefectType", method = RequestMethod.GET)
    @Logs(title = "统计近一月的所有缺陷个数",content = "根据缺陷类型统计缺陷个数",logType = 1)
    public Result countByDeviceType(){
        Result result = new Result();
        try {
            List<TJDefectByType> list = tDefectInfoService.countByDefectType();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据缺陷处理状态统计缺陷个数-饼图")
    @RequestMapping(value = "/countDefectConfMode", method = RequestMethod.GET)
    @Logs(title = "根据缺陷处理状态统计缺陷个数",content = "根据缺陷处理状态统计缺陷个数",logType = 1)
    public Result countDefectConfMode(){
        Result result = new Result();
        try {
            List<TJContentInfo> list = tDefectInfoService.countDefectConfMode();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "查看缺陷处理情况")
    @RequestMapping(value = "/selectDefectProcess", method = RequestMethod.GET)
    @Logs(title = "查看缺陷处理情况",content = "查看缺陷处理情况",logType = 1)
    public Result selectDefectProcess(@RequestParam(value = "warnId") Long defectId){
        Result result = new Result();
        try {
            List<TDefectInfoDetail> list = tDefectInfoService.selectDefectProcess(defectId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "进行缺陷处理")
    @RequestMapping(value = "/defectProcess", method = RequestMethod.PUT)
    @Logs(title = "进行缺陷处理",content = "进行缺陷处理",logType = 5)
    public Result defectProcess(@RequestBody TDefectInfo tDefectInfo,HttpServletRequest request){
        Result result = new Result();
        String userId = request.getHeader("userId");
        try {
            result.setData(tDefectInfoService.defectProcess(tDefectInfo,userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }
}
