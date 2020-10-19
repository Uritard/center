package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.websocket.WebSocketResult;
import com.yjh.platform.common.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;


@RestController
@Api("HelloController")
@RequestMapping("/hello")
public class HelloController {

    @ApiOperation("说hello")
    @PostMapping("/admin")
    @ResponseBody
    public String sayHello(@ApiParam(value = "ceshi ",required = true) @RequestParam String name){
        WebSocketServer.sendMsg(name+"hello！");
        JSONObject jsonObject = JSONObject.fromObject(WebSocketResult.builder().type("1").info(name +"  很好!").build());
        WebSocketServer.sendMsg(jsonObject.toString());
        return name +"你好!";
    }


}
