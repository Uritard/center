package com.yjh.platform.module.task.controller;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
//import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TWarnInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * @author tt
 * @since 2020-10-15
 */
@RestController
@RequestMapping("/tWarnInfo/v1")
@Api(value = "/tWarnInfo")
public class TWarnInfoController {

    @Autowired
    private final TWarnInfoService tWarnInfoService;
    @Autowired
    private LogsRecord logsRecord;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TStdRegionService tStdRegionService;
    @Autowired
    private TStdDeviceService tStdDeviceService;

    private Logger log = LoggerFactory.getLogger(TWarnInfoController.class);

    public TWarnInfoController(TWarnInfoService tWarnInfoService) {
        this.tWarnInfoService = tWarnInfoService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "新增告警信息数据",content = "根据用户传递的参数新增告警信息数据",logType =2)
    public Result insert(@Validated @RequestBody TWarnInfo tWarnInfo) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.insert(tWarnInfo));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @PostMapping(value = "/delete")
    @Logs(title = "删除告警信息数据",content = "根据用户传递的参数删除告警信息数据",logType = 4)
    public Result delete(@RequestParam(value = "warnId", required = true) Long warnId) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.deleteByPrimaryId(warnId));
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
    @PostMapping(value = "/update")
    @Logs(title = "修改告警信息数据",content = "根据用户传递的参数修改告警信息数据",logType = 3)
    public Result update(@Validated @RequestBody TWarnInfo tWarnInfo) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.update(tWarnInfo));
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
    @GetMapping(value = "/selectByPrimaryId")
    @Logs(title = "查询告警信息数据",content = "根据用户传递的参数查询告警信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "warnId", required = true) Long warnId) {
        Result result = new Result();
        try {
            TWarnInfo tWarnInfo = tWarnInfoService.selectByPrimaryId(warnId);
            result.setData(tWarnInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @GetMapping(value = "/select")
    @Logs(title = "查询告警信息数据",content = "根据用户传递的参数查询告警信息",logType = 1)
    public Result select(@RequestParam(value = "warnId", required = false) Long warnId,
                         @RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                         @RequestParam(value = "warnTime", required = false) Date warnTime,
                         @RequestParam(value = "warnType", required = false) Integer warnType,
                         @RequestParam(value = "warnName", required = false) String warnName,
                         @RequestParam(value = "warnContent", required = false) String warnContent,
                         @RequestParam(value = "deviceId", required = false) Long deviceId,
                         @RequestParam(value = "cunstomId", required = false) String cunstomId,
                         @RequestParam(value = "instanceId", required = false) Long instanceId,
                         @RequestParam(value = "stdMeteId", required = false) Long stdMeteId,
                         @RequestParam(value = "confMode", required = false) Integer confMode,
                         @RequestParam(value = "isWarn", required = false) Integer isWarn,
                         @RequestParam(value = "dealType", required = false) Integer dealType,
                         @RequestParam(value = "dealInfo", required = false) String dealInfo,
                         @RequestParam(value = "dealPersonId", required = false) String dealPersonId,
                         @RequestParam(value = "dealTime", required = false) Date dealTime,
                         @RequestParam(value = "defectModel", required = false) Integer defectModel,
                         @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
                         @RequestParam(value = "warnSubtype", required = false) Integer warnSubtype,
                         @RequestParam(value = "deviceCode", required = false) String deviceCode,
                         @RequestParam(value = "imagePath", required = false) String imagePath,
                         @RequestParam(value = "videoPath", required = false) String videoPath,
                         @RequestParam(value = "value", required = false) String value,
                         @RequestParam(value = "outRange", required = false) String outRange,
                         @RequestParam(value = "taskId", required = false) String taskId) {
        Result result = new Result();
        try {
            List<TWarnInfo> list = tWarnInfoService.select(warnId, warnLevel, warnTime, warnType, warnName, warnContent, deviceId, cunstomId, instanceId, stdMeteId, confMode, isWarn, dealType, dealInfo, dealPersonId, dealTime, defectModel, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, outRange, taskId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @PostMapping(value = "/selectByPage")
    @Logs(title = "查询告警信息数据",content = "根据用户传递的参数分页查询告警信息",logType = 1)
    public Result selectByPage(@RequestBody TWarnInfo tWarnInfo
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tWarnInfo.getPageNum()!=null?tWarnInfo.getPageNum():1, tWarnInfo.getPageSize()!=null?tWarnInfo.getPageSize():0,true,null,true);
            List<TWarnInfo> list = tWarnInfoService.selectByPage(tWarnInfo);
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
    @PostMapping(value = "/batchInsert")
    @Logs(title = "批量插入告警信息数据",content = "根据用户传递的参数批量插入告警信息数据",logType = 2)
    public Result batchInsert(@RequestBody List<TWarnInfo> list) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败描述：" + e);
        }
        return result;
    }
    @ApiOperation(value = "查询所有告警")
    @GetMapping(value = "/selectWarnByPage")
    @Logs(title = "查询所有告警",content = "根据用户传递的参数查询所有告警",logType = 1,authority = "1235")
    public Result selectWarnByPage(@RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                                   @RequestParam(value = "confMode", required = false) Integer confMode,
                                   @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
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
            List<TWarnInfoDetail> list = tWarnInfoService.selectWarnByPage(warnLevel, confMode, alarmSource,startTime,endTime,deviceName,meteName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询所有告警失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "告警确认")
    @GetMapping(value = "/WarnConfirm")
//    @Logs(title = "告警确认",content = "根据用户传递的参数进行告警确认",logType = 5)
    public Result WarnConfirm(@RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                              @RequestParam(value = "confMode", required = false) Integer confMode,
                              @RequestParam(value = "startTime", required = false) String startTime,
                              @RequestParam(value = "endTime", required = false)String endTime,
                              @RequestParam(value = "deviceName", required = false) String deviceName,
                              @RequestParam(value = "defectType", required = false) Integer defectType,
                              @RequestParam(value = "meteName", required = false) String meteName,
                              @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                              @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(pageSize==0){
//                logsRecord.LogsSend(request,"9","导出","告警确认导出");
            }else{
//                logsRecord.LogsSend(request,"1","告警确认查询","根据用户传递的参数进行告警确认");
            }
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TWarnInfoDetail> list = tWarnInfoService.WarnConfirm(warnLevel, confMode,startTime,endTime,deviceName,defectType,meteName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
//        }catch (BusinessException e) {
//            result.setMessage(10008, "用户无权限");
            //log.error("日志统计失败：" + e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询所有告警失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "告警核查")
    @GetMapping(value = "/warnReview")
    @Logs(title = "告警核查",content = "根据用户传递的参数进行告警核查",logType = 5)
    public Result warnReview(@RequestParam(value = "warnId", required = false) Long warnId,
                             @RequestParam(value = "dealInfo", required = false) String dealInfo,
                             @RequestParam(value = "dealType", required = false) Integer dealType,
                             @RequestParam(value = "defectModel", required = false) Integer defectModel,
                             HttpServletRequest request) {
        Result result = new Result();
        String userId = request.getHeader("userId");
        try {
            result.setData(tWarnInfoService.warnReview(warnId, dealInfo,dealType,userId,defectModel));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("告警核查发生异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("告警核查发生错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "根据告警来源统计告警个数")
    @GetMapping(value = "/countByAlarmSource")
    @Logs(title = "根据告警来源统计告警个数",content = "根据告警来源统计告警个数",logType = 1,authority = "1235")
    public Result countByAlarmSource(){
        Result result = new Result();
        try {
            List<TJContentInfo> list = tWarnInfoService.countByAlarmSource();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("根据告警来源统计告警个数失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据设备类型统计告警个数-近一月")
    @GetMapping(value = "/countByDeviceType")
    @Logs(title = "根据设备类型统计告警个数",content = "根据设备类型统计近一个月的告警个数",logType = 1)
    public Result countByDeviceType(){
        Result result = new Result();
        try {
            List<TJContentInfoDetail> list = tWarnInfoService.countByDeviceType();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据设备类型统计告警和缺陷个数-近一月")
    @GetMapping(value = "/countAlarmByDeviceType")
    @Logs(title = "根据设备类型统计告警个数",content = "根据设备类型统计告警和缺陷个数",logType = 1,authority = "1235")
    public Result countAlarmByDeviceType(){
        Result result = new Result();
        try {
            List<TJContentInfoDetail> list = tWarnInfoService.countAlarmByDeviceType();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "统计近一月的所有告警个数-折线图")
    @GetMapping(value = "/countAllWarnOnMonth")
    @Logs(title = "根据设备类型统计告警个数",content = "统计近一个月的所有告警",logType = 1)
    public Result countAllWarnOnMonth(){
        Result result = new Result();
        try {
            List<WarnStatistical> list = tWarnInfoService.countAllWarnOnMonth();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "统计近一月的机器人本体告警个数-折线图")
    @GetMapping(value = "/countWarnOnMonth")
    @Logs(title = "根据时间统计告警个数",content = "统计近一个月的机器人本体告警",logType = 1,authority = "1235")
    public Result countWarnOnMonth(){
        Result result = new Result();
        try {
            List<WarnStatistical> list = tWarnInfoService.countWarnOnMonth();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "统计近一月的所有告警和缺陷个数-折线图")
    @GetMapping(value = "/countWarnAndDefectOnMonth")
    @Logs(title = "根据设备类型统计告警个数",content = "统计近一个月的所有告警和缺陷",logType = 1,authority = "1235")
    public Result countWarnAndDefectOnMonth(){
        Result result = new Result();
        try {
            List<WarnStatistical> list = tWarnInfoService.countWarnAndDefectOnMonth();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据告警处理状态统计告警个数-近一月")
    @GetMapping(value = "/countWarnConfMode")
    @Logs(title = "根据告警处理状态统计告警个数",content = "根据告警处理状态统计近一个月的告警个数",logType = 1)
    public Result countWarnConfMode(){
        Result result = new Result();
        try {
            List<TJContentInfo> list = tWarnInfoService.countWarnConfMode();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据告警和缺陷处理状态统计告警个数-近一月")
    @GetMapping(value = "/countWarnDefectConfMode")
    @Logs(title = "根据告警处理状态统计告警个数",content = "根据告警和缺陷处理状态统计告警个数",logType = 1,authority = "1235")
    public Result countWarnDefectConfMode(){
        Result result = new Result();
        try {
            List<TJContentInfo> list = tWarnInfoService.countWarnDefectConfMode();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "查看告警处理情况")
    @GetMapping(value = "/selectAlarmProcess")
    @Logs(title = "根据告警处理状态统计告警个数",content = "根据用户传递的参数查询告警处理状况",logType = 1)
    public Result selectAlarmProcess(@RequestParam(value = "warnId") Long warnId){
        Result result = new Result();
        try {
            List<TWarnInfoDetail> list = tWarnInfoService.selectAlarmProcess(warnId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "进行告警处理")
    @PostMapping(value = "/alarmProcess")
    @Logs(title = "进行告警处理",content = "进行告警处理",logType = 5)
    public Result alarmProcess(@RequestBody TWarnInfo tWarnInfo, HttpServletRequest request){
        Result result = new Result();
        String userId = request.getHeader("userId");
        try {
            result.setData(tWarnInfoService.alarmProcess(tWarnInfo,userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("进行告警处理发生异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("进行告警处理发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "告警核查")
    @PostMapping(value = "/alarmAndDefectProcess")
    @Logs(title = "告警核查",content = "告警核查",logType = 5,authority = "1235")
    public Result alarmAndDefectProcess(@RequestBody AlarmAndDefectProcess alarmAndDefectProcess, HttpServletRequest request){
        Result result = new Result();
        String userId = request.getHeader("userId");
        try {
            result.setData(tWarnInfoService.alarmAndDefectProcess(alarmAndDefectProcess,userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("进行告警处理发生异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("进行告警处理发生错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "告警信息计数统计(未核查)")
    @GetMapping(value = "/warnCountsNonIdentify")
    @Logs(title = "告警信息计数统计",content = "统计未核查的告警",logType = 1, authority = "1234,1235")
    public Result warnCountsNonIdentify(HttpServletRequest request){
        Result result=new Result();
        Map<String,Integer> countResult=new HashMap<>();
        try{
//            String userId = request.getHeader("userId");
//            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
//            if(Constant.apiPermissions) {
//                if (!"1235".equals(userRole)) {
//                    //权限不够；
//                    throw new BusinessException(10008, "用户无权限");
//                    //return -1;
//                }
//            }
            countResult.put("count",tWarnInfoService.warnCountsNonIdentify());
            result.setData(countResult);
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("数量更新失败",e);
        }
        return  result;
    }
    @ApiOperation(value = "告警弹框")
    @GetMapping(value = "/warnPopUp")
    @Logs(title = "巡视任务告警",content = "巡视任务告警",logType = 1,authority = "1235")
    public Result warnPopUp(@RequestParam(value = "warnId") String warnId,
                            @RequestParam(value = "defectModel") Integer defectModel) {
        Result result = new Result();
        try {
            TWarnInfoDetail tWarnInfoDetail = tWarnInfoService.selectWarnPopUp(warnId,defectModel);
            result.setData(tWarnInfoDetail);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询告警弹窗内容失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询当前三分钟以内最新告警")
    @GetMapping(value = "/SelectCurrentWarn")
//    @Logs(title = "查询最新告警",content = "三分中内最新告警ID",logType = 5)
    public Result SelectCurrentWarn(){
        Result result=new Result();
        try{
            result.setData(tWarnInfoService.selectCurrentWarn());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询告警数据失败：", e);
        }
        return result;
    }



    @ApiOperation(value = "查询当前三分钟以内最新联动信息")
    @GetMapping(value = "/SelectCurrentUnion")
//    @Logs(title = "查询最新联动",content = "三分中内最新联动任务ID",logType = 5)
    public Result SelectCurrentUnion(){
        Result result=new Result();
        try{
            result.setData(tWarnInfoService.selectUnionWarn());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询告警数据失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询操作任务告警")
    @RequestMapping(value = "/selectOperationWarn",method = RequestMethod.POST)
    @Logs(title = "查询操作任务告警",content = "根据用户传递的参数查询查询操作任务告警",logType = 1,authority = "1235")
    public Result selectOperationWarn(@RequestBody JSONObject obj) {
        Long regionId = obj.getLong("regionId");
        String startTime= obj.getString("startTime");
        String endTime = obj.getString("endTime");
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Long> deviceIdList = new ArrayList<>();
            if (regionId == null){
                List<Long> regionIdList = tStdRegionService.selectDownId(regionId);//查询该regionId的子节点
                deviceIdList =  tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
            }else {
                List<Long> regionIdList = tStdRegionService.selectDownId(regionId);//查询该regionId的子节点
                if (regionIdList != null && !regionIdList.isEmpty()){
                    deviceIdList =  tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
                }else {
                    deviceIdList.add(regionId);
                }
            }
            Page page = PageHelper.startPage(obj.getIntValue("pageNum"), obj.getIntValue("pageSize"), true, null, true);
            List<HashMap<String, String>> list = tWarnInfoService.selectOperationWarn(deviceIdList, startTime, endTime);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询操作任务告警失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "机器人本体告警弹框")
    @GetMapping(value = "/robotWarnPopUp")
    @Logs(title = "机器人本体告警弹框",content = "机器人本体告警弹框",logType = 1,authority = "1235")
    public Result robotWarnPopUp(@RequestParam(value = "warnId") Long warnId) {
        Result result = new Result();
        try {
            TWarnInfoDetail tWarnInfoDetail = tWarnInfoService.selectRobotWarn(warnId);
            result.setData(tWarnInfoDetail);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询机器人本体告警弹框内容失败：", e);
        }
        return result;
    }


}
