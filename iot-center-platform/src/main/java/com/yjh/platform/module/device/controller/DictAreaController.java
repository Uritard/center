package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.SipBDictArea;
import com.yjh.platform.module.device.service.DictAreaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/9/7
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/dictArea/v1")
@Api(value = "/dictArea", tags = "中国省市区编码接口")
public class DictAreaController {

    @Resource
    private DictAreaService dictAreaService;

    @ApiOperation(value = "获取省市区编码")
    @GetMapping("/getDictDataByDictCode")
    @Logs(title = "获取省市区编码", content = "根据父节点获取省市区编码", logType = 1, authority = "1234")
    public Result getDictDataByDictCode(@RequestParam("parentId") String parentId) {
        return new Result(dictAreaService.getDictAreaByParentId(parentId));
    }



    @ApiOperation(value = "获取编码配置信息")
    @GetMapping("/getDictCode")
    @Logs(title = "获取编码配置信息", content = "获取编码配置信息", logType = 1, authority = "1234")
    public Result getDictCode(@RequestParam(value = "id",required = false) Long id,
                              @RequestParam(value = "code",required = false) String code) {
        Result result = new Result();
        try {
            result.setData(dictAreaService.selectSipBDictArea(id,code));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "获取编码配置信息")
    @GetMapping("/getDictCodeByType")
    @Logs(title = "获取编码配置信息", content = "获取编码配置信息", logType = 1, authority = "1234")
    public Result getDictCodeByType(@RequestParam(value = "type",required = false) Integer type) {
        Result result = new Result();
        try {
            result.setData(dictAreaService.selectSipBDictAreaByType(type));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "获取编码配置信息")
    @GetMapping("/getCodeConfig")
    @Logs(title = "获取编码配置信息", content = "获取编码配置信息", logType = 1, authority = "1234")
    public Result getCodeConfig() {
        Result result = new Result();
        try {
            result.setData(dictAreaService.getSipBConfig());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "保存编码配置信息")
    @PostMapping("/saveConfig")
    @Logs(title = "保存编码配置信息", content = "保存编码配置信息", logType = 1, authority = "1234")
    public Result saveConfig(@RequestBody SipBDictArea sipBDictArea) {
        Result result = new Result();
        try {
            result.setData(dictAreaService.saveConfig(sipBDictArea));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
        }
        return result;
    }
}
