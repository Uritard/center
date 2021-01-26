package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraInfoByDict;
import com.yjh.platform.module.user.service.TCameraInfoService;
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
 * @author tt
 * @since 2020-07-23
 */
@RestController
@RequestMapping("/tCameraInfo/v1")
@Api(value = "/tCameraInfo", description = "摄像头信息表操作接口")
public class TCameraInfoController {

    @Autowired
    private final TCameraInfoService tCameraInfoService;

    @Autowired
    private final TStdDeviceService tStdDeviceService;

    private Logger log = LoggerFactory.getLogger(TCameraInfoController.class);

    public TCameraInfoController(TCameraInfoService tCameraInfoService, TStdDeviceService tStdDeviceService) {
        this.tCameraInfoService = tCameraInfoService;
        this.tStdDeviceService = tStdDeviceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@Validated  @RequestBody  TCameraInfo tCameraInfo) {

        Result result = new Result();
        try {
            result.setData(tCameraInfoService.insert(tCameraInfo));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增相机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "cameraId", required = true) Long cameraId) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.deleteByPrimaryId(cameraId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除相机异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除相机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/deleteSelectedCamera", method = RequestMethod.DELETE)
    public Result deleteSelectedCamera(@RequestParam(value = "cameraIds", required = true) String cameraIds) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.deleteSelectedCamera(cameraIds));
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
    public Result update(@RequestBody TCameraInfo tCameraInfo) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.update(tCameraInfo));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新相机异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新相机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "cameraId", required = true) Long cameraId) {
        Result result = new Result();
        try {
            TCameraInfoByDict tCameraInfoByDict = tCameraInfoService.selectByPrimaryId(cameraId);
            result.setData(tCameraInfoByDict);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据间隔id查询所有摄像机信息")
    @RequestMapping(value = "/selectByRegionId", method = RequestMethod.GET)
    public Result selectByRegionId(@RequestParam(value = "regionId", required = false) Long regionId,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraInfoByDict> list = tCameraInfoService.selectByRegionId(regionId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据摄像机名称查询信息")
    @RequestMapping(value = "/selectByCameraName", method = RequestMethod.GET)
    public Result selectByCameraName(@RequestParam(value = "cameraName", required = false) String cameraName,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraInfoByDict> list = tCameraInfoService.selectByCameraName(cameraName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "cameraId", required = false) Long cameraId,
                         @RequestParam(value = "cameraName", required = false) String cameraName,
                         @RequestParam(value = "cameraModel", required = false) Integer cameraModel,
                         @RequestParam(value = "aliasName", required = false) String aliasName,
                         @RequestParam(value = "recordId", required = false) String recordId,
                         @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                         @RequestParam(value = "channelNum", required = false) Integer channelNum,
                         @RequestParam(value = "smsId", required = false) Integer smsId,
                         @RequestParam(value = "rmsId", required = false) Integer rmsId,
                         @RequestParam(value = "vendorId", required = false) Integer vendorId,
                         @RequestParam(value = "streamType", required = false) Integer streamType,
                         @RequestParam(value = "protocolType", required = false) Integer protocolType,
                         @RequestParam(value = "cameraIp", required = false) String cameraIp,
                         @RequestParam(value = "url", required = false) String url,
                         @RequestParam(value = "port", required = false) Integer port,
                         @RequestParam(value = "cameraType", required = false) Integer cameraType,
                         @RequestParam(value = "latitude", required = false) String latitude,
                         @RequestParam(value = "longitude", required = false) String longitude,
                         @RequestParam(value = "address", required = false) String address,
                         @RequestParam(value = "isControl", required = false) Integer isControl,
                         @RequestParam(value = "unit", required = false) String unit) {
        Result result = new Result();
        try {
            List<TCameraInfoByDict> list = tCameraInfoService.select(cameraId, cameraName, cameraModel,aliasName,
                    recordId, upRegionId, channelNum, smsId, rmsId, vendorId, streamType, protocolType,
                    cameraIp,url, port, cameraType, isControl,latitude,longitude,address,unit);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCameraInfo tCameraInfo,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Long> upRegionIds = tStdDeviceService.selectRegionIdTree(tCameraInfo.getUpRegionId());
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraInfoByDict> list = tCameraInfoService.selectByPage(tCameraInfo, upRegionIds);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }



    @ApiOperation(value = "B-查询一个任务下的摄像头信息")
    @RequestMapping(value = "/selectCameraByTaskId",method =RequestMethod.GET)
    public Result selectCameraByTaskId(@RequestParam(value = "taskId")Long taskId){
        Result result=new Result();
     try{
         result.setData(tCameraInfoService.selectCameraByTaskId(taskId));
     }catch (Exception e){
         result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
         log.error("失败描述",e);
     }
        return  result;
    }

    @ApiOperation(value = "查询所有摄像头预置位信息树")
    @RequestMapping(value = "/selectPresetTree", method = RequestMethod.GET)
    public Result selectPresetTree() {
        Result result = new Result();
        try {
            List<Map<String, Object>> cameraPresetTree = tCameraInfoService.selectPresetTree();
            result.setData(cameraPresetTree);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("摄像头预置位信息树失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "写入缓存")
    @RequestMapping(value = "/intoRedis", method = RequestMethod.GET)
    public Result intoRedis(){
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.intoRedis());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("写入缓存失败描述：", e);
        }
        return result;
    }

}
