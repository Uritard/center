package com.yjh.platform.module.simple.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.simple.entity.BasePhotoBuild;
import com.yjh.platform.module.simple.entity.BasePhotoInfo;
import com.yjh.platform.module.simple.service.SimplePointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/3
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RestController
@RequestMapping("/simplePoint/v1")
@Api(value = "/simplePoint", tags = "简易机器人接口")
@RequiredArgsConstructor
public class SimplePointController {

    private final SimplePointService simplePointService;

    @ApiOperation(value = "获取底图")
    @PostMapping(value = "/basePhoto")
    public Result basePhoto(@RequestParam(value = "deviceId", required = false) Long deviceId) {
        Result result = new Result();
        try {
            result.setData(simplePointService.basePhoto(deviceId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取底图接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "底图拆分")
    @PostMapping(value = "/splitPhoto")
    public Result splitPhoto(@RequestBody BasePhotoInfo basePhotoInfo) {
        Result result = new Result();
        try {
            result.setData(simplePointService.splitPhoto(basePhotoInfo));
        } catch (BusinessException b) { 
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("底图拆分接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "底图保存")
    @PostMapping(value = "/basePhotoBuild")
    public Result basePhotoBuild(@RequestBody List<BasePhotoBuild> buildList) {
        Result result = new Result();
        try {
            result.setData(simplePointService.basePhotoBuild(buildList));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("底图保存接口调用错误:", e);
        }
        return result;
    }
}
