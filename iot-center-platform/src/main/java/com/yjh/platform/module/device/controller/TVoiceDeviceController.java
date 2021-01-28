package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.VoiceDevice;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import java.util.HashMap;
import java.util.List;

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
 * @since 2020-12-01
 */
@RestController
@RequestMapping("/t-voice-device/v1")
@Api(value = "/t-voice-device", description = "声纹设备表操作接口")
public class TVoiceDeviceController {

    @Autowired
    private final TVoiceDeviceService tVoiceDeviceService;

    private Logger log = LoggerFactory.getLogger(TVoiceDeviceController.class);

    public TVoiceDeviceController(TVoiceDeviceService tVoiceDeviceService) {
        this.tVoiceDeviceService = tVoiceDeviceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增",content = "根据用户传递的参数新增声纹设备数据",logType = 2)
    public Result add(@RequestBody TVoiceDevice tVoiceDevice) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.add(tVoiceDevice));
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
    @Logs(title = "删除",content = "根据用户传递的参数删除声纹设备数据",logType = 4)
    public Result delete(@RequestParam(value = "voiceDeviceId", required = true) String voiceDeviceId) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.deleteByPrimaryId(voiceDeviceId));
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
    @Logs(title = "修改",content = "根据用户传递的参数修改声纹设备数据",logType = 3)
    public Result update(@RequestBody TVoiceDevice tVoiceDevice) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.update(tVoiceDevice));
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
    @Logs(title = "查询",content = "根据用户传递的参数查询声纹设备",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "voiceDeviceId", required = true) String voiceDeviceId) {
        Result result = new Result();
        try {
            TVoiceDevice tVoiceDevice = tVoiceDeviceService.selectByPrimaryId(voiceDeviceId);
            result.setData(tVoiceDevice);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询声纹设备",logType = 1)
    public Result select(@RequestParam(value = "voiceDeviceId", required = false) String voiceDeviceId,
                            @RequestParam(value = "voiceDeviceName", required = false) String voiceDeviceName,
                            @RequestParam(value = "stdDeviceId", required = false) Long stdDeviceId,
                            @RequestParam(value = "deviceType", required = false) String deviceType,
                            @RequestParam(value = "configId", required = false) String configId) {
        Result result = new Result();
        try {
            List<TVoiceDevice> list = tVoiceDeviceService.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询",content = "根据用户传递的参数分页查询声纹设备",logType = 1)
    public Result selectByPage(@RequestBody TVoiceDevice tVoiceDevice,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TVoiceDevice> list = tVoiceDeviceService.selectByPage(tVoiceDevice);
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
    @Logs(title = "批量插入",content = "根据用户传递的参数批量插入声纹设备数据",logType = 2)
    public Result batchAdd(@RequestBody List<TVoiceDevice> list) {
        Result result = new Result();
        try {
        result.setData(tVoiceDeviceService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    @Logs(title = "删除",content = "根据用户传递的参数批量删除声纹设备",logType = 4)
    public Result batchDelete(@RequestParam(value = "voiceDeviceIds") String voiceDeviceIds) {
    Result result = new Result();
    try {
        result.setData(tVoiceDeviceService.batchDelete(voiceDeviceIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }


    @ApiOperation(value = "查询音频设备树")
    @RequestMapping(value = "/selectVoiceDeviceTree", method = RequestMethod.GET)
    @Logs(title = "查询",content = "音频设备树查询",logType = 1)
    public Result selectVoiceDeviceTree() {
        Result result = new Result();
        try {
            List<VoiceDevice> list = tVoiceDeviceService.selectVoiceDeviceTree();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


}
