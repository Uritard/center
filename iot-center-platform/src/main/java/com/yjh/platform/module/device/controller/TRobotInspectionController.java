package com.yjh.platform.module.device.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
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

import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
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
    @Logs(title = "插入机器人巡检点信息",content = "根据用户传递的参数新增机器人巡检点信息",logType = 2)
    public Result add(@Validated @RequestBody TRobotInspection tRobotInspection) {
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
    @Logs(title = "删除机器人巡检点信息",content = "根据用户传递的参数删除机器人巡检点信息",logType = 4)
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
    @Logs(title = "更新机器人巡检点信息",content = "根据用户传递的参数修改机器人巡检点信息",logType = 3)
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
    @Logs(title = "查询机器人巡检点信息",content = "根据用户传递的参数查询机器人巡检点信息",logType = 1)
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
    @Logs(title = "查询机器人巡检点信息",content = "根据用户传递的参数查询机器人巡检点信息",logType = 1)
    public Result select(@RequestParam(value = "inspectionId", required = false) Long inspectionId,
                         @RequestParam(value = "inspectionCode", required = false)String inspectionCode,
                         @RequestParam(value = "robotId", required = false) Long robotId,
                         @RequestParam(value = "inspectionName", required = false) String inspectionName,
                         @RequestParam(value = "componentId", required = false) String componentId,
                         @RequestParam(value = "meterType", required = false) Integer meterType,
                         @RequestParam(value = "appearanceType", required = false) Integer appearanceType,
                         @RequestParam(value = "saveTypeList", required = false) String saveTypeList,
                         @RequestParam(value = "recognitionTypeList", required = false) String recognitionTypeList,
                         @RequestParam(value = "phase", required = false) String phase,
                         @RequestParam(value = "deviceInfo", required = false) String deviceInfo) {
        Result result = new Result();
        try {
            List<TRobotInspection> list = tRobotInspectionService.select(inspectionId, inspectionCode, robotId, inspectionName,
                    componentId,meterType,appearanceType,saveTypeList,
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
    @Logs(title = "查询机器人巡检点信息",content = "根据用户传递的参数分页查询机器人巡检点信息",logType = 1)
    public Result selectByPage(@RequestBody TRobotInspection tRobotInspection
        ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tRobotInspection.getPageNum()!=null?tRobotInspection.getPageNum():1, tRobotInspection.getPageSize()!=null?tRobotInspection.getPageSize():0,true,null,true);
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
    @Logs(title = "批量插入机器人巡检点信息",content = "根据用户传递的参数批量插入机器人巡检点信息",logType = 2)
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
    @Logs(title = "机器人任务数据",content = "根据用户传递的参数查询机器人任务数据",logType = 1)
    public Result selectRobotTaskMessage(@RequestParam(value = "robotId") Long robotId){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String,Object> map = tRobotInspectionService.selectRobotTaskProgress(robotId);
            String taskId = map.get("taskId").toString();
            List<RobotTaskMessage> list = (List) map.get("list");
            if(list != null && list.size()>0){
                resultMap.put("taskInfo",list);
            }else {
                resultMap.put("taskInfo","");
            }

            if(list != null){
//                if("100".equals(map.get("taskProgress")) ){
//                    Map<String,String> jasonMap=new HashMap<>();
//                    jasonMap.put("type","noTask");
//                    //jasonMap.put("taskId",tCruiseTask.getTaskId());
//                    String json= JSON.toJSONString(jasonMap);
//                    log.info("发送给前端的消息-停止调接口：   "+json);
//                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
//                }
                resultMap.put("taskProgress",map.get("taskProgress"));
            }else {
                resultMap.put("taskProgress",map.get("taskProgress"));
            }
            resultMap.put("taskName",map.get("taskName"));
            resultMap.put("startTime",map.get("startTime"));
            resultMap.put("taskState",map.get("taskState"));
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("机器人巡检点分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询机器人信息")
    @RequestMapping(value = "/selectRobotInfo", method = RequestMethod.GET)
    @Logs(title = "查询机器人信息",content = "查询机器人信息",logType = 1)
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
    @Logs(title = "查询机器人状态信息",content = "根据用户传递的参数查询机器人状态信息",logType = 1)
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

    @ApiOperation(value = "查询机器人树")
    @RequestMapping(value = "/robotTree", method = RequestMethod.GET)
    @Logs(title = "查询机器人树",content = "根据用户传递的参数查询机器人树",logType = 1)
    public Result robotTree() {
        Result result = new Result();
        try {
            result.setData(tRobotInspectionService.robotTree());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询机器人状态信息失败描述：", e);
        }
        return result;
    }
}
