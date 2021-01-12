package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.entity.IdAndNameDetail;
import com.yjh.platform.module.device.entity.TDeviceMaintenanceDetail;
import com.yjh.platform.module.device.service.TDeviceMaintenanceService;
import com.yjh.platform.module.device.entity.TDeviceMaintenance;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
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
 * @since 2021-01-11
 */
@RestController
@RequestMapping("/tDeviceMaintenance/v1")
@Api(value = "/tDeviceMaintenance", description = "设备区域检修表操作接口")
public class TDeviceMaintenanceController {

    @Autowired
    private final TDeviceMaintenanceService tDeviceMaintenanceService;

    private Logger log = LoggerFactory.getLogger(TDeviceMaintenanceController.class);

    public TDeviceMaintenanceController(TDeviceMaintenanceService tDeviceMaintenanceService) {
        this.tDeviceMaintenanceService = tDeviceMaintenanceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TDeviceMaintenance tDeviceMaintenance) {
        Result result = new Result();
        try {
            result.setData(tDeviceMaintenanceService.add(tDeviceMaintenance));
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
    public Result delete(@RequestParam(value = "maintenanceId", required = true) Long maintenanceId) {
        Result result = new Result();
        try {
            result.setData(tDeviceMaintenanceService.deleteByPrimaryId(maintenanceId));
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
    public Result update(@RequestBody TDeviceMaintenance tDeviceMaintenance) {
        Result result = new Result();
        try {
            result.setData(tDeviceMaintenanceService.update(tDeviceMaintenance));
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
    public Result selectByPrimaryId(@RequestParam(value = "maintenanceId", required = true) Long maintenanceId) {
        Result result = new Result();
        try {
            List<TDeviceMaintenance> tDeviceMaintenanceList = tDeviceMaintenanceService.selectByPrimaryId(maintenanceId);
            result.setData(tDeviceMaintenanceList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "maintenanceId", required = false) Long maintenanceId,
                            @RequestParam(value = "maintenanceName", required = false) String maintenanceName,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "isValid", required = false) Integer isValid,
                            @RequestParam(value = "maintenanceStart", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date maintenanceStart,
                            @RequestParam(value = "maintenanceStop", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date maintenanceStop,
                         @RequestParam(value = "effectiveState", required = false) Integer effectiveState) {
        Result result = new Result();
        try {
            List<TDeviceMaintenance> list = tDeviceMaintenanceService.select(maintenanceId, maintenanceName, deviceId, isValid, maintenanceStart, maintenanceStop,effectiveState);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public Result selectByPage(@RequestParam(value = "maintenanceName", required = false) String maintenanceName,
                               @RequestParam(value = "effectiveState", required = false) Integer effectiveState,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TDeviceMaintenanceDetail> list = tDeviceMaintenanceService.selectByPage( maintenanceName,effectiveState);
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
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TDeviceMaintenance> list) {
        Result result = new Result();
        try {
        result.setData(tDeviceMaintenanceService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "maintenanceIds") String maintenanceIds) {
    Result result = new Result();
    try {
        result.setData(tDeviceMaintenanceService.batchDelete(maintenanceIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }

    @ApiOperation(value = "查询区域下的设备")
    @RequestMapping(value = "/selectDevice", method = RequestMethod.GET)
    public Result selectDevice(@RequestParam(value = "upRegionId", required = true) Long upRegionId) {
        Result result = new Result();
        try {
            List<IdAndNameDetail> list = tDeviceMaintenanceService.selectDevice(upRegionId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询设备下的巡视点")
    @RequestMapping(value = "/selectInstance", method = RequestMethod.GET)
    public Result selectInstance(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            List<IdAndNameDetail> list = tDeviceMaintenanceService.selectInstance(deviceId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


}
