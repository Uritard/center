package com.yjh.accesstcp.module.device.controller;

import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.result.ResultCodeEnum;
import com.yjh.accesstcp.module.device.entity.SysLogs;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
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

import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2021/1/12
 */
@RestController
@RequestMapping("/sendToUpSystem/v1")
@Api(value = "/sendToUpSystem", description = "审计日志表操作接口")
public class SendToUpSystemController {

    @Autowired
    private final SendToUpSystemServices sendToUpSystemService;

    private Logger log = LoggerFactory.getLogger(SendToUpSystemController.class);

    public SendToUpSystemController(SendToUpSystemServices sendToUpSystemService) {
        this.sendToUpSystemService = sendToUpSystemService;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "list", required = true) List<Map<String,Object>> list,
                                    @RequestParam(value = "msgType", required = true) String msgType) {
        Result result = new Result();
        try {
            sendToUpSystemService.send(list,msgType);
            result.setData(1);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

}
