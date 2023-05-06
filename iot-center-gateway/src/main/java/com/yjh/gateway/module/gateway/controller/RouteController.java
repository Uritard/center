package com.yjh.gateway.module.gateway.controller;

import com.yjh.gateway.common.authFilter.zuulFilter;
import com.yjh.gateway.common.websocket.WebSocketServer;
import com.yjh.gateway.commons.result.BusinessException;
import com.yjh.gateway.commons.result.Result;
import com.yjh.gateway.commons.result.ResultCodeEnum;
import com.yjh.gateway.module.gateway.service.RefreshRouteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

/**
 * @Description
 * @Author tt
 * @Date 2019/6/4
 **/
@Api(value = "zuul路由管理", tags = {"zuul路由管理"})
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

    @ApiOperation(value = "同步到websocket")
    @RequestMapping(value = "/syncWebsocket", method = RequestMethod.POST)
    public Result syncWebsocketInfo(@RequestParam(value = "json") String json) {
        Result result = new Result();
        try {
            WebSocketServer.sendMsg(json);
            result.setData("同步到websocket");
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("同步到websocket异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("同步到websocket错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "通过websocket发送ByteBuffer")
    @PostMapping(value = "/sendMsgBuffer")
    public Result sendMsgBuffer(HttpServletRequest request) {
        Result result = new Result();
        try {
            InputStreamReader reader = new InputStreamReader(request.getInputStream());
            byte[] bytes = IOUtils.toByteArray(reader);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            WebSocketServer.sendMsgBuffer(byteBuffer);
            result.setData("通过websocket发送ByteBuffer成功");
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("通过websocket发送ByteBuffer异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("通过websocket发送ByteBuffer错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "刷新安全标记")
    @RequestMapping(value = "/reloadSecureSigns", method = RequestMethod.GET)
    public Result reloadSecureSigns() {
        Result result = new Result();
        try {
            zuulFilter.reloadSecureSigns();
            result.setData("刷新安全标识");
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("同步到websocket异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("同步到websocket错误:", e);
        }
        return result;
    }
}
