package com.yjh.platform.module.device.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousPointInstance;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousWarnInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.service.TCruiseNonhomologousPointInstanceService;
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
 * @author sunjinyan
 * @since 2022-04-08
 */
@RestController
@RequestMapping("/tCruiseNonhomologousPointInstance/v1")
@Api(value = "/tCruiseNonhomologousPointInstance", description = "巡检点实例表操作接口")
public class TCruiseNonhomologousPointInstanceController {

    @Autowired
    private final TCruiseNonhomologousPointInstanceService tCruiseNonhomologousPointInstanceService;

    private Logger log = LoggerFactory.getLogger(TCruiseNonhomologousPointInstanceController.class);

    public TCruiseNonhomologousPointInstanceController(TCruiseNonhomologousPointInstanceService tCruiseNonhomologousPointInstanceService) {
        this.tCruiseNonhomologousPointInstanceService = tCruiseNonhomologousPointInstanceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增非同源巡检点关系",content = "根据用户传递的参数新增非同源巡检点关系",logType = 2, authority = "1234")
    public Result add(@Validated @RequestBody TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance) {
        Result result = new Result();
        try {
            result.setData(tCruiseNonhomologousPointInstanceService.insert(tCruiseNonhomologousPointInstance));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("非同源巡检点实例添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "根据主键删除")
    @RequestMapping(value = "/deleteByPrimaryId", method = RequestMethod.DELETE)
    @Logs(title = "删除非同源巡检点数据",content = "根据用户传递的参数删除非同源巡检点数据",logType = 4, authority = "1234")
    public Result deleteByPrimaryId(@RequestParam(value = "instanceId", required = true) Long instanceId) {
        Result result = new Result();
        try {
            result.setData(tCruiseNonhomologousPointInstanceService.deleteByPrimaryId(instanceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("根据主键删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("根据主键删除错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改非同源巡检点数据",content = "根据用户传递的参数修改非同源巡检点数据",logType = 3, authority = "1234")
    public Result update(@Validated @RequestBody TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance) {
        Result result = new Result();
        try {
            result.setData(tCruiseNonhomologousPointInstanceService.update(tCruiseNonhomologousPointInstance));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("非同源巡检点实例更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("非同源巡检点实例更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.POST)
    @Logs(title = "查询非同源巡检点数据",content = "根据用户传递的参数查询非同源巡检点数据",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "instanceId", required = true) Long instanceId) {
        Result result = new Result();
        try {
            TCruisePointInstance tCruisePointInstance = tCruiseNonhomologousPointInstanceService.selectByPrimaryId(instanceId);
            result.setData(tCruisePointInstance);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("巡检点实例查询失败描述：", e);
        }
        return result;
    }

//    @ApiOperation(value = "查询")
//    @RequestMapping(value = "/select", method = RequestMethod.GET)
//    @Logs(title = "查询非同源巡检点数据",content = "根据用户传递的参数查询非同源巡检点数据",logType = 1)
//    public Result select(@RequestParam(value = "instanceId", required = false) Long instanceId,
//                         @RequestParam(value = "stationId", required = false) String stationId,
//                         @RequestParam(value = "stationName", required = false) String stationName,
//                         @RequestParam(value = "robotDeviceId", required = false) String robotDeviceId,
//                         @RequestParam(value = "videoPresetId", required = false) String videoPresetId,
//                         @RequestParam(value = "identifyType", required = false) Integer identifyType,
//                         @RequestParam(value = "identifySonType", required = false) Integer identifySonType,
//                         @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
//                         @RequestParam(value = "cruiseId", required = false) Long cruiseId,
//                         @RequestParam(value = "cruiseName", required = false) String cruiseName,
//                         @RequestParam(value = "warnThreshold", required = false) String warnThreshold,
//                         @RequestParam(value = "ifSy", required = false) Integer ifSy,
//                         @RequestParam(value = "syType", required = false) Integer syType,
//                         @RequestParam(value = "warnLevel", required = false) String warnLevel) {
//        Result result = new Result();
//        try {
//            List<TCruisePointInstance> list = tCruiseNonhomologousPointInstanceService.select(instanceId, stationId, stationName, robotDeviceId, videoPresetId, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, warnThreshold, ifSy, syType, warnLevel);
//            result.setData(list);
//        } catch (Exception e) {
//            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
//            log.error("非同源巡检点实例查询失败描述：", e);
//        }
//        return result;
//    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询非同源巡检点数据",content = "根据用户传递的参数查询非同源巡检点数据",logType = 1, authority = "1234")
    public Result selectByPage(@RequestBody TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruiseNonhomologousPointInstance.getPageNum()!=null?tCruiseNonhomologousPointInstance.getPageNum():1, tCruiseNonhomologousPointInstance.getPageSize()!=null?tCruiseNonhomologousPointInstance.getPageSize():0,true,null,true);
            List<TCruiseNonhomologousPointInstance> list = tCruiseNonhomologousPointInstanceService.selectByPage(tCruiseNonhomologousPointInstance);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("非同源巡检点实例分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "告警分页查询")
    @RequestMapping(value = "/selectWarnByPage", method = RequestMethod.POST)
    @Logs(title = "查询非同源告警数据",content = "根据用户传递的参数查询非同源告警数据",logType = 1, authority = "1235")
    public Result selectWarnByPage(@RequestBody TCruiseNonhomologousWarnInfo tCruiseNonhomologousWarnInfo
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruiseNonhomologousWarnInfo.getPageNum()!=null?tCruiseNonhomologousWarnInfo.getPageNum():1, tCruiseNonhomologousWarnInfo.getPageSize()!=null?tCruiseNonhomologousWarnInfo.getPageSize():0,true,null,true);
            List<TCruiseNonhomologousWarnInfo> list = tCruiseNonhomologousPointInstanceService.selectWarnByPage(tCruiseNonhomologousWarnInfo);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("非同源告警分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectWarnByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询非同源告警详细数据",content = "根据用户传递的参数查询非同源告警详细数据",logType = 1, authority = "1235")
    public Result selectWarnByPrimaryId(@RequestParam(value = "warnId", required = true) String warnId) {
        Result result = new Result();
        try {
            Map<String,Object> tCruiseNonhomologousWarnInfo = tCruiseNonhomologousPointInstanceService.selectWarnByPrimaryId(warnId);
            result.setData(tCruiseNonhomologousWarnInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("巡检点实例查询失败描述：", e);
        }
        return result;
    }

}
