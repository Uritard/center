package com.yjh.Manager.module.controller;

import com.yjh.Manager.common.result.Result;
import com.yjh.Manager.common.result.ResultCodeEnum;
import com.yjh.Manager.module.entity.TCfgAccess;
import com.yjh.Manager.module.service.TCfgAccessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;


/**
 * @author tt
 * @since 2020-08-17
 */
@RestController
@RequestMapping("/tCfgAccess/v1")
@Api(value = "/tCfgAccess", description = "接入配置表操作接口")
public class AccessManagerController {

    @Autowired
    private final TCfgAccessService tCfgAccessService;

    private Logger log = LoggerFactory.getLogger(AccessManagerController.class);

    public AccessManagerController(TCfgAccessService tCfgAccessService) {
        this.tCfgAccessService = tCfgAccessService;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "accessId", required = false) Long accessId,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "projectId", required = false) Long projectId,
                            @RequestParam(value = "url", required = false) String url,
                            @RequestParam(value = "port", required = false) String port,
                            @RequestParam(value = "appId", required = false) String appId,
                            @RequestParam(value = "secret", required = false) String secret,
                            @RequestParam(value = "accessCode", required = false) String accessCode,
                            @RequestParam(value = "userId", required = false) Long userId,
                            @RequestParam(value = "userName", required = false) String userName,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                            @RequestParam(value = "remark", required = false) String remark,
                            @RequestParam(value = "state", required = false) String state) {
        Result result = new Result();
        try {
            List<TCfgAccess> list = tCfgAccessService.select(accessId, name, projectId, url, port, appId, secret, accessCode, userId, userName, createTime, updateTime, remark, state);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
