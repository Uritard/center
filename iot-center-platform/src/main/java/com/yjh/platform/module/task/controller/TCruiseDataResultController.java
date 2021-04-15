package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsAspect;
//import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TCruiseDataResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCruiseDataResult/v1")
@Api(value = "/tCruiseDataResult")
public class TCruiseDataResultController {

    @Autowired
    private final TCruiseDataResultService tCruiseDataResultService;

    private Logger log = LoggerFactory.getLogger(TCruiseDataResultController.class);

    @Autowired
    private TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdRegionService tStdRegionService;
    @Resource
    private RedisTemplate redisTemplate;

    public TCruiseDataResultController(TCruiseDataResultService tCruiseDataResultService) {
        this.tCruiseDataResultService = tCruiseDataResultService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数新增巡检点结果数据",logType = 2)
    public Result insert(@Validated @RequestBody TCruiseDataResult tCruiseDataResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.insert(tCruiseDataResult));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @DeleteMapping(value = "/delete")
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数删除巡检点结果数据",logType = 4)
    public Result delete(@RequestParam(value = "cruiseDataId", required = true) Long cruiseDataId) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.deleteByPrimaryId(cruiseDataId));
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
    @PutMapping(value = "/update")
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数修改巡检点结果数据",logType = 3)
    public Result update(@RequestBody TCruiseDataResult tCruiseDataResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.update(tCruiseDataResult));
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
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数查询巡检点结果数据",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "cruiseDataId", required = true) Long cruiseDataId) {
        Result result = new Result();
        try {
            TCruiseDataResult tCruiseDataResult = tCruiseDataResultService.selectByPrimaryId(cruiseDataId);
            result.setData(tCruiseDataResult);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @GetMapping(value = "/select")
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数查询巡检点结果数据",logType = 1)
    public Result select(@RequestParam(value = "cruiseDataId", required = false) Long cruiseDataId,
                         @RequestParam(value = "cruiseResultId", required = false) String cruiseResultId,
                         @RequestParam(value = "cruiseId", required = false) Long cruiseId,
                         @RequestParam(value = "cruiseName", required = false) String cruiseName,
                         @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                         @RequestParam(value = "cruiseAbnormal", required = false) Integer cruiseAbnormal,
                         @RequestParam(value = "resultDesc", required = false) String resultDesc,
                         @RequestParam(value = "resultNum", required = false) String resultNum,
                         @RequestParam(value = "modifyNum", required = false) String modifyNum,
                         @RequestParam(value = "picPathAnl", required = false) String picPathAnl,
                         @RequestParam(value = "picpath", required = false) String picpath,
                         @RequestParam(value = "personCheck", required = false) String personCheck,
                         @RequestParam(value = "origPicAnl", required = false) String origPicAnl,
                         @RequestParam(value = "origpic", required = false) String origpic,
                         @RequestParam(value = "evaluationState", required = false) String evaluationState,
                         @RequestParam(value = "identifyState", required = false) Integer identifyState,
                         @RequestParam(value = "identifyResult", required = false) Integer identifyResult,
                         @RequestParam(value = "createtime", required = false) Date createtime,
                         @RequestParam(value = "remark", required = false) String remark,
                         @RequestParam(value = "checkUser", required = false) String checkUser,
                         @RequestParam(value = "checkDate", required = false) Date checkDate,
                         @RequestParam(value = "isWarn", required = false) Integer isWarn,
                         @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult) {

        Result result = new Result();
        try {
            List<TCruiseDataResult> list = tCruiseDataResultService.select(cruiseDataId, cruiseResultId, cruiseId, cruiseName, cruiseType, cruiseAbnormal, resultDesc, resultNum, modifyNum, picPathAnl,picpath,
                    personCheck, origPicAnl,origpic, evaluationState, identifyState, identifyResult, createtime, remark,
                    checkUser, checkDate, isWarn, cruiseResult);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @PostMapping(value = "/selectByPage")
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数分页查询巡检点结果数据",logType = 1)
    public Result selectByPage(@RequestBody TCruiseDataResult tCruiseDataResult
                              ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruiseDataResult.getPageNum()!=null?tCruiseDataResult.getPageNum():1, tCruiseDataResult.getPageSize()!=null?tCruiseDataResult.getPageSize():0,true, null, true);
            List<TCruiseDataResult> list = tCruiseDataResultService.selectByPage(tCruiseDataResult);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @PostMapping(value = "/batchInsert")
    @Logs(title = "批量插入巡检点结果数据",content = "根据用户传递的参数批量插入巡检点结果数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCruiseDataResult> list) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "巡视结果分析--测点查询")
    @GetMapping(value = "/selectCruiseResultAnal")
    @Logs(title = "查询测点信息",content = "查询测点信息",logType = 1)
    public Result selectCruiseResultAnal(@RequestParam(value = "deviceId", required = false) Long deviceId,
                                         @RequestParam(value = "resultSwitch") String resultSwitch,
                                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "6") int pageSize) {
        Result result = new Result();
        List<Long> regionIds = new ArrayList<>();
        List<Long> deviceIds = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(Objects.nonNull(deviceId)){
                if (Objects.nonNull(tStdDeviceService.selectByPrimaryId(deviceId))) {
                    deviceIds.add(deviceId);
                } else {
                    regionIds = tStdDeviceService.selectRegionIdTree(deviceId);
                    deviceIds = tStdDeviceService.selectDeviceIdsByRegion(regionIds);
                }
            }else {
                deviceIds.add(Long.valueOf(996));
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos = tCruiseDataResultService.selectCruiseResultAnal(deviceIds, resultSwitch);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultAnalMeteInfos);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视结果分析--测点查询失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视结果分析--测点查询2")
    @GetMapping(value = "/selectCruiseResultAnalyze")
    @Logs(title = "查询测点信息",content = "查询测点信息",logType = 1)
    public Result selectCruiseResultAnalyze(@RequestParam(value = "regionId", required = false) Long regionId,
                                         @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                         @RequestParam(value = "meteType", required = false) String meteType,
                                         @RequestParam(value = "meterType", required = false) Integer meterType,
                                         @RequestParam(value = "cruiseRes", required = false) Integer cruiseRes,
                                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "6") int pageSize) {
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
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = tCruiseDataResultService.selectCruiseResultAnalyze(deviceIdList,deviceType,meteType,meterType,cruiseRes);
            resultMap.put("count",page.getTotal());
            resultMap.put("list", cruiseResultAnalMeteInfoList);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视结果分析--测点查询2失败描述：", e);
        }

        return result;
    }
    @ApiOperation(value = "巡视报表")
    @GetMapping(value = "/selectCruiseDataReport")
  //  @Logs(title = "巡视报表",content = "根据用户传递的参数查询巡视报表",logType = 1)
    public Result selectCruiseDataReport(@RequestParam(value = "cType", required = false) Integer cType,
                                         @RequestParam(value = "meteType", required = false) String meteType,
                                         @RequestParam(value = "meterType", required = false) Integer meterType,
                                         @RequestParam(value = "regionId", required = false) Long regionId,
                                         @RequestParam(value = "instanceName", required = false) String instanceName,
                                         @RequestParam(value = "endTime", required = false) String endTime,
                                         @RequestParam(value = "startTime", required = false) String startTime,
                                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize, HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        Long userIds = Long.valueOf(request.getHeader("userId"));
        String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userIds,"userName"));
        try {
            if(pageSize==0){
                MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                param.set("logType", "9");
                param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
                param.set("title", "导出");
                param.set("state", 1);
                param.set("userId", userIds);
                param.set("userName", userName);
                param.set("requestOrigin", request.getRequestURL());
                param.set("requestPath", request.getRequestURI());
                param.set("requestMethod", request.getMethod());
                param.set("content", "巡视报表导出");
                LogsAspect logsAspects = new LogsAspect();
                logsAspects.post(param);
            }else{
                MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                param.set("logType", "1");
                param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
                param.set("title", "巡视报表");
                param.set("state", 1);
                param.set("userId", userIds);
                param.set("userName", userName);
                param.set("requestOrigin", request.getRequestURL());
                param.set("requestPath", request.getRequestURI());
                param.set("requestMethod", request.getMethod());
                param.set("content", "根据用户传递的参数查询巡视报表");
                LogsAspect logsAspects = new LogsAspect();
                logsAspects.post(param);
            }
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
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
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = tCruiseDataResultService.selectCruiseDataReport(cType,meteType,meterType,endTime, startTime,deviceIdList,instanceName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultAnalyzeInfoList);
            result.setData(resultMap);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视报表失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视结果分析--巡检点结果列表")
    @GetMapping(value = "/selectCruiseDataResultByList2")
   // @Logs(title = "巡检点结果列表",content = "根据用户传递的参数查询巡检点结果信息",logType = 1)
    public Result selectCruiseDataResultByList2(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                               @RequestParam(value = "cType", required = false) Integer cType,
                                               @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                                                @RequestParam(value = "meteType", required = false) String meteType,
                                                @RequestParam(value = "meterType", required = false) Integer meterType,
                                                @RequestParam(value = "endTime", required = false) String endTime,
                                               @RequestParam(value = "startTime", required = false) String startTime,
                                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
      //  LogsRecord logsRecord=new LogsRecord();
        try {
//            if(pageSize==0){
//                logsRecord.LogsSend(request,"9","导出","巡检点结果列表导出");
//            }else{
//                logsRecord.LogsSend(request,"1","巡检点结果列表","根据用户传递的参数查询巡检点结果信息");
//            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
            List<CruiseResultAnalyzeInfo>  list = tCruiseDataResultService.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId, meteType,meterType,endTime, startTime,pageNum,pageSize);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视结果分析--巡检点结果列表失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视结果分析--巡检点结果列表")
    @GetMapping(value = "/selectCruiseDataResultByList")
    @Logs(title = "巡检点结果列表",content = "根据用户传递的参数查询巡检点结果信息",logType = 1)
    public Result selectCruiseDataResultByList(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                               @RequestParam(value = "cType", required = false) Integer cType,
                                               @RequestParam(value = "deviceMeteId") Long deviceMeteId,
                                               @RequestParam(value = "endDate", required = false) String endDate,
                                               @RequestParam(value = "startDate", required = false) String startDate,
                                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "4") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date endDateTemp;
        Date startDateTemp;
        try {
            if (endDate.equals("") && startDate.equals("")) {
                endDateTemp = null;
                startDateTemp = null;
            } else {
                endDateTemp = dateFormat.parse(endDate);
                startDateTemp = dateFormat.parse(startDate);
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalInfo> list = (tCruiseDataResultService.selectCruiseDataResultByList(cruiseType, cType, deviceMeteId, endDateTemp, startDateTemp));
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视结果分析--巡检点结果列表失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "获取折线图元素信息")
    @GetMapping(value = "/selectBrokenLine")
    @Logs(title = "获取折线图元素信息",content = "根据用户传递的参数获取折线图信息",logType = 1)
    public Result selectBrokenLine(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                   @RequestParam(value = "cType", required = false) Integer cType,
                                   @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                                   @RequestParam(value = "meteType", required = false) String meteType,
                                   @RequestParam(value = "meterType", required = false) Integer meterType,
                                   @RequestParam(value = "endTime", required = false) String endTime,
                                   @RequestParam(value = "startTime", required = false) String startTime) {
        Result result = new Result();
        try {
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
            result.setData(tCruiseDataResultService.selectBrokenLine(cruiseType, cType, deviceMeteId, startTime, endTime,meteType,meterType));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("获取折线图元素信息失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "测点巡检时间记录表维护")
    @PostMapping(value = "/updateCruiseAnalyze")
   // @Logs(title = "测点巡检时间记录表维护",content = "测点巡检时间记录表维护",logType = 2)
    public Result updateCruiseAnalyze(@RequestBody List<String> cruiseResultIdList) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.updateCruiseAnalyze(cruiseResultIdList));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("测点巡检时间记录表维护失败：" + e);
        }
        return result;
    }



    @ApiOperation(value = "图像判别结果查询")
    @RequestMapping(value = "/QueryDifferentiateResult",method = RequestMethod.GET)
    public Result QueryDifferentiateResult(@RequestParam String taskId){
        Result result=new Result();
        try {
           result.setData(tCruiseDataResultService.QueryDifferentiateResult(taskId));
        }catch (Exception e){
            log.error("查询失败"+e);
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
        }
        return  result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPicFirPage",method = RequestMethod.GET)
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数分页查询巡检点结果数据",logType = 1)
    public Result selectByPicFirPage(@RequestParam(value = "endDate", required = false) String endDate,
                                     @RequestParam(value = "fileName", required = false) String fileName,
                                     @RequestParam(value = "startDate", required = false) String startDate,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "4") int pageSize
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true, null, true);
            List<FirAndPicInfo> list = tCruiseDataResultService.selectByPicFirPage(endDate,fileName,startDate,pageNum,pageSize);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("分页查询失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据cameraId分页查询")
    @RequestMapping(value = "/selectByCameraId",method = RequestMethod.GET)
    @Logs(title = "根据cameraId分页查询",content = "根据cameraId分页查询",logType = 1)
    public Result selectByCameraId(@RequestParam(value = "cameraId", required = false) Long cameraId,
                                   @RequestParam(value = "endDate", required = false) String endDate,
                                   @RequestParam(value = "fileName", required = false) String fileName,
                                   @RequestParam(value = "startDate", required = false) String startDate,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "4") int pageSize)
    {

        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        Page page= PageHelper.startPage(pageNum, pageSize,true, null, true); ;
        try {
            List<FirAndPicInfo> list = tCruiseDataResultService.selectByCameraId(cameraId,startDate,endDate,fileName);
            if(list!=null) {
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("cameraId查询：", e);
        }
        return result;
    }


    /*@ApiOperation(value = "主键查询")
    @GetMapping(value = "/selectByPicFirPrimaryId")
    @Logs(title = "巡检点结果数据",content = "根据用户传递的参数查询巡检点结果数据",logType = 1)
    public Result selectByPicFirPrimaryId(@RequestParam(value = "cruiseDataId", required = true) Long cruiseDataId) {
        Result result = new Result();
        try {
            FirAndPicInfo f = tCruiseDataResultService.selectByPicFirPrimaryId(cruiseDataId);
            result.setData(f);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }*/

}
