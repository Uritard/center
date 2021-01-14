package com.yjh.accessvideo.module.device.controller;


import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.commons.utils.http.HttpClientUtils;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.service.AnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/analysis/v1")
@Api(value = "/analysis", description = "算法调用操作接口")
public class AQDTest {
    @Value("http://192.168.33.241:800/PSIA/Custom/SelfExt/AS/VQDDiagnose/queryStatus")
    private String URL;

    @ApiOperation(value = "视频诊断测试接口")
    @RequestMapping(value = "/AQDTest",method = RequestMethod.GET)
    public Result AQDTest() throws IOException {
        Result result=new Result();
        String services= HttpClientUtils.getInstance().getUrl(URL,null);
        result.setData(services);
        return result;
    }



}
