package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.TCfgDeviceDetail;
import com.yjh.platform.module.device.service.TCfgDeviceService;
import com.yjh.platform.module.device.entity.TCfgDevice;
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
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author lqh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCfgDevice/v1")
@Api(value = "/tCfgDevice", description = "设备表操作接口")
public class TCfgDeviceController {

    @Autowired
    private final TCfgDeviceService tCfgDeviceService;

    private Logger log = LoggerFactory.getLogger(TCfgDeviceController.class);

    public TCfgDeviceController(TCfgDeviceService tCfgDeviceService) {
        this.tCfgDeviceService = tCfgDeviceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增",content = "根据用户传递的参数新增设备数据",logType = 2)
    public Result add(@RequestBody TCfgDevice tCfgDevice) {
        Result result = new Result();
        try {
            result.setData(tCfgDeviceService.insert(tCfgDevice));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设备表操作添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除",content = "根据用户传递的参数删除设备数据",logType = 4)
    public Result delete(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            result.setData(tCfgDeviceService.deleteByPrimaryId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("设备表操作删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设备表操作删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改",content = "根据用户传递的参数更新设备数据",logType = 3)
    public Result update(@RequestBody TCfgDevice tCfgDevice) {
        Result result = new Result();
        try {
            result.setData(tCfgDeviceService.update(tCfgDevice));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("设备表操作更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设备表操作更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询设备信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            TCfgDevice tCfgDevice = tCfgDeviceService.selectByPrimaryId(deviceId);
            result.setData(tCfgDevice);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设备表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询设备信息",logType = 1)
    public Result select(@RequestParam(value = "deviceId", required = false) String deviceId,
                            @RequestParam(value = "deviceName", required = false) String deviceName,
                            @RequestParam(value = "deviceType", required = false) String deviceType,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "stationId", required = false) String stationId,
                            @RequestParam(value = "relationCode", required = false) String relationCode,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TCfgDevice> list = tCfgDeviceService.select(deviceId, deviceName, deviceType, deviceCode, stationId, relationCode, createTime, updateTime, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设备表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询",content = "根据用户传递的参数分页查询设备信息",logType = 1)
    public Result selectByPage(@RequestBody TCfgDeviceDetail tCfgDeviceDetail,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<HashMap<String,Object>> list = tCfgDeviceService.selectByPage(tCfgDeviceDetail);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设备表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入",content = "根据用户传递的参数批量插入设备信息",logType = 2)
    public Result batchAdd(@RequestBody List<TCfgDevice> list) {
        Result result = new Result();
        try {
        result.setData(tCfgDeviceService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("设备表操作批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "生成四遥测点")
    @RequestMapping(value = "/createSYPoint", method = RequestMethod.POST)
    //@Logs(title = "生成",content = "根据用户传递的参数查询",logType = 5)
    public Result createSYPoint(@RequestBody List<Map<String,String>> list) {
        Result result = new Result();
        try {
            result.setData(tCfgDeviceService.createSYPoint(list));
        }catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("生成四遥测点失败：" + e);
        }
        return result;
    }

}
