package com.yjh.accessrobot.module.command.controller;

import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.TDroneInspectionAttr;
import com.yjh.accessrobot.module.command.service.TDroneInspectionAttrService;
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
@RequestMapping("/t-drone-inspection-attr/v1")
@Api(value = "/t-drone-inspection-attr", tags = "无人机巡检点属性表操作接口")
@Slf4j
public class TDroneInspectionAttrController {

    @Resource
    private final TDroneInspectionAttrService tDroneInspectionAttrService;

    public TDroneInspectionAttrController(TDroneInspectionAttrService tDroneInspectionAttrService) {
        this.tDroneInspectionAttrService = tDroneInspectionAttrService;
    }


    @ApiOperation(value = "查询线路保护装置按钮接口")
    @RequestMapping(value = "/selectByInspectionCode", method = RequestMethod.GET)
    public Result selectByInspectionCode(@RequestParam(value = "inspectionCode") String inspectionCode) {
        Result result = new Result();
        try {
            result.setData(tDroneInspectionAttrService.selectByInspectionCode(inspectionCode));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TDroneInspectionAttr> list) {
        Result result = new Result();
        try {
            result.setData(tDroneInspectionAttrService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }
}
