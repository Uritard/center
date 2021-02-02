package com.yjh.accessudp.module.device.controller;

import com.yjh.accessudp.commons.result.Result;
import com.yjh.accessudp.commons.result.ResultCodeEnum;
import com.yjh.accessudp.module.device.service.SendFileServices;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2021/2/2
 */
@RestController
@RequestMapping("/sendFile/v1")
@Api(value = "/sendFile", description = "发送")
public class SendFileController {

    private Logger log = LoggerFactory.getLogger(SendFileController.class);
    @Autowired
    private SendFileServices sendFileServices;

    @ApiOperation(value = "将生成好的文件发送给反向隔离装置")
    @RequestMapping(value = "/sendFile", method = RequestMethod.POST)
    public Result sendFile(@RequestBody Map<String, List<String>> map){
        Result result = new Result();
        try {
            String path  = map.get("list").get(0);
            result.setData(this.sendFileServices.sendFile(path));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }
}
