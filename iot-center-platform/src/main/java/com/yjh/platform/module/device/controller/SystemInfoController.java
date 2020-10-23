package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.SystemInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lqh
 * @since 2020/10/20
 */
@RestController
@RequestMapping("/systemInfo/v1")
@Api(value = "/systemInfo", description = "系统监控")
public class SystemInfoController {

    @Autowired
    private SystemInfoService systemInfoService;

    private Logger log = LoggerFactory.getLogger(SystemInfoController.class);

    public SystemInfoController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }

    @ApiOperation(value = "获取内存信息")
    @RequestMapping(value = "/getMemory", method = RequestMethod.GET)
    public Result getMemory() {
        Result result = new Result();
        try {
            result.setData(systemInfoService.getMemory());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取内存信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取cpu信息")
    @RequestMapping(value = "/getCPU", method = RequestMethod.GET)
    public Result getCPU() {
        Result result = new Result();
        try {
            result.setData(systemInfoService.getCPU());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取cpu信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取磁盘信息")
    @RequestMapping(value = "/getDisk", method = RequestMethod.GET)
    public Result getDisk() {
        Result result = new Result();
        try {
            result.setData(systemInfoService.getSwap());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取磁盘信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取cpu利用率")
    @RequestMapping(value = "/getCpuOnUse", method = RequestMethod.GET)
    public Result getCpuOnUse() {
        Result result = new Result();
        try {
            result.setData(systemInfoService.getCpuOnUse());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取cpu利用率错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取硬盘利用率")
    @RequestMapping(value = "/getDeskOnUse", method = RequestMethod.GET)
    public Result getDeskOnUse() {
        Result result = new Result();
        try {
            result.setData(systemInfoService.getDeskOnUse());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取硬盘利用率错误:", e);
        }
        return result;
    }



}
