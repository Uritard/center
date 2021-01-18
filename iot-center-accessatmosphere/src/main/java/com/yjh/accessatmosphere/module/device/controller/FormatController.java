package com.yjh.accessatmosphere.module.device.controller;

import com.yjh.accessatmosphere.common.Constant;
import com.yjh.accessatmosphere.commons.result.BusinessException;
import com.yjh.accessatmosphere.commons.result.Result;
import com.yjh.accessatmosphere.commons.result.ResultCodeEnum;
import com.yjh.accessatmosphere.commons.utils.weatherUtils.ParamConfig;
import com.yjh.accessatmosphere.module.device.service.FormatService;
import com.yjh.accessatmosphere.module.device.entity.Format;
import java.util.HashMap;
import java.util.List;

import com.yjh.accessatmosphere.thread.ListerThread;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/format/v1")
@Api(value = "/format", description = "测试表操作接口")
public class FormatController {

    @Autowired
    private final FormatService formatService;

    private Logger log = LoggerFactory.getLogger(FormatController.class);

    public FormatController(FormatService formatService) {
        this.formatService = formatService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody Format format) {
        Result result = new Result();
        try {
            result.setData(formatService.insert(format));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加etl错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "testId", required = true) String testId) {
        Result result = new Result();
        try {
            result.setData(formatService.deleteByPrimaryId(testId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除etl异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody Format format) {
        Result result = new Result();
        try {
            result.setData(formatService.update(format));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新etl异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "testId", required = true) String testId) {
        Result result = new Result();
        try {
            Format format = formatService.selectByPrimaryId(testId);
            result.setData(format);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select() {
        Result result = new Result();
        try {
            result.setData("success");
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody Format format,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<Format> list = formatService.selectByPage(format);
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
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<Format> list) {
        Result result = new Result();
        try {
        result.setData(formatService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "设置接受信息服务的ip")
    @RequestMapping(value = "/setPath", method = RequestMethod.GET)
    public Result getWeatherInfo(@RequestParam(value = "path") String path) {
        Result result = new Result();
        try {
            Constant.path = path;
            log.info("设置成功"+Constant.path);
            result.setData(1);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设置接受信息服务的ip失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "设置微气象设备的相关信息")
    @RequestMapping(value = "/setConf", method = RequestMethod.GET)
    public Result setConf(@RequestParam(value = "serialNumber",required = false,defaultValue = "COM3") String serialNumber,
                          @RequestParam(value = "baudRate",required = false,defaultValue = "19200") int baudRate,
                          @RequestParam(value = "checkoutBit",required = false,defaultValue = "0") int checkoutBit,
                          @RequestParam(value = "dataBit",required = false,defaultValue = "8") int dataBit,
                          @RequestParam(value = "stopBit",required = false,defaultValue = "1") int stopBit) {
        Result result = new Result();
        try {
            ParamConfig paramConfig = new ParamConfig(serialNumber, baudRate, checkoutBit, dataBit, stopBit);
            Constant.serialPort.closeSerialPort();
            Thread thread = new ListerThread(paramConfig);
            thread.start();
            result.setData("ok");
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("设置微气象设备的相关信息失败描述：", e);
        }
        return result;
    }

}
