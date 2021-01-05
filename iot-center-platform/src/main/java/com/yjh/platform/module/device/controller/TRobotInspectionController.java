package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.service.TRobotInspectionService;
import com.yjh.platform.module.device.entity.TRobotInspection;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author tt
 * @since 2020-08-08
 */
@RestController
@RequestMapping("/tRobotInspection/v1")
@Api(value = "/tRobotInspection", description = "机器人巡检点信息表操作接口")
public class TRobotInspectionController {

    @Autowired
    private final TRobotInspectionService tRobotInspectionService;

    private Logger log = LoggerFactory.getLogger(TRobotInspectionController.class);

    public TRobotInspectionController(TRobotInspectionService tRobotInspectionService) {
        this.tRobotInspectionService = tRobotInspectionService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TRobotInspection tRobotInspection) {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionService.insert(tRobotInspection));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人巡检点添加错误:", e);
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
            log.error("机器人巡检点删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人巡检点删除错误:", e);
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
            log.error("机器人巡检点更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("机器人巡检点更新错误:", e);
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
            log.error("机器人巡检点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "inspectionId", required = false) Long inspectionId,
                         @RequestParam(value = "inspectionCode", required = false)String inspectionCode,
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
            List<TRobotInspection> list = tRobotInspectionService.select(inspectionId, inspectionCode, robotId, inspectionName, inspectionType, alarmTop, alarmBottom, defaultValue, inspectionPosition, collectStatus, calibrationStatus, unit);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("机器人巡检点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TRobotInspection tRobotInspection,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TRobotInspection> list = tRobotInspectionService.selectByPage(tRobotInspection);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("机器人巡检点分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TRobotInspection> list) {
        Result result = new Result();
        try {
        result.setData(tRobotInspectionService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("机器人巡检点批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "机器人任务数据")
    @RequestMapping(value = "/selectRobotTaskMessage", method = RequestMethod.GET)
    public Result selectRobotTaskMessage(@RequestParam(value = "robotId") Long robotId){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<RobotTaskMessage> list =tRobotInspectionService.selectRobotTaskMessage(robotId);
            resultMap.put("taskInfo",list);
            if(list != null){
                Object taskProgress = tRobotInspectionService.selectRobotTaskProgress(robotId);
                resultMap.put("taskProgress",taskProgress);
            }else {
                resultMap.put("taskProgress",0);
            }
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("机器人巡检点分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询机器人信息")
    @RequestMapping(value = "/selectRobotInfo", method = RequestMethod.GET)
    public Result selectRobotInfo() {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionService.selectRobotInfo());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询机器人信息失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询机器人状态信息")
    @RequestMapping(value = "/selectRobotStatus", method = RequestMethod.GET)
    public Result selectRobotStatus(@RequestParam(value = "robotCode") String robotCode) {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionService.selectRobotStatus(robotCode));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询机器人状态信息失败描述：", e);
        }
        return result;
    }

}
