package com.yjh.platform.module.task.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.handler.CustomCellWriteHandler;
import com.yjh.platform.common.handler.CustomCellWriteHeightConfig;
import com.yjh.platform.common.logs.Logs;
//import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.ReportManageService;
import com.yjh.platform.module.task.service.TCruiseResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;




/**
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCruiseResult/v1")
@Api(value = "/tCruiseResult")
public class TCruiseResultController {

    @Autowired
    private final TCruiseResultService tCruiseResultService;
    @Autowired
    private LogsRecord logsRecord;

    @Autowired
    private ReportManageService reportManageService;
    @Autowired
    private TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdRegionService tStdRegionService;

    private Logger log = LoggerFactory.getLogger(TCruiseResultController.class);

    public TCruiseResultController(TCruiseResultService tCruiseResultService) {
        this.tCruiseResultService = tCruiseResultService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "新增巡检任务结果数据",content = "根据用户传递的参数新增巡检任务结果数据",logType = 2)
    public Result insert(@Validated @RequestBody TCruiseResult tCruiseResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.insert(tCruiseResult));
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
    @Logs(title = "删除巡检任务结果数据",content = "根据用户传递的参数删除巡检任务结果数据",logType = 4)
    public Result delete(@RequestParam(value = "taskResultId", required = true) String taskResultId) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.deleteByPrimaryId(taskResultId));
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
    @Logs(title = "修改巡检任务结果数据",content = "根据用户传递的参数修改巡检任务结果数据",logType = 3)
    public Result update(@Validated @RequestBody TCruiseResult tCruiseResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.update(tCruiseResult));
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
    @Logs(title = "查询巡检任务结果数据",content = "根据用户传递的参数查询巡检任务结果信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "taskResultId", required = true) String taskResultId) {
        Result result = new Result();
        try {
            TCruiseResult tCruiseResult = tCruiseResultService.selectByPrimaryId(taskResultId);
            result.setData(tCruiseResult);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @GetMapping(value = "/select")
    @Logs(title = "查询巡检任务结果数据",content = "根据用户传递的参数查询巡检任务结果信息",logType = 1)
    public Result select(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                         @RequestParam(value = "taskId", required = false) String taskId,
                         @RequestParam(value = "taskName", required = false) String taskName,
                         @RequestParam(value = "areaId", required = false) String areaId,
                         @RequestParam(value = "cType", required = false) Integer cType,
                         @RequestParam(value = "cState", required = false) Integer cState,
                         @RequestParam(value = "modifyState", required = false) Integer modifyState,
                         @RequestParam(value = "taskCount", required = false) Integer taskCount,
                         @RequestParam(value = "taskWait", required = false) Integer taskWait,
                         @RequestParam(value = "checkUser", required = false) String checkUser,
                         @RequestParam(value = "checkDate", required = false) Date checkDate,
                         @RequestParam(value = "weather", required = false) String weather,
                         @RequestParam(value = "createTime", required = false) Date createTime,
                         @RequestParam(value = "executeTime", required = false) Date executeTime,
                         @RequestParam(value = "taskCode", required = false) String taskCode,
                         @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TCruiseResult> list = tCruiseResultService.select(taskResultId, taskId,taskName, areaId, cType, cState, modifyState, taskCount, taskWait, checkUser, checkDate, weather, createTime, executeTime, taskCode, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询--巡视结果任务查询")
    @GetMapping(value = "/selectTaskByPage")
   // @Logs(title = "查询巡检任务结果数据",content = "根据用户传递的参数分页查询巡检任务结果信息",logType = 1)
    public Result selectTaskByPage(@RequestParam(value = "taskName", required = false) String taskName,
                                   @RequestParam(value = "cState", required = false) Integer cState,
                                   @RequestParam(value = "cType", required = false) Integer cType,
                                   @RequestParam(value = "regionId",required = false) Long regionId,
                                   @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                   @RequestParam(value = "startDate",required = false) String startDate,
                                   @RequestParam(value = "endDate",required = false) String endDate,
                                   @RequestParam(value = "meteType", required = false) Integer meteType,
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
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCruiseResultExpand> list = tCruiseResultService.selectTaskByPage(taskName,cState,cType,deviceType,startDate,endDate,deviceIdList,meteType);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (BusinessException e) {
            result.setMessage(10008, "用户无权限");
            //log.error("日志统计失败：" + e);
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
            List<StatisticalResult> taskStatisticalList = tCruiseResultService.taskStatistical();
            result.setData(taskStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视点结果统计")
    @GetMapping(value = "/cruiseStatistical")
    @Logs(title = "巡视点结果统计",content = "根据用户传递的参数统计巡视点结果",logType = 1)
    public Result cruiseStatistical() {
        Result result = new Result();
        try {
            List<CruiseStatistical> cruiseStatisticalList = tCruiseResultService.cruiseStatistical();
            result.setData(cruiseStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视点结果统计--根据正常异常状态统计")
    @GetMapping(value = "/cruiseStatisticalByStatus")
    @Logs(title = "根据正常异常状态统计",content = "根据用户传递的参数统计正异常的巡视点",logType = 1)
    public Result cruiseStatisticalByStatus() {
        Result result = new Result();
        try {
            List<StatisticalTools> statisticalToolsList = tCruiseResultService.cruiseStatisticalByStatus();
            result.setData(statisticalToolsList);
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
            List<CruiseStatistical> cruiseStatisticalList = tCruiseResultService.cruiseStatisticalByAbnormal();
            result.setData(cruiseStatisticalList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "分页查询--巡视结果任务详情查询")
    @GetMapping(value = "/selectCruiseByPage")
   // @Logs(title = "巡视结果任务详情查询",content = "根据用户传递的参数查询巡视结果任务详情",logType = 1)
    public Result selectCruiseByPage(@RequestParam(value = "taskResultId", required = false) String taskResultId,
                                     @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                     @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult,
                                     @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                     @RequestParam(value = "startTime",required = false) String startTime,
                                     @RequestParam(value = "endTime",required = false) String endTime,
                                     @RequestParam(value = "regionId",required = false) Long regionId,
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
            List<CruiseResultDetail> cruiseResultDetailList = tCruiseResultService.selectCruiseByPage(taskResultId,cruiseType,cruiseResult,deviceType,startTime,endTime,deviceIdList);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultDetailList);
            result.setData(resultMap);
        }catch (BusinessException e) {
            result.setMessage(10008, "用户无权限");
            //log.error("日志统计失败：" + e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "人工复核")
    @PutMapping(value = "/manualReview")
    @Logs(title = "人工复核",content = "人工复核",logType = 5,authority = "1235")
    public Result manualReview(@RequestBody CruiseManualReview cruiseManualReview,HttpServletRequest request) {

        String userId = request.getHeader("userId");
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.manualReview(cruiseManualReview,userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "批量插入")
    @PostMapping(value = "/batchInsert")
    @Logs(title = "批量插入数据",content = "根据用户传递的参数批量插入数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCruiseResult> list) {
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：",e);
        }
        return result;
    }

    @ApiOperation(value = "A-查询正在执行的任务")
    @GetMapping(value = "/selectTaskIsRunning")
    @Logs(title = "查询正在执行的任务",content = "根据用户传递的参数查询正在执行的任务",logType = 1,authority = "1235")
    public Result selectTaskIsRunning(){
        Result result=new Result();
        try{
            result.setData(tCruiseResultService.selectTaskIsRunning());
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
            result.setData(tCruiseResultService.manualReviewTask(taskResultId,userId,request));
        }catch(Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("审核任务失败描述",e);
        }
        return result;
    }

    @ApiOperation(value = "操作任务记录查询")
    @RequestMapping(value = "/QueryOperationTask",method = RequestMethod.POST)
    public Result QueryOperationTask(@RequestBody JSONObject obj){

        Long regionId = obj.getLong("regionId");
        String operationType = obj.getString("operationType");
        String startTime= obj.getString("startTime");
        String endTime = obj.getString("endTime");
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try{
            //递归查询
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
            List<OperationTaskRecord> hashMaps = tCruiseResultService.QueryOperationTask(deviceIdList , operationType, startTime, endTime);
            resultMap.put("count",page.getTotal());
            resultMap.put("list",hashMaps);
            result.setData(resultMap);
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(),ResultCodeEnum.QUERYERROR.getName());
            log.error("操作任务记录查询失败描述",e);
        }
        return result;
    }

    @ApiOperation(value = "操作任务详情查询")
    @RequestMapping(value = "/QueryOperationResult",method = RequestMethod.POST)
    public Result QueryOperationResult(@RequestBody HashMap<String,String> map){
        String taskId = map.get("taskId");
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<OperationTaskRecordResult> hashMaps = tCruiseResultService.QueryOperationResult(taskId);
            resultMap.put("count",hashMaps.size());
            resultMap.put("list",hashMaps);
            result.setData(resultMap);
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(),ResultCodeEnum.QUERYERROR.getName());
            log.error("操作任务详情查询失败描述",e);
        }
       return result;
    }

    @ApiOperation(value = "操作任务详情导出")
    @RequestMapping(value = "/operationResultExport",method = RequestMethod.GET)
    public Result operationResultExport (@RequestParam(value = "taskId",required = false) String taskId){
        Result result = new Result();
        try {
            result.setData(tCruiseResultService.downLoadOperationDetailReport(taskId));
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(),ResultCodeEnum.QUERYERROR.getName());
            log.error("操作任务详情查询失败描述",e);
        }
        return result;
    }
}
