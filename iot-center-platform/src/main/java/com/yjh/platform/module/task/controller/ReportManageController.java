package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TReportInfo;
import com.yjh.platform.module.task.service.ReportManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @date 2020/10/20 - 20:13
 */
@RestController
@RequestMapping("/reportManage/v1")
@Api(value = "/reportManage")
public class ReportManageController {

    private Logger log = LoggerFactory.getLogger(ReportManageController.class);

    @Autowired
    private ReportManageService reportManageService;

    public ReportManageController(ReportManageService reportManageService) {
        this.reportManageService = reportManageService;
    }

    @ApiOperation(value = "生成报表")
    @GetMapping(value = "/reportGenerate")
    @Logs(title = "生成报表",content = "生成报表",logType = 2,authority = "1235")
    public Result reportGenerate(@RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                                 @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                 @RequestParam(value="deviceIds")String deviceIds,
                                 @RequestParam(value="reportName")String reportName,
                                 @RequestParam(value="reportType")String reportType) {
        Result result = new Result();
        try {
            int generateResult = reportManageService.reportGenerate(startTime,endTime,deviceIds,reportName,reportType);
            result.setData(generateResult);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("生成报表发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "下载报表")
    @GetMapping(value = "/reportDownload")
    @Logs(title = "下载报表",content = "下载报表",logType = 9)
    public Result reportDownload(@RequestParam(value = "reportId") String reportId) {
        Result result = new Result();
        try {
            String downLoadFilePath = reportManageService.reportDownload(reportId);
            result.setData(downLoadFilePath);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("下载报表错误:", e);
        }
        return result;
    }
    //    @ApiOperation(value = "查询报表生成记录")
//    @RequestMapping(value = "/reportSelect", method = RequestMethod.GET)
//    public Result reportSelect(@RequestParam(value = "reportName", required = false) String reportName,
//                               @RequestParam(value = "startTime", required = false)String startTime,
//                               @RequestParam(value = "endTime", required = false)String endTime,
//                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
//                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
//        Result result = new Result();
//        Map<String,Object> resultMap = new HashMap<>();
//        try {
//            String url = "ls "+reportAbsolutePath+" | wc -w";
//            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
//            BufferedReader readerForId = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
//            String lineForId = null;
//            while ((lineForId = readerForId.readLine()) != null) {
//                long count = Long.parseLong(lineForId);
//                resultMap.put("count", count);
//            }
//            Page page = PageHelper.startPage(pageNum, pageSize);
//            List<TReportInfo> list = reportManageService.reportSelect(reportName,startTime,endTime,reportAbsolutePath);
//            resultMap.put("count", page.getTotal());
//            resultMap.put("list", list);
//            result.setData(resultMap);
//        } catch (BusinessException b) {
//            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
//        } catch (Exception e) {
//            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
//            log.error("查询报表记录错误:", e);
//        }
//        return result;
//    }
    @ApiOperation(value = "查询报表生成记录")
    @GetMapping(value = "/reportSelect")
    @Logs(title = "查询报表生成记录",content = "根据用户传递的参数查询报表生成记录",logType = 1,authority = "1235")
    public Result reportSelect(@RequestParam(value = "reportName", required = false) String reportName,
                               @RequestParam(value = "startTime", required = false) String startTime,
                               @RequestParam(value = "endTime", required = false) String endTime,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TReportInfo> list = reportManageService.reportSelect(reportName,startTime,endTime);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "删除报表")
    @PostMapping(value = "/reportDelete")
    @Logs(title = "删除报表",content = "根据用户传递的参数删除报表",logType = 4)
    public Result reportDelete(@RequestParam(value = "reportId") String reportId) {
        Result result = new Result();
        try {
            result.setData(reportManageService.reportDelete(reportId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除报表错误:", e);
        }
        return result;
    }
    //    @ApiOperation(value = "批量删除报表")
//    @RequestMapping(value = "/reportBatchDelete", method = RequestMethod.POST)
//    public Result reportBatchDelete(@RequestBody List<TReportInfo> fileNameList) {
//        Result result = new Result();
//        try {
////            result.setData(reportManageService.reportBatchDelete(reportAbsolutePath,fileNameList));
//        } catch (BusinessException b) {
//            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
//        } catch (Exception e) {
//            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
//            log.error("批量删除报表错误:", e);
//        }
//        return result;
//    }
    @ApiOperation(value = "审核完成后根据任务生成巡检记录报告")
    @GetMapping(value = "/reportByTask")
    @Logs(title = "审核报表",content = "审核完成后根据任务生成巡检记录报告",logType = 5)
    public Result reportByTask(@RequestParam(value="taskId")String taskId) {
        Result result = new Result();
        try {
            String filePath = reportManageService.cruiseReportGenerate(taskId);
            result.setData(filePath);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("审核完成后根据任务生成巡检记录报告发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "下载巡视报告")
    @GetMapping(value = "/downLoadCruiseReport")
    @Logs(title = "下载巡视报告",content = "下载巡视报告",logType = 9,authority = "1235")
    public Result downLoadCruiseReport(@RequestParam(value="taskId")String taskId,@RequestParam(value = "remark")String remark) {
        Result result = new Result();
        try {
            result = reportManageService.downLoadCruiseReport(taskId,remark);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("下载巡视报告发生错误:", e);
        }
        return result;
    }

   /* @ApiOperation(value = "巡视结果分析报表生成")
    @GetMapping(value = "/cruiseResultAnalyseReporter")
    @Logs(title = "巡视结果分析报表生成",content = "巡视结果分析报表生成",logType = 5)
    public Result cruiseResultAnalyseReporter(@RequestParam(value = "deviceMeteId")Long deviceMeteId){
        Result result=new Result();
        try {
            result.setData(reportManageService.cruiseResultAnalyseReporter(deviceMeteId));

        }catch (Exception e){
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("报表生成失败：",e);
        }
        return result;
    }*/
}
