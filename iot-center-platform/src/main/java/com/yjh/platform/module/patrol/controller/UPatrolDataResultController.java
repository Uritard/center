package com.yjh.platform.module.patrol.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.aop.handler.CustomCellWriteHandler;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.ImageConverter;
import com.yjh.platform.common.utils.MyUrlImageConverter;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.common.utils.smUtil.report.ExportUtil;
import com.yjh.platform.common.utils.smUtil.report.FileUtil;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.patrol.service.UPatrolDataResultService;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeMeteInfo;
import com.yjh.platform.module.task.entity.FirAndPicInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
            List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = uPatrolDataResultService.selectCruiseResultAnalyze(deviceIdList,deviceType,meteType,meterType,cruiseRes,customId);
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
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,
                                         @RequestParam(value = "stationName", required = false) String stationName) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();

        try {

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
            List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = uPatrolDataResultService.selectCruiseDataReport(cType,meteType,meterType,endTime, startTime,deviceIdList,instanceName, stationName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultAnalyzeInfoList);
            result.setData(resultMap);
        }catch (Exception e) {
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
                                         @RequestParam(value = "typeString") String typeString) {

        Result result = new Result();
        try {
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
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

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    //创建本地文件路径
                    Map<String, String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
                    String filePathAndName = resMap.get("content") + File.separator;
                    //创建文件夹
                    FileUtil.createDirectory(filePathAndName);
                    //拼接Excel文件名
                    String fileName = "巡检点列表数据" + System.currentTimeMillis() + ".xlsx";
                    String fileNamePath = filePathAndName + fileName;

                    //设置表头
                    List<List<String>> heads = Lists.newArrayList();
                    List<String> strings = Arrays.asList(typeString.split(","));
                    strings.forEach(s -> heads.add(Lists.newArrayList(s)));
                    //设置内容
                    List<List<String>> contents = Lists.newArrayList();
                    cruiseResultAnalyzeInfoList.forEach(cruiseResult -> {
                        List<String> content = Lists.newArrayList();
                        strings.forEach(s -> content.add(String.valueOf(cruiseResult.get(ExportUtil.map.get(s)))));
                        contents.add(content);
                    });
                    ExcelWriter excelWriter = EasyExcel.write(fileNamePath).build();
                    WriteSheet writeSheet = EasyExcel.writerSheet(0, "巡检点列表数据")
                            .includeColumnFiledNames(ExportUtil.getCruiseDataReportModel(strings))
                            .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
                            .registerWriteHandler(new SimpleRowHeightStyleStrategy((short) 25, (short) 100))
                            .registerConverter(new ImageConverter())
                            .head(heads).registerWriteHandler(ExportUtil.getCellStyle()).build();
                    excelWriter.write(contents, writeSheet);
                    //关闭写excel
                    excelWriter.finish();
                    Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
                    String fileRelativePath = map.get("content") + "/" + fileName;

                    Map<String, String> jasonMap = new HashMap<>(2);
                    jasonMap.put("type", "cruiseDataReport");
                    jasonMap.put("url", fileRelativePath);
                    try {
                        Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            ThreadPoolUtil.COMMON_POOL.addThread(runnable);

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
            List<CruiseResultAnalyzeInfo>  list = uPatrolDataResultService.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId, meteType,meterType,endTime, startTime,pageNum,pageSize);
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
            result.setData(uPatrolDataResultService.selectBrokenLine(cruiseType, cType, deviceMeteId, startTime, endTime,meteType,meterType));
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
            List<FirAndPicInfo> list = uPatrolDataResultService.selectByCameraId(cameraId,startDate,endDate,fileName);
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
