package com.yjh.accessrobot.module.command.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.TDroneInspection;
import com.yjh.accessrobot.module.command.service.TDroneInspectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author YC
 * @since 2020-11-19
 */
@RestController
@RequestMapping("/t-drone-inspection/v1")
@Api(value = "/t-drone-inspection", tags = "无人机巡检点信息表操作接口")
public class TDroneInspectionController {

    @Autowired
    private final TDroneInspectionService tDroneInspectionService;

    private Logger log = LoggerFactory.getLogger(TDroneInspectionController.class);

    public TDroneInspectionController(TDroneInspectionService tDroneInspectionService) {
        this.tDroneInspectionService = tDroneInspectionService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@Validated  @RequestBody TDroneInspection tDroneInspection) {
        Result result = new Result();
        try {
            result.setData(tDroneInspectionService.insert(tDroneInspection));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "inspectionId", required = true) Long inspectionId) {
        Result result = new Result();
        try {
            result.setData(tDroneInspectionService.deleteByPrimaryId(inspectionId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@Validated @RequestBody TDroneInspection tDroneInspection) {
        Result result = new Result();
        try {
            result.setData(tDroneInspectionService.update(tDroneInspection));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "inspectionId", required = true) Long inspectionId) {
        Result result = new Result();
        try {
            TDroneInspection tDroneInspection = tDroneInspectionService.selectByPrimaryId(inspectionId);
            result.setData(tDroneInspection);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "inspectionId", required = false) Long inspectionId,
                         @RequestParam(value = "inspectionCode", required = false)String inspectionCode,
                         @RequestParam(value = "droneId", required = false) Long droneId,
                         @RequestParam(value = "inspectionName", required = false) String inspectionName,
                         @RequestParam(value = "inspectionType", required = false) Integer inspectionType,
                         @RequestParam(value = "componentId", required = false) String componentId,
                         @RequestParam(value = "meterType", required = false) Integer meterType,
                         @RequestParam(value = "appearanceType", required = false) Integer appearanceType,
                         @RequestParam(value = "mainOperationType", required = false) Integer mainOperationType,
                         @RequestParam(value = "operationType", required = false) Integer operationType,
                         @RequestParam(value = "saveTypeList", required = false) String saveTypeList,
                         @RequestParam(value = "recognitionTypeList", required = false) String recognitionTypeList,
                         @RequestParam(value = "phase", required = false) String phase,
                         @RequestParam(value = "deviceInfo", required = false) String deviceInfo) {
        Result result = new Result();
        try {
            List<TDroneInspection> list = tDroneInspectionService.select(inspectionId, inspectionCode, droneId, inspectionName,inspectionType,
                    componentId,meterType,appearanceType, mainOperationType, operationType,saveTypeList,
                    recognitionTypeList,phase,deviceInfo);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("无人机巡检点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TDroneInspection tDroneInspection
                              ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tDroneInspection.getPageNum()!=null?tDroneInspection.getPageNum():1, tDroneInspection.getPageSize()!=null?tDroneInspection.getPageSize():0, true,null,true);
            List<TDroneInspection> list = tDroneInspectionService.selectByPage(tDroneInspection);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TDroneInspection> list) {
        Result result = new Result();
        try {
            result.setData(tDroneInspectionService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

}
