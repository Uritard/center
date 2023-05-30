package com.yjh.platform.module.config.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.config.entity.SystemConfig;
import com.yjh.platform.module.config.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * <p>
 * 系统配置表 前端控制器
 * </p>
 *
 * @author 
 * @since 2023-04-12
 */
@Slf4j
@RestController
@RequestMapping("/systemConfig/v1")
@Api(value = "/systemConfig", tags = "系统配置表操作接口")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @ApiOperation(value = "查询系统配置")
    @PostMapping(value = "/select")
    public Result select(){
        Result result = new Result();
        try {
            result.setData(systemConfigService.selectConfigInfo());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("查询系统配置出错：",e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }


    @ApiOperation(value = "修改系统配置")
    @PostMapping(value = "/update")
    public Result update( @RequestBody List<SystemConfig> systemConfig){
        Result result = new Result();
        try {
            result.setData(systemConfigService.batchUpdate(systemConfig));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("修改系统配置出错：",e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

}
