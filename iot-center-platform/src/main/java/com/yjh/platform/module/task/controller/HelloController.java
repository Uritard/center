package com.yjh.platform.module.task.controller;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.CruiseTaskJob;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.websocket.WebSocketResult;
import com.yjh.platform.common.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import sun.misc.REException;

import java.util.HashMap;


@RestController
@Api("HelloController")
@RequestMapping("/hello")
public class HelloController {

    private Logger log = LoggerFactory.getLogger(HelloController.class);

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";

    @Value("${picModelPath.dir}")
    private String picModelPath;
    //等待相机转到预置位时间
    @Value("${waitTime}")
    private Long waitTime;

    @ApiOperation("说hello")
    @PostMapping("/admin")
    @ResponseBody
    public String sayHello(@ApiParam(value = "ceshi ",required = true) @RequestParam String name){
        WebSocketServer.sendMsg(name+"hello！");
        JSONObject jsonObject = JSONObject.fromObject(WebSocketResult.builder().type("1").info(name +"  很好!").build());
        WebSocketServer.sendMsg(jsonObject.toString());
        return name +"你好!";
    }


    @ApiOperation("说hello")
    @PostMapping("/test")
    @ResponseBody
    public String test(@RequestParam String name)throws Exception{
        QuartzTask quartzTask = new QuartzTask();
        quartzTask.setJobName(name);
        quartzTask.setJobGroup("qh111");
        JobManager jobManager = new JobManager();
        jobManager.addJob(quartzTask);
        return "ok";
    }
}
