package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.DeviceTreeCondition;
import com.yjh.platform.module.device.service.TStdDeviceTreeService;
import io.swagger.annotations.Api;
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

    @GetMapping(value = "/selectDevTree")
    public Result selectDevTree(@RequestBody DeviceTreeCondition deviceTreeCondition) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceTreeService.selectDevTree(deviceTreeCondition));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设备树查询失败描述：", e);
        }
        return result;
    }
}
