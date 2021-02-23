package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.service.TStdDeviceAttrService;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
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
 * @author lqh
 * @since 2020-08-24
 */
@RestController
@RequestMapping("/tStdDeviceAttr/v1")
@Api(value = "/tStdDeviceAttr", description = "标准化设备参数表操作接口")
public class TStdDeviceAttrController {

    @Autowired
    private final TStdDeviceAttrService tStdDeviceAttrService;

    private Logger log = LoggerFactory.getLogger(TStdDeviceAttrController.class);

    public TStdDeviceAttrController(TStdDeviceAttrService tStdDeviceAttrService) {
        this.tStdDeviceAttrService = tStdDeviceAttrService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增标准化设备参数",content = "根据用户传递的参数新增标准化设备参数",logType = 2)
    public Result add(@Validated  @RequestBody TStdDeviceAttr tStdDeviceAttr) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceAttrService.insert(tStdDeviceAttr));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准化设备参数表添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除标准化设备参数",content = "根据用户传递的参数删除标准化设备参数",logType = 4)
    public Result delete(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceAttrService.deleteByPrimaryId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准化设备参数表删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改标准化设备参数",content = "根据用户传递的参数修改标准化设备参数",logType = 3)
    public Result update(@RequestBody TStdDeviceAttr tStdDeviceAttr) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceAttrService.update(tStdDeviceAttr));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("标准化设备参数表更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准化设备参数表更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询标准化设备参数",content = "根据用户传递的参数查询标准化设备参数",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            TStdDeviceAttr tStdDeviceAttr = tStdDeviceAttrService.selectByPrimaryId(deviceId);
            result.setData(tStdDeviceAttr);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准化设备参数表失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询标准化设备参数",content = "根据用户传递的参数查询标准化设备参数",logType = 1)
    public Result select(@RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "deviceModel", required = false) Integer deviceModel,
                            @RequestParam(value = "pmsType", required = false) String pmsType,
                            @RequestParam(value = "pmsId", required = false) String pmsId,
                            @RequestParam(value = "manufacturer", required = false) String deviceVendor,
                            @RequestParam(value = "productionDate", required = false) Date productionDate,
                            @RequestParam(value = "openingDate", required = false) Date usedTime,
                            @RequestParam(value = "disableDate", required = false) Date disableDate,
                            @RequestParam(value = "lastMaintenance", required = false) Date lastMaintenance,
                            @RequestParam(value = "maintenanceCount", required = false) String maintenanceCount,
                            @RequestParam(value = "organization", required = false) String organization,
                            @RequestParam(value = "department", required = false) String department,
                            @RequestParam(value = "responsiblePerson", required = false) String responsiblePerson,
                            @RequestParam(value = "latitude", required = false) String latitude,
                            @RequestParam(value = "longitude", required = false) String longitude,
                            @RequestParam(value = "ip", required = false) String ip,
                            @RequestParam(value = "port", required = false) Integer port,
                            @RequestParam(value = "para1", required = false) String voltageLevel,
                            @RequestParam(value = "para2", required = false) String sequencePoint,
                            @RequestParam(value = "para3", required = false) String realCode,
                            @RequestParam(value = "gpsX", required = false) String address) {
        Result result = new Result();
        try {
            List<TStdDeviceAttr> list = tStdDeviceAttrService.select(deviceId, deviceModel, pmsType, pmsId, deviceVendor, productionDate, usedTime, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, voltageLevel, sequencePoint, realCode,address);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准化设备参数表失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询标准化设备参数",content = "根据用户传递的参数查询标准化设备参数",logType = 1)
    public Result selectByPage(@RequestBody TStdDeviceAttr tStdDeviceAttr
                             ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tStdDeviceAttr.getPageNum()!=null?tStdDeviceAttr.getPageNum():1, tStdDeviceAttr.getPageSize()!=null?tStdDeviceAttr.getPageSize():0,true,null,true);
            List<TStdDeviceAttr> list = tStdDeviceAttrService.selectByPage(tStdDeviceAttr);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准化设备参数表失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量新增标准化设备参数",content = "根据用户传递的参数批量新增标准化设备参数",logType = 2)
    public Result batchAdd(@Validated @RequestBody List<TStdDeviceAttr> list) {
        Result result = new Result();
        try {
        result.setData(tStdDeviceAttrService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("标准化设备参数表批量插入失败：" + e);
        }
        return result;
    }

}
