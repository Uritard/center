package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.service.TCruisePointInstanceService;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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
    public Result insert(@RequestBody TCruisePointInstance tCruisePointInstance) {
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

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "instanceId", required = true) Long instanceId) {
        Result result = new Result();
        try {
            result.setData(tCruisePointInstanceService.deleteByPrimaryId(instanceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("巡检点实例删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡检点实例删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
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
                         @RequestParam(value = "unitVal", required = false) String unitVal,
                         @RequestParam(value = "unitName", required = false) String unitName,
                         @RequestParam(value = "ifSy", required = false) Integer ifSy,
                         @RequestParam(value = "syType", required = false) Integer syType,
                         @RequestParam(value = "ifVideotape", required = false) Integer ifVideotape,
                         @RequestParam(value = "videotapeTime", required = false) String videotapeTime,
                         @RequestParam(value = "textDesc", required = false) String textDesc,
                         @RequestParam(value = "sort", required = false) String sort) {
        Result result = new Result();
        try {
            List<TCruisePointInstance> list = tCruisePointInstanceService.select(instanceId, deviceMeteId, stationId, stationName, deviceId, customId, dataFormat, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, cruiseContent, fluctuatingValue, unitVal, unitName, ifSy, syType, ifVideotape, videotapeTime, textDesc, sort);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruisePointInstance tCruisePointInstance,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
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

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TCruisePointInstance> list) {
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

    @ApiOperation(value = "标准设备测点关联机器人巡检实例")
    @RequestMapping(value = "/STDMateUnionTRInspection", method = RequestMethod.POST)
    public Result STDMateUnionTRInspection(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.STDMateUnionTRInspection(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点关联机器人巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥测量测点关联机器人巡检实例")
    @RequestMapping(value = "/telemeterUnionTRInspection", method = RequestMethod.POST)
    public Result telemeterUnionTRInspection(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.telemeterUnionTRInspection(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥测量测点关联机器人巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥控量测点关联机器人巡检实例")
    @RequestMapping(value = "/telecontrolUnionTRInspection", method = RequestMethod.POST)
    public Result telecontrolUnionTRInspection(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.telecontrolUnionTRInspection(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥控量测点关联机器人巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥调量测点关联机器人巡检实例")
    @RequestMapping(value = "/teleadjustUnionTRInspection", method = RequestMethod.POST)
    public Result teleadjustUnionTRInspection(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.teleadjustUnionTRInspection(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥调量测点关联机器人巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥信量测点关联机器人巡检实例")
    @RequestMapping(value = "/telesignalUnionTRInspection", method = RequestMethod.POST)
    public Result telesignalUnionTRInspection(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.telesignalUnionTRInspection(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥信量测点关联机器人巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "标准设备测点关联摄像头预置位巡检实例")
    @RequestMapping(value = "/STDMateUnionTCPreset", method = RequestMethod.POST)
    public Result STDMateUnionTCPreset(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.STDMateUnionTCPreset(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点关联摄像头预置位巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥测量测点关联摄像头预置位巡检实例")
    @RequestMapping(value = "/telemeterUnionTCPreset", method = RequestMethod.POST)
    public Result telemeterUnionTCPreset(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.telemeterUnionTCPreset(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥测量测点关联摄像头预置位巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥控量测点关联摄像头预置位巡检实例")
    @RequestMapping(value = "/telecontrolUnionTCPreset", method = RequestMethod.POST)
    public Result telecontrolUnionTCPreset(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.telecontrolUnionTCPreset(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥控量测点关联摄像头预置位巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥调量测点关联摄像头预置位巡检实例")
    @RequestMapping(value = "/teleadjustUnionTCPreset", method = RequestMethod.POST)
    public Result teleadjustUnionTCPreset(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.teleadjustUnionTCPreset(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥调量测点关联摄像头预置位巡检实例失败描述：", e);
        }
        return result;

    }

    @ApiOperation(value = "遥信量测点关联摄像头预置位巡检实例")
    @RequestMapping(value = "/telesignalUnionTCPreset", method = RequestMethod.POST)
    public Result telesignalUnionTCPreset(@RequestBody List<Map<String,String> > list){
        Result result = new Result();
        try{
            result.setData(tCruisePointInstanceService.telesignalUnionTCPreset(list));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥信量测点关联摄像头预置位巡检实例失败描述：", e);
        }
        return result;

    }


}
