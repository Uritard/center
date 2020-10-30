package com.yjh.Manager.module.controller;

import com.yjh.Manager.common.result.Result;
import com.yjh.Manager.common.result.ResultCodeEnum;
import com.yjh.Manager.module.service.ManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author tt
 * @since 2020-08-17
 */
@RestController
@RequestMapping("/managerService/v1")
@Api(value = "/statusInfo", description = "服务信息")
public class ManagerServiceController {

    @Autowired
    private final ManagerService managerService;

    private Logger log = LoggerFactory.getLogger(ManagerServiceController.class);

    public ManagerServiceController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @ApiOperation(value = "查询服务状态")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select() {
        Result result = new Result();
        try {
            result.setData(managerService.select());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}

