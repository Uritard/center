package com.yjh.platform.module.device.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.device.service.TStdDeviceSAttrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/tStdDeviceAttr/v1")
@Api(value = "/tStdDeviceAttr", description = "标准化设备参数表操作接口")
public class TStdDeviceAttrController {

    @Autowired
    private final TStdDeviceSAttrService tStdDeviceSAttrService;

    private Logger log = LoggerFactory.getLogger(TStdDeviceController.class);

    public TStdDeviceAttrController(TStdDeviceSAttrService tStdDeviceSAttrService) {
        this.tStdDeviceSAttrService = tStdDeviceSAttrService;
    }

    //插入
    @ApiOperation(value = "插入")
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Result insert(@RequestBody TStdDeviceAttr tStdDeviceAttr){
        Result result = new Result();
          try {
              result.setData(tStdDeviceSAttrService.insert(tStdDeviceAttr));
          }catch (BusinessException b) {
              result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
          } catch (Exception e) {
              result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
              log.error("添加设备参数错误:", e);
          }
        return result;
    }

    //删除
    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceId", required = true) Long deviceId) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceSAttrService.deleteByPrimaryID(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除设备参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除设备参数错误:", e);
        }
        return result;
    }

    //更新
    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TStdDeviceAttr tStdDeviceAttr) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceSAttrService.update(tStdDeviceAttr));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新设备参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新设备参数错误:", e);
        }
        return result;
    }

    //查询 根据主键ID查询
    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId",required = true) Long deviceId){
        Result result = new Result();
        try {
            TStdDeviceAttr tStdDeviceAttr = this.tStdDeviceSAttrService.selectByPrimaryId(deviceId);
            result.setData(tStdDeviceAttr);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //查询
    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "deviceId", required = false) Long deviceId,
                         @RequestParam(value = "deviceSubtype", required = false) Integer deviceSubtype,
                         @RequestParam(value = "serial", required = false) String serial,
                         @RequestParam(value = "manufacturer", required = false) String manufacturer,
                         @RequestParam(value = "supplier", required = false) String supplier,
                         @RequestParam(value = "productionDate", required = false) Date productionDate,
                         @RequestParam(value = "openingDate", required = false) Date openingDate,
                         @RequestParam(value = "disableDate", required = false) Date disableDate,
                         @RequestParam(value = "lastMaintenance", required = false) Date lastMaintenance,
                         @RequestParam(value = "maintenanceCycle", required = false) String maintenanceCycle,
                         @RequestParam(value = "organization", required = false) String organization,
                         @RequestParam(value = "department", required = false) String department,
                         @RequestParam(value = "responsiblePerson", required = false) String responsiblePerson,
                         @RequestParam(value = "latitude", required = false) String latitude,
                         @RequestParam(value = "longitude", required = false) String longitude,
                         @RequestParam(value = "remark", required = false) String remark,
                         @RequestParam(value = "para1", required = false) String para1,
                         @RequestParam(value = "para2", required = false) String para2,
                         @RequestParam(value = "para3", required = false) String para3
                         ){
        Result result = new Result();
        try {
            List<TStdDeviceAttr> list = this.tStdDeviceSAttrService.select(deviceId,deviceSubtype,serial,manufacturer,supplier,
                    productionDate,openingDate,disableDate,lastMaintenance,
                    maintenanceCycle,organization,department,responsiblePerson,
                    latitude,longitude,remark,para1,para2,para3);
            result.setData(list);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //分页查询
    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TStdDeviceAttr tStdDeviceAttr,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TStdDeviceAttr> list = tStdDeviceSAttrService.selectByPage(tStdDeviceAttr);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
}
