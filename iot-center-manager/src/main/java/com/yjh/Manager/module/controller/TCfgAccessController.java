package com.yjh.Manager.module.controller;

import com.yjh.Manager.module.entity.TCfgAccess;
import com.yjh.Manager.module.service.TCfgAccessService;
import com.yjh.Manager.common.result.BusinessException;
import com.yjh.Manager.common.result.Result;
import com.yjh.Manager.common.result.ResultCodeEnum;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author tt
 * @since 2020-08-17
 */
@RestController
@RequestMapping("/tCfgAccess/v1")
@Api(value = "/tCfgAccess", description = "接入配置表操作接口")
public class TCfgAccessController {

    @Autowired
    private final TCfgAccessService tCfgAccessService;

    private Logger log = LoggerFactory.getLogger(TCfgAccessController.class);

    public TCfgAccessController(TCfgAccessService tCfgAccessService) {
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
