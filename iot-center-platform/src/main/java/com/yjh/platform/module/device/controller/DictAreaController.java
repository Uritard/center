package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.result.Result;
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
    public Result getDictDataByDictCode(@RequestParam("parentId") String parentId) {
        return new Result(dictAreaService.getDictAreaByParentId(parentId));
    }
}
