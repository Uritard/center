package com.yjh.platform.module.device.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.service.TRobotInspectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
                         @RequestParam(value = "componentId", required = false) String componentId,
                         @RequestParam(value = "componentName", required = false) String componentName,
                         @RequestParam(value = "bayId", required = false) String bayId,
                         @RequestParam(value = "bayName", required = false) String bayName,
                         @RequestParam(value = "mainDeviceId", required = false) String mainDeviceId,
                         @RequestParam(value = "mainDeviceName", required = false) String mainDeviceName,
                         @RequestParam(value = "deviceType", required = false) String deviceType,
                         @RequestParam(value = "meterType", required = false) String meterType,
                         @RequestParam(value = "appearanceType", required = false) String appearanceType,
                         @RequestParam(value = "saveTypeList", required = false) String saveTypeList,
                         @RequestParam(value = "recognitionTypeList", required = false) String recognitionTypeList,
                         @RequestParam(value = "phase", required = false) String phase,
                         @RequestParam(value = "deviceInfo", required = false) String deviceInfo) {
        Result result = new Result();
        try {
            List<TRobotInspection> list = tRobotInspectionService.select(inspectionId, inspectionCode, robotId, inspectionName,
                    componentId,componentName,bayId,bayName,mainDeviceId,mainDeviceName,deviceType,meterType,appearanceType,saveTypeList,
                    recognitionTypeList,phase,deviceInfo);
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
                Integer taskProgress = tRobotInspectionService.selectRobotTaskProgress(robotId);
                if(taskProgress == 100 ){
                    Map<String,Object> jasonMap=new HashMap<>();
                    jasonMap.put("type","noTask");
                    //jasonMap.put("taskId",tCruiseTask.getTaskId());
                    String json= JSON.toJSONString(jasonMap);
                    WebSocketServer.sendMsg(json);
                }
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
