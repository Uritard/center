package com.yjh.platform.module.patrol.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.page.PageMethod;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.patrol.service.UPatrolDataResultService;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeMeteInfo;
import com.yjh.platform.module.task.entity.CruiseResultDetail;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private UPatrolDataResultService uPatrolDataResultService;
    @Autowired
    private TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdRegionService tStdRegionService;
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
                                            @RequestParam(value = "meteName", required = false) String meteName,
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
            List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = uPatrolDataResultService.selectCruiseResultAnalyze(deviceIdList,deviceType,meteType,meterType,cruiseRes,customId, meteName);
            resultMap.put("count",page.getTotal());
            resultMap.put("list", cruiseResultAnalMeteInfoList);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
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
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,
                                         @RequestParam(value = "stationName", required = false) String stationName) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();

        try {

            DateTimeUtil.checkDate(startTime, endTime);

            List<Long> deviceIdList = new ArrayList<>();
            List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
            if (regionId == null){
                //查询该regionId的子节点
                deviceIdList =  tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
            }else {
                //查询该regionId的子节点
                if (regionIdList != null && !regionIdList.isEmpty()){
                    deviceIdList =  tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
                    // 若没有查询出设备，则增加一个-1表示该区域下无设备
                    if (CollectionUtils.isEmpty(deviceIdList)) {
                        deviceIdList = Collections.singletonList(-1L);
                    }
                }else {
                    deviceIdList.add(regionId);
                }
            }

            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = uPatrolDataResultService.selectCruiseDataReport(cType,meteType,meterType,endTime, startTime, deviceIdList,instanceName, stationName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultAnalyzeInfoList);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视报表失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视报表")
    @GetMapping(value = "/exportCruiseDataReport")
    @Logs(title = "导出",content = "根据用户传递的参数导出巡视报表",logType = 9, authority = "1235")
    public Result exportCruiseDataReport(@RequestParam(value = "cType", required = false) Integer cType,
                                         @RequestParam(value = "meteType", required = false) String meteType,
                                         @RequestParam(value = "meterType", required = false) Integer meterType,
                                         @RequestParam(value = "regionId", required = false) Long regionId,
                                         @RequestParam(value = "instanceName", required = false) String instanceName,
                                         @RequestParam(value = "endTime", required = false) String endTime,
                                         @RequestParam(value = "startTime", required = false) String startTime,
                                         @RequestParam(value = "stationName", required = false) String stationName,
                                         @RequestParam(value = "typeString") String typeString,
                                         HttpServletRequest request) {

        Result result = new Result();
        try {
            DateTimeUtil.checkDate(startTime, endTime);
            List<Long> deviceIdList = new ArrayList<>();
            if (regionId == null){
                //查询该regionId的子节点
                List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
                deviceIdList =  tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
            }else {
                //查询该regionId的子节点
                List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
                if (regionIdList != null && !regionIdList.isEmpty()){
                    deviceIdList =  tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
                }else {
                    deviceIdList.add(regionId);
                }
            }

            List<Map<String, Object>> cruiseResultAnalyzeInfoList = uPatrolDataResultService.exportCruiseDataReport(cType, meteType, meterType, endTime, startTime, deviceIdList, instanceName, stationName);
            String userId = request.getHeader("userId") + "_" + request.getHeader("token");
            uPatrolDataResultService.createCruiseDataReport(userId, cruiseResultAnalyzeInfoList, typeString);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
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
                                                @RequestParam(value = "deviceMeteIds") String deviceMeteIds,
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
            DateTimeUtil.checkDate(startTime, endTime);

            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalyzeInfo>  list = uPatrolDataResultService.selectCruiseDataResultByList(cruiseType, cType, deviceMeteIds, meteType,meterType,endTime, startTime);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
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
                                   @RequestParam(value = "deviceMeteIds") String deviceMeteIds,
                                   @RequestParam(value = "meteType", required = false) String meteType,
                                   @RequestParam(value = "meterType", required = false) Integer meterType,
                                   @RequestParam(value = "endTime", required = false) String endTime,
                                   @RequestParam(value = "startTime", required = false) String startTime) {
        Result result = new Result();
        try {
            DateTimeUtil.checkDate(startTime, endTime);
            result.setData(uPatrolDataResultService.selectBrokenLine(cruiseType, cType, deviceMeteIds, startTime, endTime,meteType,meterType));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("获取折线图元素信息失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据cameraId分页查询")
    @RequestMapping(value = "/selectByCameraId",method = RequestMethod.GET)
    @Logs(title = "根据cameraId分页查询",content = "根据cameraId分页查询",logType = 1, authority = "1235")
    public Result selectByCameraId(@RequestParam(value = "cameraId", required = false) Long cameraId,
                                   @RequestParam(value = "endDate", required = false) String endDate,
                                   @RequestParam(value = "startDate", required = false) String startDate,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "4") int pageSize)
    {

        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        Page page= PageHelper.startPage(pageNum, pageSize,true, null, true); ;
        try {
            List<UPatrolDataResult> list = uPatrolDataResultService.selectByCameraIdNew(cameraId,startDate,endDate);
            if(list!=null) {
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("cameraId查询：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视结果分析--报表导出")
    @GetMapping(value = "/cruiseDataReport")
    @Logs(title = "导出", content = "巡视结果分析导出", logType = 9)
    public Result cruiseDataReport(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
        @RequestParam(value = "cType", required = false) Integer cType, @RequestParam(value = "deviceMeteIds") String deviceMeteIds,
        @RequestParam(value = "meteType", required = false) String meteType,
        @RequestParam(value = "meterType", required = false) Integer meterType,
        @RequestParam(value = "endTime", required = false) String endTime,
        @RequestParam(value = "startTime", required = false) String startTime, HttpServletRequest request) {
        Result result = new Result();
        try {
            DateTimeUtil.checkDate(startTime, endTime);
            String userId = request.getHeader("userId") + "_" + request.getHeader("token");
            uPatrolDataResultService.cruiseDataReport(cruiseType, cType, deviceMeteIds, meteType, meterType, endTime, startTime, userId);

        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡视结果分析--巡检点结果列表失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "审核异常点位")
    @GetMapping(value = "/identifyAbnormal")
    public Result identifyAbnormal(@RequestParam(value = "cType", required = false) Integer cType,
        @RequestParam(value = "meteType", required = false) String meteType,
        @RequestParam(value = "meterType", required = false) Integer meterType,
        @RequestParam(value = "regionId", required = false) Long regionId,
        @RequestParam(value = "instanceName", required = false) String instanceName,
        @RequestParam(value = "endTime", required = false) String endTime,
        @RequestParam(value = "startTime", required = false) String startTime,
        @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
        @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize, HttpServletRequest request) {
        Result result = new Result();
        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出","审核异常点位列表导出");
            }else{
                logsRecord.LogsSend(request,"1","审核异常点位","根据用户传递的参数获取审核异常点位信息");
            }
            DateTimeUtil.checkDate(startTime, endTime);

            List<Long> deviceIdList = null;
            if (regionId != null){
                //查询该regionId的子节点
                List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
                if (CollectionUtils.isNotEmpty(regionIdList)){
                    deviceIdList =  tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
                    // 若没有查询出设备，则增加一个-1表示该区域下无设备
                    if (CollectionUtils.isEmpty(deviceIdList)) {
                        deviceIdList = Collections.singletonList(-1L);
                    }
                }else {
                    deviceIdList = new ArrayList<>();
                    deviceIdList.add(regionId);
                }
            }

            Map<String, Object> resultMap = new HashMap<>(4);
            Page<CruiseResultAnalyzeInfo> page = PageMethod.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = uPatrolDataResultService.selectIdentifyAbnormal(cType, meteType, meterType, endTime, startTime, deviceIdList, instanceName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultAnalyzeInfoList);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("审核异常点位--审核异常点位列表失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "审核异常点位详细")
    @GetMapping(value = "/identifyAbnormalDetail")
    public Result identifyAbnormalDetail(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
        @RequestParam(value = "deviceMeteId") Long deviceMeteId,
        @RequestParam(value = "endTime", required = false) String endTime,
        @RequestParam(value = "startTime", required = false) String startTime,
        @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
        @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize, HttpServletRequest request) {
        Result result = new Result();
        try (Page<CruiseResultDetail> page = PageMethod.startPage(pageNum, pageSize, true, null, true)) {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出","审核异常点位详细信息导出");
            }else{
                logsRecord.LogsSend(request,"1","审核异常点位详细","根据用户传递的参数获取审核异常点位详细信息");
            }
            DateTimeUtil.checkDate(startTime, endTime);

            Map<String, Object> resultMap = new HashMap<>(4);
            List<CruiseResultDetail> cruiseResultAnalyzeInfoList = uPatrolDataResultService.selectIdentifyAbnormalByMeteId(cruiseType, deviceMeteId, endTime, startTime);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultAnalyzeInfoList);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("审核异常点位详细--审核异常点位详细失败描述：", e);
        }
        return result;
    }
}
