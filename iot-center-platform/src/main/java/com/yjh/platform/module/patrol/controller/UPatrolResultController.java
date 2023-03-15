package com.yjh.platform.module.patrol.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.patrol.service.UPatrolResultService;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.ReportManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 *
 */
@RestController
@RequestMapping("/uPatrolResult/v1")
@Api(value = "/uPatrolResult")
public class UPatrolResultController {

    @Autowired
    private UPatrolResultService uPatrolResultService;
    @Autowired
    private LogsRecord logsRecord;

    @Autowired
    private TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdRegionService tStdRegionService;

    private Logger log = LoggerFactory.getLogger(UPatrolResultController.class);

    @ApiOperation(value = "分页查询--巡视结果任务查询")
    @GetMapping(value = "/selectTaskByPage")
    @Logs(title = "查询巡检任务结果数据",content = "根据用户传递的参数分页查询巡检任务结果信息",logType = 1, authority = "1235")
    public Result selectTaskByPage(@RequestParam(value = "taskName", required = false) String taskName,
                                   @RequestParam(value = "cState", required = false) Integer cState,
                                   @RequestParam(value = "cType", required = false) Integer cType,
                                   @RequestParam(value = "regionId",required = false) Long regionId,
                                   @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                   @RequestParam(value = "startDate",required = false) String startDate,
                                   @RequestParam(value = "endDate",required = false) String endDate,
                                   @RequestParam(value = "meteType", required = false) Integer meteType,
                                   @RequestParam(value = "customId", required = false) String customId,
                                   @RequestParam(value = "isCheck", required = false) Integer isCheck,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,HttpServletRequest request) {

        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出","巡检任务结果数据导出");
            }else{
                logsRecord.LogsSend(request,"1","查询巡检任务结果数据","根据用户传递的参数分页查询巡检任务结果信息");
            }
            if (Objects.isNull(startDate) || "".equals(startDate)){
                startDate = null;
            }
            if (Objects.isNull(endDate) || "".equals(endDate)){
                endDate = null;
            }
            List<Long> deviceIdList = new ArrayList<>();
            if (regionId == null){
                deviceIdList =  null;
            }else {
                //判断是否是根节点
                TStdRegion rootRegion = tStdRegionService.selectByPrimaryId(regionId);
                if (rootRegion != null && rootRegion.getUpRegionId() == -1){
                    deviceIdList = null;
                }else {
                    List<Long> regionIdList = tStdRegionService.selectDownId(regionId);//查询该regionId的子节点
                    if (regionIdList != null && !regionIdList.isEmpty()) {
                        deviceIdList = tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
                        if (deviceIdList.isEmpty()){
                            //此区域下没有设备
                            deviceIdList.add(-1L);
                        }

                    } else {
                        deviceIdList.add(regionId);
                    }
                }
            }
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<String> taskIdList = uPatrolResultService.selectTaskByPageByPage(taskName,cState,cType,deviceType,startDate,endDate,deviceIdList,meteType,customId,isCheck);
            if (taskIdList.isEmpty()){
                taskIdList.add("-1");
            }
            List<TCruiseResultExpand> list = uPatrolResultService.selectTaskByPage(taskIdList);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视任务结果统计")
    @GetMapping(value = "/taskStatistical")
    @Logs(title = "巡视任务结果统计",content = "根据用户传递的参数统计巡视任务结果",logType = 1,authority = "1235")
    public Result taskStatistical() {
        Result result = new Result();
        try {
            List<StatisticalResult> taskStatisticalList = uPatrolResultService.taskStatistical();
            result.setData(taskStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视点结果统计--根据异常分类统计")
    @GetMapping(value = "/cruiseStatisticalByAbnormal")
    @Logs(title = "根据异常分类统计",content = "根据用户传递的参数根据异常的分类进行统计",logType = 1,authority = "1235")
    public Result cruiseStatisticalByAbnormal() {
        Result result = new Result();
        try {
            List<CruiseStatistical> cruiseStatisticalList = uPatrolResultService.cruiseStatisticalByAbnormal();
            result.setData(cruiseStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询--巡视结果任务详情查询")
    @GetMapping(value = "/selectCruiseByPage")
    public Result selectCruiseByPage(@RequestParam(value = "taskId", required = false) String taskId,
                                     @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                     @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult,
                                     @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                     @RequestParam(value = "startTime",required = false) String startTime,
                                     @RequestParam(value = "endTime",required = false) String endTime,
                                     @RequestParam(value = "regionId",required = false) Long regionId,
                                     @RequestParam(value = "customId",required = false) String customId,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();

        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出","巡视结果任务详情导出");
            }else{
                logsRecord.LogsSend(request,"1","巡视结果任务详情查询","根据用户传递的参数查询巡视结果任务详情");
            }
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
            List<Long> deviceIdList = new ArrayList<>();
            if (regionId == null){
                deviceIdList =  null;
            }else {
                //判断是否是根节点
                TStdRegion rootRegion = tStdRegionService.selectByPrimaryId(regionId);
                if (rootRegion != null && rootRegion.getUpRegionId() == -1){
                    deviceIdList = null;
                }else {
                    List<Long> regionIdList = tStdRegionService.selectDownId(regionId);//查询该regionId的子节点
                    if (regionIdList != null && !regionIdList.isEmpty()) {
                        deviceIdList = tStdDeviceService.selectDeviceIdListByRegion(regionIdList);
                        if (deviceIdList.isEmpty()){
                            //此区域下没有设备
                            deviceIdList.add(-1L);
                        }
                    } else {
                        deviceIdList.add(regionId);
                    }
                }
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultDetail> cruiseResultDetailList = uPatrolResultService.selectCruiseByPage(taskId,cruiseType,cruiseResult,deviceType,startTime,endTime,deviceIdList,customId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultDetailList);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询--识别异常点位")
    @GetMapping(value = "/selectAbnormalResult")
    public Result selectAbnormalResult(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                                     @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                     @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult,
                                     @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                       @RequestParam(value = "instanceName", required = false) String instanceName,
                                     @RequestParam(value = "startTime",required = false) String startTime,
                                     @RequestParam(value = "endTime",required = false) String endTime,
                                     @RequestParam(value = "regionId",required = false) Long regionId,
                                     @RequestParam(value = "customId",required = false) String customId,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();

        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出","巡视结果异常点位导出");
            }else{
                logsRecord.LogsSend(request,"1","识别异常点位查询","根据用户传递的参数查询识别异常点位");
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
            List<CruiseResultDetail> cruiseResultDetailList = uPatrolResultService.selectAbnormalResult(taskResultId,cruiseType,cruiseResult,deviceType,instanceName,startTime,endTime,deviceIdList,customId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultDetailList);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "人工复核")
    @PostMapping(value = "/manualReview")
    @Logs(title = "人工复核",content = "人工复核",logType = 5,authority = "1235")
    public Result manualReview(@RequestBody CruiseManualReview cruiseManualReview,HttpServletRequest request) {

        String userId = request.getHeader("userId");
        Result result = new Result();
        try {
            result.setData(uPatrolResultService.manualReview(cruiseManualReview,userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "A-查询正在执行的任务")
    @GetMapping(value = "/selectTaskIsRunning")
    @Logs(title = "查询正在执行的任务",content = "根据用户传递的参数查询正在执行的任务",logType = 1,authority = "1235")
    public Result selectTaskIsRunning(){
        Result result=new Result();
        try{
            result.setData(uPatrolResultService.selectTaskIsRunning());
        }catch(Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述",e);
        }

        return result;
    }
    @ApiOperation(value = "一键审核--任务下的所有巡视点")
    @GetMapping(value = "/manualReviewTask")
    @Logs(title = "审核任务",content = "一键审核",logType = 5)
    public Result manualReviewTask(@RequestParam(value = "taskResultId") String taskResultId,HttpServletRequest request){
        String userId = request.getHeader("userId");
        Result result=new Result();
        try{
            result.setData(uPatrolResultService.manualReviewTask(taskResultId,userId,request));
        }catch(Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("审核任务失败描述",e);
        }
        return result;
    }

    @ApiOperation(value = "下级系统巡视结果审核")
    @PostMapping(value = "/robotPatrolTaskReview")
    public Result robotPatrolTaskReview(@RequestBody List<CruiseManualReview> resultList) {
        Result result = new Result();
        try {
            log.info("The resultList from accessRobot is=={}", resultList);
            uPatrolResultService.manualReviewTask(resultList);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("接收并处理机器人/无人机/边缘节点巡视结果错误:", e);
        }
        return result;
    }

}
