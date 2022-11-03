package com.yjh.platform.module.patrol.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.patrol.service.UPatrolDataResultService;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeMeteInfo;
import com.yjh.platform.module.task.entity.FirAndPicInfo;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
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
import java.util.*;

/**
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/uPatrolDataResult/v1")
@Api(value = "/uPatrolDataResult", tags = {"新的巡检任务历史数据接口"})
public class UPatrolDataResultController {


    private Logger log = LoggerFactory.getLogger(UPatrolDataResultController.class);

    @Autowired
    private UPatrolDataResultService tCruiseDataResultService;
    @Autowired
    private TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdRegionService tStdRegionService;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private LogsRecord logsRecord;


    @ApiOperation(value = "巡视结果分析--测点查询2")
    @GetMapping(value = "/selectCruiseResultAnalyze")
    @Logs(title = "查询测点信息",content = "查询测点信息",logType = 1,authority = "1235")
    public Result selectCruiseResultAnalyze(@RequestParam(value = "regionId", required = false) Long regionId,
                                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                            @RequestParam(value = "customId", required = false) Long customId,
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
            List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = tCruiseDataResultService.selectCruiseResultAnalyze(deviceIdList,deviceType,meteType,meterType,cruiseRes,customId);
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
      @Logs(title = "巡视报表",content = "根据用户传递的参数查询巡视报表",logType = 1, authority = "1235")
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
//        Long userIds = 10011l;

        String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userIds,"userName"));
        try {
//            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userIds).get("roleId"));
//            if(Constant.apiPermissions) {
//                if (!"1235".equals(userRole)) {
//                    //权限不够；
//                    throw new BusinessException(10008, "用户无权限");
//                    //return -1;
//                }
//            }
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
//        }catch (BusinessException e) {
//            result.setMessage(10008, "用户无权限");
            //log.error("日志统计失败：" + e);
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
        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出","巡检点结果列表导出");
            }else{
                logsRecord.LogsSend(request,"1","巡检点结果列表","根据用户传递的参数查询巡检点结果信息");
            }
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

    @ApiOperation(value = "获取折线图元素信息")
    @GetMapping(value = "/selectBrokenLine")
    @Logs(title = "获取折线图元素信息",content = "根据用户传递的参数获取折线图信息",logType = 1,authority = "1235")
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

}
