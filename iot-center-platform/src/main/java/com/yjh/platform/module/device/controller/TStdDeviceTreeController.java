package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.InspectedDevTreeCondition;
import com.yjh.platform.module.device.entity.PatrolDevTreeCondition;
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

    @ApiOperation(value = "巡视设备树")
    @GetMapping(value = "/selectPatrolDevTree")
    public Result selectPatrolDevTree(@RequestBody PatrolDevTreeCondition patrolDevTreeCondition,
                                      HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = NumberUtils.toLong(request.getHeader("userId"));
            result.setData(tStdDeviceTreeService.selectPatrolDevTree(patrolDevTreeCondition, userId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视设备树查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "被巡视设备树")
    @GetMapping(value = "/selectInspectedDevTree")
    public Result selectInspectedDevTree(@RequestBody InspectedDevTreeCondition deviceTreeCondition) {
        Result result = new Result();
        try {
            if (String.valueOf(-1).equals(deviceTreeCondition.getMeteType())){
                deviceTreeCondition.setMeteType(null);
            }
            result.setData(tStdDeviceTreeService.selectInspectedDevTree(deviceTreeCondition));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("被巡视设备树查询失败描述：", e);
        }
        return result;
    }
}
