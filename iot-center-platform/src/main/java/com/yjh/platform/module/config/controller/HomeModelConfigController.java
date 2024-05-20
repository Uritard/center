package com.yjh.platform.module.config.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.config.entity.HomeModelConfig;
import com.yjh.platform.module.config.entity.HomeModelConfigVO;
import com.yjh.platform.module.config.service.HomeModelConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/homeModelConfig/v1")
@Api(value = "/homeModelConfig", tags = "系统配置表操作接口")
public class HomeModelConfigController {
    @Resource
    private HomeModelConfigService homeModelConfigService;

    @ApiOperation(value = "查询用户首页配置")
    @PostMapping(value = "/selectByUserId")
    @Logs(title = "查询用户首页配置", content = "查询用户首页配置", logType = 1,authority = "1235")
    public Result selectByUserId(@RequestParam Long userId){
        Result result = new Result();
        try {
            result.setData(homeModelConfigService.selectByUserId(userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("查询用户首页配置出错：",e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "修改用户首页配置")
    @PostMapping(value = "/updateByUserId")
    @Logs(title = "修改用户首页配置", content = "修改用户首页配置", logType = 1,authority = "1235")
    public Result updateByUserId(@RequestBody HomeModelConfigVO homeModelConfig){
        Result result = new Result();
        try {
            result.setData(homeModelConfigService.updateByUserId(homeModelConfig));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("修改用户首页配置出错：",e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
}
