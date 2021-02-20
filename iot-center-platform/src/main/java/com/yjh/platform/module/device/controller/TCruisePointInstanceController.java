package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.service.TCruisePointInstanceService;

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

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author tt
 * @since 2020-08-08
 */
@RestController
@RequestMapping("/tCruisePointInstance/v1")
@Api(value = "/tCruisePointInstance", description = "巡检点实例表操作接口")
public class TCruisePointInstanceController {

    @Autowired
    private final TCruisePointInstanceService tCruisePointInstanceService;

    private Logger log = LoggerFactory.getLogger(TCruisePointInstanceController.class);

    public TCruisePointInstanceController(TCruisePointInstanceService tCruisePointInstanceService) {
        this.tCruisePointInstanceService = tCruisePointInstanceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增巡检点数据",content = "根据用户传递的参数新增巡检点数据",logType = 2)
    public Result add(@Validated  @RequestBody TCruisePointInstance tCruisePointInstance) {
        Result result = new Result();
        try {
            result.setData(tCruisePointInstanceService.insert(tCruisePointInstance));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡检点实例添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "根据主键删除删除")
    @RequestMapping(value = "/deleteByPrimaryId", method = RequestMethod.DELETE)
    @Logs(title = "删除巡检点数据",content = "根据用户传递的参数删除巡检点数据",logType = 4)
    public Result deleteByPrimaryId(@RequestParam(value = "instanceId", required = true) Long instanceId) {
        Result result = new Result();
        try {
            result.setData(tCruisePointInstanceService.deleteByPrimaryId(instanceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("根据主键删除删除删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("根据主键删除删除删除错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改巡检点数据",content = "根据用户传递的参数修改巡检点数据",logType = 3)
    public Result update(@RequestBody TCruisePointInstance tCruisePointInstance) {
        Result result = new Result();
        try {
            result.setData(tCruisePointInstanceService.update(tCruisePointInstance));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("巡检点实例更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数查询巡检点数据",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "instanceId", required = true) Long instanceId) {
        Result result = new Result();
        try {
            TCruisePointInstance tCruisePointInstance = tCruisePointInstanceService.selectByPrimaryId(instanceId);
            result.setData(tCruisePointInstance);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数查询巡检点数据",logType = 1)
    public Result select(@RequestParam(value = "instanceId", required = false) Long instanceId,
                         @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                         @RequestParam(value = "stationId", required = false) String stationId,
                         @RequestParam(value = "stationName", required = false) String stationName,
                         @RequestParam(value = "deviceId", required = false) Long deviceId,
                         @RequestParam(value = "customId", required = false) String customId,
                         @RequestParam(value = "dataFormat", required = false) String dataFormat,
                         @RequestParam(value = "identifyType", required = false) Integer identifyType,
                         @RequestParam(value = "identifySonType", required = false) Integer identifySonType,
                         @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                         @RequestParam(value = "cruiseId", required = false) Long cruiseId,
                         @RequestParam(value = "cruiseName", required = false) String cruiseName,
                         @RequestParam(value = "cruiseContent", required = false) String cruiseContent,
                         @RequestParam(value = "fluctuatingValue", required = false) String fluctuatingValue,
                         @RequestParam(value = "unit", required = false) String unit,
                         @RequestParam(value = "ifSy", required = false) Integer ifSy,
                         @RequestParam(value = "syType", required = false) Integer syType,
                         @RequestParam(value = "ifVideotape", required = false) Integer ifVideotape,
                         @RequestParam(value = "videotapeTime", required = false) String videotapeTime,
                         @RequestParam(value = "textDesc", required = false) String textDesc,
                         @RequestParam(value = "sort", required = false) String sort) {
        Result result = new Result();
        try {
            List<TCruisePointInstance> list = tCruisePointInstanceService.select(instanceId, deviceMeteId, stationId, stationName, deviceId, customId, dataFormat, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, cruiseContent, fluctuatingValue, unit, ifSy, syType, ifVideotape, videotapeTime, textDesc, sort);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数查询巡检点数据",logType = 1)
    public Result selectByPage(@RequestBody TCruisePointInstance tCruisePointInstance
//                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
//                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruisePointInstance.getPageNum()!=null?tCruisePointInstance.getPageNum():1, tCruisePointInstance.getPageSize()!=null?tCruisePointInstance.getPageSize():0,true,null,true);
            List<TCruisePointInstance> list = tCruisePointInstanceService.selectByPage(tCruisePointInstance);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡检点关联分页查询")
    @RequestMapping(value = "/selectCruisePointByPage", method = RequestMethod.POST)
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数查询巡检点数据",logType = 1)
    public Result selectCruisePointByPage(@RequestBody TStdDeviceMete tStdDeviceMete,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        return tCruisePointInstanceService.selectCruisePointByPage(tStdDeviceMete,pageNum,pageSize);
    }

    @ApiOperation(value = "告警联动分页查询")
    @RequestMapping(value = "/selectSYCruisePointByPage", method = RequestMethod.POST)
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数查询巡检点数据",logType = 1)
    public Result selectSYCruisePointByPage(@RequestBody TCfgMeteForPointDetail tCfgMeteForPointDetail,
                                          @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        return tCruisePointInstanceService.selectSYCruisePointByPage(tCfgMeteForPointDetail,pageNum,pageSize);
    }
    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量插入巡检点数据",content = "根据用户传递的参数批量插入巡检点数据",logType = 2)
    public Result batchAdd(@Validated @RequestBody List<TCruisePointInstance> list) {
        Result result = new Result();
        try {
            result.setData(tCruisePointInstanceService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡检点实例批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "标准测点关联机器人巡检点")
    @RequestMapping(value = "/StdMeteUnionInspectionId", method = RequestMethod.GET)
    @Logs(title = "标准测点关联机器人巡检点",content = "标准测点关联机器人巡检点",logType = 5)
    public Result StdMeteUnionInspectionId(@RequestParam(value = "deviceId", required = false) Long deviceId) {
        Result result = new Result();
        try {
            List<TCruisePointInstance> list = tCruisePointInstanceService.StdMeteUnionInspectionId(deviceId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准测点关联机器人巡检点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡检点关联配置")
    @RequestMapping(value = "/instanceUpdate", method = RequestMethod.PUT)
    @Logs(title = "巡检点关联配置",content = "巡检点关联配置",logType = 2)
    public Result instanceUnionUpdate(@RequestBody TCruisePointInstanceDetail tCruisePointInstanceDetail){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.instanceUpdate(tCruisePointInstanceDetail));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点关联配置失败描述：", e);
        }
        return result;

    }


    @ApiOperation(value = "告警配置")
    @RequestMapping(value = "/warnInspectUpdate", method = RequestMethod.PUT)
    @Logs(title = "告警配置",content = "告警配置",logType = 2)
    public Result warnInspectUpdate(@RequestBody TCruisePointInstanceDetail tCruisePointInstanceDetail){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.warnInspectUpdate(tCruisePointInstanceDetail));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("告警配置失败描述：", e);
        }
        return result;

    }



    @ApiOperation(value = "C-统计当前任务下的巡检点数量")
    @RequestMapping(value = "/selectCruiseCount",method = RequestMethod.GET)
    @Logs(title = "统计当前任务下的巡检点数量",content = "统计当前任务下的巡检点数量",logType = 1)
    public  Result selectCruiseCount(@RequestParam  String taskId){
        Result result=new Result();
        try {
            result.setData(tCruisePointInstanceService.selectCruiseCount(taskId));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
            log.error("数量查询失败描述",e);
        }
        return result;
    }

    @ApiOperation(value = "C-查询各种巡检类型下的巡检点数量")
    @RequestMapping(value = "/selectCruiseCountByType",method = RequestMethod.GET)
    @Logs(title = "查询巡检类型下的巡检点数量",content = "查询巡检类型下的巡检点数量",logType = 1)
    public Result selectCruiseCountByType(@RequestParam String taskId){
        Result result=new Result();
        try {
           result.setData(tCruisePointInstanceService.selectCruiseCountByType(taskId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例查询失败描述：", e);
        }
        return result;
    }


}
