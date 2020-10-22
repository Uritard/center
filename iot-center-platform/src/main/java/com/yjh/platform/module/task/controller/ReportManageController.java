package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.ReportForms;
import com.yjh.platform.module.task.entity.TestReportMange;
import com.yjh.platform.module.task.service.ReportManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
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

    public ReportManageController(ReportManageService reportManageService) {
        this.reportManageService = reportManageService;
    }

    @ApiOperation(value = "生成报表")
    @RequestMapping(value = "/reportGenerate", method = RequestMethod.GET)
    public Result reportGenerate(@RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                                 @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                 @RequestParam(value="deviceIds", required = false)String deviceIds,
                                 @RequestParam(value="reportName", required = false)String reportName) {
        Result result = new Result();
        try {
            List<Map<String, Object>> list = reportManageService.reportGenerate(startTime,endTime,deviceIds,reportName);
            result.setData(list);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "下载报表")
    @RequestMapping(value = "/reportDownload", method = RequestMethod.GET)
    public Result reportDownload() {
        Result result = new Result();
        try {
//            List<TestReportMange> list = reportManageService.selectAll(startTime,endTime,deviceIds);
//            EasyPoiUtil.exportExcel(list,"XXX报告单", "啥也不是","testFile", TestReportMange.class, true, true);
//            result.setData(list);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "查询报表生成记录")
    @RequestMapping(value = "/reportSelect", method = RequestMethod.GET)
    public Result reportSelect(@RequestParam(value = "reportName", required = false) String reportName,
                               @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                               @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime) {
        Result result = new Result();
        try {
            List<ReportForms> list = reportManageService.reportSelect(reportName,startTime,endTime);
            result.setData(list);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "删除报表")
    @RequestMapping(value = "/reportDelete", method = RequestMethod.GET)
    public Result reportDelete() {
        Result result = new Result();
        try {
//            List<TestReportMange> list = reportManageService.selectAll();
//            result.setData(list);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }
}
