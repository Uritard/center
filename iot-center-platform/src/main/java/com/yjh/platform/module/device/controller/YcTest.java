package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.service.TStdDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 丫C
 * @date 2023/7/6
 */
@RestController
@RequestMapping("/YcTest/v1")
@Api(value = "/YcTest", tags = "测试设备数")
public class YcTest {
    private final TStdDeviceService tStdDeviceService;

    private Logger log = LoggerFactory.getLogger(YcTest.class);

    public YcTest(TStdDeviceService tStdDeviceService) {
        this.tStdDeviceService = tStdDeviceService;
    }

    @GetMapping(value = "/selectDevTree")
    public Result selectDevTree(@RequestParam(value = "level") String level,
                                @RequestParam(value = "deviceShow", required = false) String deviceShow,
                                @RequestParam(value = "deviceType", required = false) String deviceType,
                                @RequestParam(value = "analyseType", required = false) String analyseType,
                                @RequestParam(value = "id", required = false) Long id) {
        Result result = new Result();
        try {
            List<AreaInfo> devTreeList = tStdDeviceService.selectDevTest(level, deviceShow, deviceType, analyseType, id);
            result.setData(devTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }




}
