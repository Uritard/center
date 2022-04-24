package com.yjh.accessrobot.module.command.controller;


import com.yjh.accessrobot.common.smUtil.Demo;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.service.NonhomologousWarnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/robot/v1")
@Api(value = "/NonhomologousWarn", tags = "非同源告警入库接口")
public class TestController {

    @Autowired
    private final NonhomologousWarnService nonhomologousWarnService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Demo demo;

    private Logger log = LoggerFactory.getLogger(TestController.class);

    public TestController(NonhomologousWarnService nonhomologousWarnService) { this.nonhomologousWarnService = nonhomologousWarnService; }

    @ApiOperation(value = "非同源告警入库接口")
    @GetMapping(value = "/add")
    public Result feignRobotControl(HttpServletRequest request,
                                    @RequestParam(value = "identifier",required = false) String identifier) {
        Result result = new Result();
        try {
            Map<String,String> cruiseResultMap= new HashMap<>();
            cruiseResultMap.put("patrolDeviceName", "111");
            cruiseResultMap.put("patrolDeviceCode", "111");
            cruiseResultMap.put("robotCode","A200-0008");
            cruiseResultMap.put("taskName", "测试任务1");
            cruiseResultMap.put("taskCode", "2f3f5054705946d4956c1dc10e7484e3");
            cruiseResultMap.put("deviceName", "精度测试/精度-指针电流表");
            cruiseResultMap.put("deviceId", "73564857BFCE4ED6B4A7C9C7B29836D6");
            // 2022过检 新增字段value_type 0:默认值类型 11:局放放电频次 12:局放信号峰值 13:局放信号均值
            cruiseResultMap.put("valueType", "0");
            cruiseResultMap.put("value", "1.3");
            cruiseResultMap.put("valueUnit", "1.3A");
            cruiseResultMap.put("unit", "A");
            cruiseResultMap.put("time", "");
            cruiseResultMap.put("recognitionType", "3");
            cruiseResultMap.put("fileType", "1");
            cruiseResultMap.put("rectangle", "");
            cruiseResultMap.put("taskPatrolledId", "2f3f5054705946d4956c1dc10e7484e3aaa");
            cruiseResultMap.put("valid", "1");
            nonhomologousWarnService.insertNonhomologousWarn(cruiseResultMap, 1);
            result.setData("success");
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("非同源告警入库接口调用错误:", e);
        }
        return result;
    }
}
