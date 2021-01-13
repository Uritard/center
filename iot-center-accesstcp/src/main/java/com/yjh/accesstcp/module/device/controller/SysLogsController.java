package com.yjh.accesstcp.module.device.controller;

import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.result.ResultCodeEnum;
import com.yjh.accesstcp.module.device.entity.SysLogs;
import com.yjh.accesstcp.module.device.service.SysLogsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author tt
 * @since 2020-08-12
 */
@RestController
@RequestMapping("/sysLogs/v1")
@Api(value = "/sysLogs", description = "审计日志表操作接口")
public class SysLogsController {

    @Autowired
    private final SysLogsService sysLogsService;

    private Logger log = LoggerFactory.getLogger(SysLogsController.class);

    public SysLogsController(SysLogsService sysLogsService) {
        this.sysLogsService = sysLogsService;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "logId", required = true) String logId) {
        Result result = new Result();
        try {
            SysLogs sysLogs = sysLogsService.selectByPrimaryId(logId);
            result.setData(sysLogs);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

}
