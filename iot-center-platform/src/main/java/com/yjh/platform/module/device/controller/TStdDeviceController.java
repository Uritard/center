package com.yjh.platform.module.device.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceDetail;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.service.TStdDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//import com.yjh.platform.module.device.dao.TStdRegionDao;


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
    @Logs(title = "新增标准化设备",content = "根据用户传递的参数新增标准化设备",logType = 2)
    public Result add(@Validated @RequestBody TStdDevice tStdDevice) {
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
    @Logs(title = "新增设备和属性",content = "根据用户传递的参数新增设备和属性",logType = 2,authority = "1234")
    public Result addALL(@RequestBody TStdDeviceDetail tStdDeviceDetail) {
        Result result = new Result();
        try {
            log.info("getDeviceName: "+tStdDeviceDetail.getDeviceName());
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
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除标准化设备",content = "根据用户传递的参数删除标准化设备",logType = 4)
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
    @RequestMapping(value = "/deleteAll", method = RequestMethod.POST)
    @Logs(title = "删除标准化设备",content = "根据用户传递的参数删除设备及属性",logType = 4,authority = "1234")
    public Result deleteAll(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            int re  = tStdDeviceService.deleteByPrimaryIdALL(deviceId);
            if(re == -1){
                result.setCode(209,"设备下有测点已关联巡视设备");
            }else {
                result.setData(re);
            }
            //result.setData(tStdDeviceService.deleteByPrimaryIdALL(deviceId));
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改标准化设备",content = "根据用户传递的参数修改标准化设备",logType = 3)
    public Result update(@Validated @RequestBody TStdDevice tStdDevice) {
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
    @RequestMapping(value = "/updateAll", method = RequestMethod.POST)
    @Logs(title = "修改标准化设备",content = "根据用户传递的参数修改标准化设备",logType = 3,authority = "1234")
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
    @Logs(title = "查询标准化设备",content = "根据用户传递的参数查询标准化设备",logType = 1)
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
    @Logs(title = "查询标准化设备",content = "根据用户传递的参数查询标准化设备",logType = 1,authority = "1234")
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
    @Logs(title = "查询标准化设备",content = "根据用户传递的参数查询标准化设备",logType = 1)
    public Result select(@RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "deviceName", required = false) String deviceName,
                            @RequestParam(value = "aliasName", required = false) String aliasName,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "positionType", required = false) String positionType,
                            @RequestParam(value = "modelId", required = false) Long modelId,
                            @RequestParam(value = "regionPath", required = false) String regionPath,
                            @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                            @RequestParam(value = "upRegionName", required = false) String upRegionName,
                            @RequestParam(value = "customType", required = false) Integer customType,
                            @RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                            @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TStdDevice> list = tStdDeviceService.select(deviceId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customType, status, updateTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "查询设备及属性")
    @RequestMapping(value = "/selectAll", method = RequestMethod.GET)
    @Logs(title = "查询标准化设备",content = "根据用户传递的参数查询标准化设备",logType = 1,authority = "1234")
    public Result selectAll(@RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "deviceName", required = false) String deviceName,
                            @RequestParam(value = "aliasName", required = false) String aliasName,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "positionType", required = false) String positionType,
                            @RequestParam(value = "modelId", required = false) Long modelId,
                            @RequestParam(value = "regionPath", required = false) String regionPath,
                            @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                            @RequestParam(value = "upRegionName", required = false) String upRegionName,
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
            List<TStdDeviceDetail> list = tStdDeviceService.selectAll(deviceId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName,  customType, status, updateTime, createTime,deviceModel, pmsType, pmsId, deviceVendor, productionDate, usedTime, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, voltageLevel, sequencePoint, realCode,address);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询标准化设备",content = "根据用户传递的参数查询标准化设备",logType = 1)
    public Result selectByPage(@RequestBody TStdDevice tStdDevice
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tStdDevice.getPageNum()!=null?tStdDevice.getPageNum():1, tStdDevice.getPageSize()!=null?tStdDevice.getPageSize():0,true,null,true);
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
    @Logs(title = "查询标准化设备",content = "根据用户传递的参数查询标准化设备",logType = 1,authority = "1234")
    public Result selectByPageAll(@RequestBody TStdDeviceDetail tStdDeviceDetail
                              ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
//            List<Long> upRegionIds = tStdRegionDao.selectRegionIds(tStdDeviceDetail.getUpRegionId());
            List<Long> upRegionIds = tStdDeviceService.selectRegionIdTree(tStdDeviceDetail.getUpRegionId());
            log.info("upRegionIds:"+upRegionIds);
            if(upRegionIds.size() != 0){
                tStdDeviceDetail.setUpRegionIds(upRegionIds);
            }else {
                upRegionIds.add(tStdDeviceDetail.getUpRegionId());
                tStdDeviceDetail.setUpRegionIds(upRegionIds);
            }
            Page page = PageHelper.startPage(tStdDeviceDetail.getPageNum()!=null?tStdDeviceDetail.getPageNum():1, tStdDeviceDetail.getPageSize()!=null?tStdDeviceDetail.getPageSize():0,true,null,true);
            List<Long>listForPage = tStdDeviceService.selectForPage( tStdDeviceDetail);
            List<TStdDeviceDetail> list=new ArrayList<>();
            if(listForPage !=null && listForPage.size()>0){
                list = tStdDeviceService.selectByPageAll(tStdDeviceDetail,listForPage);
            }
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
    @Logs(title = "根据设备ID查询上级区域信息",content = "根据用户传递的参数查询设备上级区域信息",logType = 1)
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

    @ApiOperation(value = "设备树查询(level：5-间隔，6-设备，7-部位，8-点位，9巡视点；deviceShow：all-所有，dev-设备，camera-摄像头，robot-机器人)")
    @RequestMapping(value = "/selectDevTree", method = RequestMethod.GET)
    @Logs(title = "设备树查询",content = "设备树查询",logType = 1)
    public Result selectDevTree(@RequestParam(value = "level", required = true) String level,
                                    @RequestParam(value = "deviceShow", required = true) String deviceShow,
                                @RequestParam(value = "deviceType", required = false) String deviceType,
                                @RequestParam(value = "analyseType", required = false) String analyseType) {
        Result result = new Result();
        try {
            List<AreaInfo> devTreeList = tStdDeviceService.selectDevTree(level, deviceShow,deviceType,analyseType);
            result.setData(devTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "操作设备树查询")
    @RequestMapping(value = "/selectOperationDevTree", method = RequestMethod.GET)
    @Logs(title = "操作设备树查询",content = "操作设备树查询",logType = 1)
    public Result selectOperationDevTree(@RequestParam(value = "regionId", required = true) Long regionId) {
        Result result = new Result();
        try {
            List<AreaInfo> devTaskTreeList = tStdDeviceService.selectOperationDevTree(regionId);
            result.setData(devTaskTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "设备任务树查询")
    @RequestMapping(value = "/selectDevTaskTree", method = RequestMethod.GET)
    @Logs(title = "设备任务树查询",content = "设备任务树查询",logType = 1)
    public Result selectDevTaskTree(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            List<AreaInfo> devTaskTreeList = tStdDeviceService.selectDevTaskTree(taskId);
            result.setData(devTaskTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "区域树模糊查询")
    @RequestMapping(value = "/selectRegionTreeByName", method = RequestMethod.GET)
    @Logs(title = "区域树模糊查询",content = "区域树模糊查询",logType = 1)
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
    @Logs(title = "根据ModelId查询",content = "根据用户传递的参数查询",logType = 1)
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
    @RequestMapping(value = "/updateModelIdByDevCus",method = RequestMethod.POST)
    @Logs(title = "修改设备的模板",content = "根据用户传递的参数修改设备的模板",logType = 3)
    public Result updateModelIdByDevCus(@RequestParam(value = "deviceId")Long deviceId,
                                        @RequestParam(value = "modelId")Long modelId){
        Result result=new Result();
        try{
            result.setData(tStdDeviceService.updateModelIdByDevCus(deviceId, modelId));
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
    @RequestMapping(value = "/batchDelete",method = RequestMethod.POST)
    @Logs(title = "批量删除设备",content = "根据用户传递的参数批量删除",logType = 4)
    public Result batchDelete(@RequestParam(value="deviceIds")String deviceIds){
        Result result=new Result();
        try{
            int re  = tStdDeviceService.batchDelete(deviceIds);
            if(re == -1){
                result.setCode(209,"设备下有测点已关联巡视设备");
            }else {
                result.setData(re);
            }
        }catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除设备异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除设备错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "导出模型文件")
    @RequestMapping(value = "/downloadModel",method = RequestMethod.GET)
    @Logs(title = "导出模型文件",content = "导出模型文件",logType = 9)
    public Result downloadModel(@RequestParam(value="type")String type){
        Result result=new Result();
        try{
            Map<String,Object> map = new HashMap<>();
            map.put("type",type);
            result = Constant.mapToOtherServer(map,Constant.TCP_MODEL_DOWNLOAD_URL);
//            result.setData("http://192.168.33.19:10086/files/tcpFiles//01/Model/host_model.zip");
        }catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("导出模型文件:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导出模型文件:", e);
        }
        return result;
    }

    @ApiOperation(value = "上报模型文件")
    @RequestMapping(value = "/uploadModel", method = RequestMethod.GET)
    @Logs(title = "上报模型文件", content = "上报模型文件", logType = 5)
    public Result uploadModel(@RequestParam(value = "type") String type) {
        Result result = new Result();
        try {
            String code;
            switch (type) {
                case "1":
                    code = "2";
                    break;
                case "2":
                    code = "3";
                    break;
                case "3":
                    code = "4";
                    break;
                case "4":
                    code = "1";
                    break;
                default:
                    code = type;
                    break;
            }
            if (StringUtils.isNotEmpty(code)) {
                Constant.modelUpload(code);
                result.setData("type:" + type + "upload success");
            } else {
                result.setData("type is not empty !");
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("上报模型文件:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("上报模型文件:", e);
        }
        return result;
    }
}
