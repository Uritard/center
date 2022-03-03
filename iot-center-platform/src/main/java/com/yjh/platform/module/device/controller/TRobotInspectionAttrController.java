package com.yjh.platform.module.device.controller;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.TRobotInspectionAttr;
import com.yjh.platform.module.device.service.TRobotInspectionAttrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author hyh
 * @since 2022/2/17
 **/
@RestController
@RequestMapping("/tRobotInspectionAttr/v1")
@Api(value = "/tRobotInspectionAttr", description = "机器人巡检点属性表操作接口")
@Slf4j
public class TRobotInspectionAttrController {

    @Resource
    private final TRobotInspectionAttrService tRobotInspectionAttrService;

    public TRobotInspectionAttrController(TRobotInspectionAttrService tRobotInspectionAttrService) {
        this.tRobotInspectionAttrService = tRobotInspectionAttrService;
    }


    @ApiOperation(value = "查询线路保护装置按钮接口")
    @RequestMapping(value = "/selectByInspectionCode", method = RequestMethod.GET)
    public Result selectByInspectionCode(@RequestParam(value = "inspectionCode") String inspectionCode) {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionAttrService.selectByInspectionCode(inspectionCode));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TRobotInspectionAttr> list) {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionAttrService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }
}
