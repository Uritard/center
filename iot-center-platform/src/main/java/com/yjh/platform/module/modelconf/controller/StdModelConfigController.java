package com.yjh.platform.module.modelconf.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.modelconf.service.StdModelConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/4/15
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/stdModelConfig/v1")
@Api(value = "stdModelConfig", tags = "模型配置表对象功能接口")
@Slf4j
public class StdModelConfigController {

    @Resource
    private StdModelConfigService stdModelConfigService;

    @ApiOperation(value = "查询默认模型配置信息")
    @GetMapping(value = "/getDefaultConfig")
    public Result selectDevTree(@RequestParam(value = "droneId") Long droneId) {
        Result result = new Result();
        try {
            result.setData(stdModelConfigService.getDefaultConfig(droneId));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询默认模型配置信息失败描述：", e);
        }
        return result;
    }
}
