package com.yjh.platform.module.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.result.BusinessException;
//import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TStdDeviceDetail;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.entity.TStdDevice;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;


/**
 * @author tt
 * @since 2020-07-27
 */
@RestController
@RequestMapping("/tStdDevice/v1")
@Api(value = "/tStdDevice", description = "标准化设备表操作接口")
public class TStdDeviceController {

    @Autowired
    private final TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    private Logger log = LoggerFactory.getLogger(TStdDeviceController.class);

    public TStdDeviceController(TStdDeviceService tStdDeviceService) {
        this.tStdDeviceService = tStdDeviceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TStdDevice tStdDevice) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceService.add(tStdDevice));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加设备错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "插入设备及属性")
    @RequestMapping(value = "/addAll", method = RequestMethod.POST)
    public Result addALL(@RequestBody TStdDeviceDetail tStdDeviceDetail) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceService.addALL(tStdDeviceDetail));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加设备错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceService.deleteByPrimaryId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除设备异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除设备错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除设备及属性")
    @RequestMapping(value = "/deleteAll", method = RequestMethod.DELETE)
    public Result deleteAll(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceService.deleteByPrimaryIdALL(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除设备及属性异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除设备及属性错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TStdDevice tStdDevice) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceService.update(tStdDevice));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新设备异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新设备错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新设备及属性")
    @RequestMapping(value = "/updateAll", method = RequestMethod.PUT)
    public Result updateAll(@RequestBody TStdDeviceDetail tStdDeviceDetail) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceService.updateAll(tStdDeviceDetail));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新设备及属性异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新设备及属性错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            TStdDevice tStdDevice = tStdDeviceService.selectByPrimaryId(deviceId);
            result.setData(tStdDevice);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询设备及属性")
    @RequestMapping(value = "/selectByPrimaryIdAll", method = RequestMethod.GET)
    public Result selectByPrimaryIdAll(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            TStdDeviceDetail tStdDeviceDetail = tStdDeviceService.selectByPrimaryIdAll(deviceId);
            result.setData(tStdDeviceDetail);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询设备及属性失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "customId", required = false) String customId,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "deviceName", required = false) String deviceName,
                            @RequestParam(value = "aliasName", required = false) String aliasName,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "positionType", required = false) String positionType,
                            @RequestParam(value = "modelId", required = false) Long modelId,
                            @RequestParam(value = "regionPath", required = false) String regionPath,
                            @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                            @RequestParam(value = "upRegionName", required = false) String upRegionName,
                            @RequestParam(value = "customName", required = false) String customName,
                            @RequestParam(value = "customType", required = false) Integer customType,
                            @RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                            @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TStdDevice> list = tStdDeviceService.select(deviceId, customId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customName, customType, status, updateTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "查询设备及属性")
    @RequestMapping(value = "/selectAll", method = RequestMethod.GET)
    public Result selectAll(@RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "customId", required = false) String customId,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "deviceName", required = false) String deviceName,
                            @RequestParam(value = "aliasName", required = false) String aliasName,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "positionType", required = false) String positionType,
                            @RequestParam(value = "modelId", required = false) Long modelId,
                            @RequestParam(value = "regionPath", required = false) String regionPath,
                            @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                            @RequestParam(value = "upRegionName", required = false) String upRegionName,
                            @RequestParam(value = "customName", required = false) String customName,
                            @RequestParam(value = "customType", required = false) Integer customType,
                            @RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "deviceModel", required = false) Integer deviceModel,
                            @RequestParam(value = "pmsType", required = false) String pmsType,
                            @RequestParam(value = "pmsId", required = false) String pmsId,
                            @RequestParam(value = "deviceVendor", required = false) String deviceVendor,
                            @RequestParam(value = "productionDate", required = false) Date productionDate,
                            @RequestParam(value = "usedTime", required = false) Date usedTime,
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
                            @RequestParam(value = "voltageLevel", required = false) String voltageLevel,
                            @RequestParam(value = "sequencePoint", required = false) String sequencePoint,
                            @RequestParam(value = "realCode", required = false) String realCode,
                            @RequestParam(value = "address", required = false) String address) {
        Result result = new Result();
        try {
            List<TStdDeviceDetail> list = tStdDeviceService.selectAll(deviceId, customId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customName, customType, status, updateTime, createTime,deviceModel, pmsType, pmsId, deviceVendor, productionDate, usedTime, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, voltageLevel, sequencePoint, realCode,address);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TStdDevice tStdDevice,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TStdDevice> list = tStdDeviceService.selectByPage(tStdDevice);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询设备及属性")
    @RequestMapping(value = "/selectByPageAll", method = RequestMethod.POST)
    public Result selectByPageAll(@RequestBody TStdDeviceDetail tStdDeviceDetail,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Long> upRegionIds = tStdRegionDao.selectRegionIds(tStdDeviceDetail.getUpRegionId());
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TStdDeviceDetail> list = tStdDeviceService.selectByPageAll(tStdDeviceDetail, upRegionIds);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备ID查询上级区域信息")
    @RequestMapping(value = "/selectRegionById", method = RequestMethod.GET)
    public Result selectRegionById(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            TStdRegion tStdRegion = tStdDeviceService.selectRegionById(deviceId);
            result.setData(tStdRegion);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "设备树查询(level：5-间隔，6-设备，7-部位，8-点位；deviceShow：all-所有，dev-设备，camera-摄像头，robot-机器人)")
    @RequestMapping(value = "/selectDevTree", method = RequestMethod.GET)
    public Result selectDevTree(@RequestParam(value = "level", required = true) String level,
                                    @RequestParam(value = "deviceShow", required = true) String deviceShow) {
        Result result = new Result();
        try {
            List<AreaInfo> devTreeList = tStdDeviceService.selectDevTree(level, deviceShow);
            result.setData(devTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "区域树模糊查询")
    @RequestMapping(value = "/selectRegionTreeByName", method = RequestMethod.GET)
    public Result selectRegionTreeByName(@RequestParam(value = "regionName", required = false) String regionName) {
        Result result = new Result();
        try {
            List<AreaInfo> devTreeList = tStdDeviceService.selectRegionTreeByName(regionName);
            result.setData(devTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据ModelId查询")
    @RequestMapping(value = "/selectByModelId", method = RequestMethod.GET)
    public Result selectByModelId(@RequestParam(value = "modelId", required = false) Long modelId) {
        Result result = new Result();
        try {
            List<String> list = tStdDeviceService.selectByModelId(modelId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备ID和部位ID修改设备的模板ID")
    @RequestMapping(value = "/updateModelIdByDevCus",method = RequestMethod.PUT)
    public Result updateModelIdByDevCus(@RequestParam(value = "deviceId")Long deviceId,
                                        @RequestParam(value = "customId")Long customId,
                                        @RequestParam(value = "modelId")Long modelId){
        Result result=new Result();
        try{
            result.setData(tStdDeviceService.updateModelIdByDevCus(deviceId, customId, modelId));
        }catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新模板ID异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新模板ID错误:", e);
        }
        return result;
    }



    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete",method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value="deviceIds")String deviceIds){
        Result result=new Result();
        try{
            result.setData(tStdDeviceService.batchDelete(deviceIds));
        }catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除设备异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除设备错误:", e);
        }
        return result;
    }


}
