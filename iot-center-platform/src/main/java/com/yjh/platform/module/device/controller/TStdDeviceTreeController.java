package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.DevSynthesisTreeCondition;
import com.yjh.platform.module.device.service.TStdDeviceTreeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * 设备树统一接口查询
 *
 * @author 丫C
 * @date 2023/06/29
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/tStdDeviceTree/v1")
@Api(value = "/tStdDeviceTree", tags = "设备树统一接口查询")
public class TStdDeviceTreeController {

    private final TStdDeviceTreeService tStdDeviceTreeService;

    private final Logger log = LoggerFactory.getLogger(TStdDeviceTreeController.class);

    public TStdDeviceTreeController(TStdDeviceTreeService tStdDeviceTreeService) {
        this.tStdDeviceTreeService = tStdDeviceTreeService;
    }

    @ApiOperation(value = "设备综合树")
    @PostMapping(value = "/selectDevSynthesisTree")
    public Result selectDevSynthesisTree(@RequestBody DevSynthesisTreeCondition deviceTreeCondition,
                                         HttpServletRequest request) {
        Result result = new Result();
        try {
            // 非同源告警趋势对比类该值为-1
            if (String.valueOf(-1).equals(deviceTreeCondition.getMeteType())) {
                deviceTreeCondition.setMeteType(null);
            }
            Long userId = NumberUtils.toLong(request.getHeader("userId"));
            result.setData(tStdDeviceTreeService.selectDevSynthesisTree(deviceTreeCondition, userId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设备综合树查询失败描述：", e);
        }
        return result;
    }
}
