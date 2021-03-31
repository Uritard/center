package com.yjh.gateway.module.controller;

import com.yjh.gateway.commons.result.Result;
import com.yjh.gateway.commons.result.ResultCodeEnum;
import com.yjh.gateway.module.service.RefreshRouteService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author tt
 * @Date 2019/6/4
 **/
@Api(value = "zuul路由管理", tags = {"zuul路由管理1"})
@RestController
@RequestMapping("/route")
public class RouteController {

    @Autowired
    private RefreshRouteService refreshRouteService;

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

    @PostMapping("/refreshRoute")
    public Result refreshRoute() {
        Result result = new Result();
        try {
            refreshRouteService.refreshRoute();
            result.setData("刷新成功");
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), "内部错误");
            log.error("login", e);
        }
        return result;
    }
}
