package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.TStdDeviceTypeModel;
import com.yjh.platform.module.device.service.TStdDeviceTypeModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author hyh
 * @since 2022/4/15
 **/
@RestController
@Slf4j
@RequestMapping("/tStdDeviceTypeModel/v1")
@Api(value = "/tStdDeviceTypeModel", description = "设备类型模板表操作接口")
public class TStdDeviceTypeModelController {

    @Resource
    private TStdDeviceTypeModelService tStdDeviceTypeModelService;

    public TStdDeviceTypeModelController(TStdDeviceTypeModelService tStdDeviceTypeModelService){
        this.tStdDeviceTypeModelService = tStdDeviceTypeModelService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增设备类型模型",content = "根据用户传递的参数新增设备类型模型",logType = 2,authority = "1234")
    public Result add(@Validated @RequestBody TStdDeviceTypeModel tStdDeviceTypeModel)  {
        Result result = new Result();
        try {
            result.setData(tStdDeviceTypeModelService.add(tStdDeviceTypeModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设备类型模型添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除设备类型模型",content = "根据用户传递的参数删除设备类型模型",logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "deviceTypeId") Long deviceTypeId) {
        Result result = new Result();
        try {
            result.setData(tStdDeviceTypeModelService.deleteByPrimaryId(deviceTypeId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("设备类型模型删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设备类型模型删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改设备类型模型",content = "根据用户传递的参数修改设备类型模型",logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody TStdDeviceTypeModel tStdDeviceTypeModel, HttpServletRequest request) {
        Result result = new Result();
        try {
            int i = tStdDeviceTypeModelService.update(tStdDeviceTypeModel);
            if (i==0) {
                result.setMessage("AI判别不能和识别算法或者AI缺陷同时选择！");
                result.setCode(10102);
            } else { result.setData(i); }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("设备类型模型更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设备类型模型更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询设备类型模型",content = "根据用户传递的参数查询设备类型模型",logType = 1, authority = "1234")
    public Result selectByPrimaryId(@RequestParam(value = "deviceTypeId") Long deviceTypeId) {
        Result result = new Result();
        try {
            TStdDeviceTypeModel tStdDeviceTypeModel = tStdDeviceTypeModelService.selectByPrimaryId(deviceTypeId);
            result.setData(tStdDeviceTypeModel);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }
}
