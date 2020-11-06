package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.ReportForms;
import com.yjh.platform.module.task.entity.TCruiseDataResultDetail;
import com.yjh.platform.module.task.service.ReportManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.conn.util.PublicSuffixList;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
@Api(value = "/reportManage", description = "报表管理接口")
public class ReportManageController {

    private Logger log = LoggerFactory.getLogger(ReportManageController.class);

    @Autowired
    private ReportManageService reportManageService;

    @Value("${nginx.report.reflect}")
    private String reportAbsolutePath;//报表绝对路径
    @Value("${nginx.reportRelative.reflect}")
    private String reportRelativePath;//报表相对路径
    @Value("${nginx.temporary.reflect}")
    private String temporaryPath;//临时路径

    public ReportManageController(ReportManageService reportManageService) {
        this.reportManageService = reportManageService;
    }

    @ApiOperation(value = "生成报表")
    @RequestMapping(value = "/reportGenerate", method = RequestMethod.GET)
    public Result reportGenerate(@RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                                 @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                 @RequestParam(value="deviceIds")String deviceIds,
                                 @RequestParam(value="reportName")String reportName,
                                 @RequestParam(value="reportType")String reportType) {
        Result result = new Result();
        try {
            List<TCruiseDataResultDetail> list = reportManageService.reportGenerate(startTime,endTime,deviceIds,reportName,reportType,reportAbsolutePath);
            result.setData(list);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("生成报表错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "下载报表")
    @RequestMapping(value = "/reportDownload", method = RequestMethod.GET)
    public Result reportDownload(@RequestParam(value = "reportName") String reportName,
                                 @RequestParam(value="reportType")String reportType,
                                 @RequestParam(value = "startTime")String startTime) {
        Result result = new Result();
        try {
            String downLoadFilePath = reportManageService.reportDownload(reportName,reportType,startTime,reportRelativePath);
            result.setData(downLoadFilePath);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("下载报表错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "查询报表生成记录")
    @RequestMapping(value = "/reportSelect", method = RequestMethod.GET)
    public Result reportSelect(@RequestParam(value = "reportName", required = false) String reportName,
                               @RequestParam(value = "startTime", required = false)String startTime,
                               @RequestParam(value = "endTime", required = false)String endTime,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String,Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<ReportForms> list = reportManageService.reportSelect(reportName,startTime,endTime,reportAbsolutePath);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询报表记录错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "删除报表")
    @RequestMapping(value = "/reportDelete", method = RequestMethod.DELETE)
    public Result reportDelete(@RequestParam(value = "reportName") String reportName,
                               @RequestParam(value="reportType")String reportType,
                               @RequestParam(value = "startTime")String startTime) {
        Result result = new Result();
        try {
            result.setData(reportManageService.reportDelete(reportName,reportType,startTime,reportAbsolutePath));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除报表错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "批量删除报表")
    @RequestMapping(value = "/reportBatchDelete", method = RequestMethod.DELETE)
    public Result reportBatchDelete(@RequestBody List<ReportForms> fileNameList) {
        Result result = new Result();
        try {
            result.setData(reportManageService.reportBatchDelete(reportAbsolutePath,fileNameList));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量删除报表错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "根据任务生成巡检记录报告")
    @RequestMapping(value = "/reportByTask", method = RequestMethod.GET)
    public Result reportByTask(@RequestParam(value="taskId")String taskId) {
        Result result = new Result();
        try {
            String filePath = reportManageService.reportByTask(taskId,temporaryPath);
            result.setData(filePath);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("生成报表错误:", e);
        }
        return result;
    }
    @RequestMapping(value = "/getDiZhi",method = RequestMethod.GET)
    public Result  getDiZhi(HttpServletRequest request){
        Result result = new Result();
        try {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int port = request.getServerPort();
            String path = scheme+"://"+serverName+":"+port;
            log.info("<-------------path------------->:"+path);
            result.setData(path);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取东西错误:", e);
        }
        return result;
    }
}
