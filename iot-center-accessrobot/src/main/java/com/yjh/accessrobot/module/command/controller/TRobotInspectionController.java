package com.yjh.accessrobot.module.command.controller;

import java.util.HashMap;

import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.TRobotInspection;
import com.yjh.accessrobot.module.command.service.TRobotInspectionService;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author YC
 * @since 2020-11-19
 */
@RestController
@RequestMapping("/t-robot-inspection/v1")
@Api(value = "/t-robot-inspection", description = "机器人巡检点信息表操作接口")
public class TRobotInspectionController {

    @Autowired
    private final TRobotInspectionService tRobotInspectionService;

    private Logger log = LoggerFactory.getLogger(TRobotInspectionController.class);

    public TRobotInspectionController(TRobotInspectionService tRobotInspectionService) {
        this.tRobotInspectionService = tRobotInspectionService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TRobotInspection tRobotInspection) {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionService.insert(tRobotInspection));
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
            result.setData(tRobotInspectionService.deleteByPrimaryId(inspectionId));
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
    public Result update(@RequestBody TRobotInspection tRobotInspection) {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionService.update(tRobotInspection));
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
            TRobotInspection tRobotInspection = tRobotInspectionService.selectByPrimaryId(inspectionId);
            result.setData(tRobotInspection);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "inspectionId", required = false) Long inspectionId,
                            @RequestParam(value = "robotId", required = false) Long robotId,
                            @RequestParam(value = "inspectionName", required = false) String inspectionName,
                            @RequestParam(value = "inspectionType", required = false) Integer inspectionType,
                            @RequestParam(value = "alarmTop", required = false) String alarmTop,
                            @RequestParam(value = "alarmBottom", required = false) String alarmBottom,
                            @RequestParam(value = "defaultValue", required = false) String defaultValue,
                            @RequestParam(value = "inspectionPosition", required = false) Integer inspectionPosition,
                            @RequestParam(value = "collectStatus", required = false) Integer collectStatus,
                            @RequestParam(value = "calibrationStatus", required = false) Integer calibrationStatus,
                            @RequestParam(value = "unit", required = false) String unit) {
        Result result = new Result();
        try {
            List<TRobotInspection> list = tRobotInspectionService.select(inspectionId, robotId, inspectionName, inspectionType, alarmTop, alarmBottom, defaultValue, inspectionPosition, collectStatus, calibrationStatus, unit);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TRobotInspection tRobotInspection,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TRobotInspection> list = tRobotInspectionService.selectByPage(tRobotInspection);
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
    public Result batchInsert(@RequestBody List<TRobotInspection> list) {
        Result result = new Result();
        try {
        result.setData(tRobotInspectionService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
