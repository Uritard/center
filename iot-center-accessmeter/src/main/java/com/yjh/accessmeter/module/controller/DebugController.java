package com.yjh.accessmeter.module.controller;

import com.google.common.annotations.VisibleForTesting;
import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.logs.Logs;
import com.yjh.accessmeter.module.dao.TMeterDao;
import com.yjh.accessmeter.module.device.entity.TMeter;
import com.yjh.accessmeter.module.feign.PlatformProxy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: lqh
 * @Date: 2023/11/13
 */
@RestController
@RequestMapping("/debug/v1")
@Api(value = "/debug", description = "电表操作接口")
@Slf4j
public class DebugController {

    private TMeterDao tMeterDao;
    @Resource
    private PlatformProxy platformProxy;

    public DebugController(TMeterDao tMeterDao){
        this.tMeterDao = tMeterDao;
    }

    @ApiOperation(value = "测试电表数据上报")
    @RequestMapping(value = "/test-meter-upload", method = RequestMethod.GET)
    public Result testMeterUpload(@RequestParam("id") Long id,
                                  @RequestParam("totalPositivePower") String totalPositivePower,
                                  @RequestParam("totalPositiveReactivePower") String totalPositiveReactivePower,
                                  @RequestParam("totalNegativePositivePower") String totalNegativePositivePower) {
        Result result = new Result();
        try {
            TMeter tMeter = tMeterDao.selectByPrimaryKey(id);
            tMeter.setTotalPositivePower(totalPositivePower);
            tMeter.setTotalPositiveReactivePower(totalPositiveReactivePower);
            tMeter.setTotalNegativePositivePower(totalNegativePositivePower);
            tMeter.setTotalPositivePowerDifferenceValue("120");
            platformProxy.uploadMeterInfo(tMeter);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("增加电表失败：", e);
        }
        return result;
    }

}
