package com.yjh.accesstcp.module.device.controller;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.result.ResultCodeEnum;
import com.yjh.accesstcp.module.device.entity.SysLogs;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.module.device.service.SysLogsService;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2021/1/12
 */
@RestController
@RequestMapping("/sendToUpSystem/v1")
@Api(value = "/sendToUpSystem", description = "上报服务")
public class SendToUpSystemController {

    @Autowired
    private final SendToUpSystemServices sendToUpSystemService;

    private Logger log = LoggerFactory.getLogger(SendToUpSystemController.class);

    public SendToUpSystemController(SendToUpSystemServices sendToUpSystemService) {
        this.sendToUpSystemService = sendToUpSystemService;
    }

    @ApiOperation(value = "发送")
    @RequestMapping(value = "/send", method = RequestMethod.GET)
    public Result send(@RequestParam(value = "list", required = true) List<Map<String,Object>> list,
                       @RequestParam(value = "type", required = true) String type,
                       @RequestParam(value = "command", required = false) String command,
                       @RequestParam(value = "code", required = false) String code) {
        Result result = new Result();
        try {
            sendToUpSystemService.sendResponse(type,command,code,list);
            result.setData(1);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "发送")
    @RequestMapping(value = "/sendXML", method = RequestMethod.POST)
    public Result sendXML(@RequestBody Map<String,List<XMLBaseModel>> robotMap) {
        Result result = new Result();
        try {
            XMLBaseModel xmlBaseModel = robotMap.get("list").get(0);
            if("21".equals(xmlBaseModel.getType())){
                Constant.weatherXmlModel = xmlBaseModel;
                result.setData(1);
                return result;
            }
            sendToUpSystemService.sendXML(xmlBaseModel);
            result.setData(1);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("发送失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "模型同步")
    @RequestMapping(value = "/creatFile", method = RequestMethod.GET)
    public Result creatFile() {
        Result result = new Result();
        try {
            result.setData(sendToUpSystemService.creatFile());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视结果查询")
    @RequestMapping(value = "/testCount", method = RequestMethod.GET)
    public Result testCount(@RequestParam(value = "type") String type,
                            @RequestParam(value = "startTime") String startTime,
                            @RequestParam(value = "endTime") String endTime) {
        Result result = new Result();
        try {
            result.setData(sendToUpSystemService.resultStatistical(type,startTime,endTime));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "模型同步上报")
    @RequestMapping(value = "/modelUpload", method = RequestMethod.GET)
    public Result modelUpload(@RequestParam(value = "type") String type) {
        Result result = new Result();
        try {
           sendToUpSystemService.creatFile(type);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

}
