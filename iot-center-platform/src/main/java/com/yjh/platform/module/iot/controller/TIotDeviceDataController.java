package com.yjh.platform.module.iot.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.iot.entity.TIotDeviceData;
import com.yjh.platform.module.iot.service.TIotDeviceDataService;
import com.yjh.platform.module.task.entity.EnvDeviceStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/tIotDeviceData/v1")
@Api(value = "tIotDeviceData", tags = "物联设备结果接口")
@Slf4j
public class TIotDeviceDataController {

    @Resource
    private TIotDeviceDataService tIotDeviceDataService;

    @ApiOperation(value = "新增物联设备结果配置")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增物联设备结果配置", content = "新增物联设备结果配置", logType = 2)
    public Result add(@RequestBody TIotDeviceData tIotDeviceData) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceDataService.save(tIotDeviceData));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增物联设备结果配置失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新物联设备结果配置")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "更新物联设备结果配置", content = "更新物联设备结果配置", logType = 3)
    public Result update(@RequestBody TIotDeviceData tIotDeviceData) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceDataService.updateById(tIotDeviceData));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新物联设备结果配置失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "删除物联设备结果")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除物联设备结果", content = "删除物联设备结果", logType = 4)
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceDataService.removeById(id));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除物联设备结果失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询物联设备结果曲线")
    @RequestMapping(value = "/selectIotLine", method = RequestMethod.GET)
    @Logs(title = "查询物联设备结果曲线", content = "查询物联设备结果曲线", logType = 1)
    public Result selectIotLine(@RequestParam(value = "iotDeviceId") Long iotDeviceId,
                                @RequestParam(value = "startTime") String startTime,
                                @RequestParam(value = "endTime") String endTime) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceDataService.selectIotLine(iotDeviceId, startTime, endTime));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询物联设备结果失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询物联设备结果")
    @RequestMapping(value = "/selectIotData", method = RequestMethod.GET)
    @Logs(title = "查询物联设备结果", content = "查询物联设备结果", logType = 1)
    public Result selectIotData(@RequestParam(value = "upRegionId", required = false) Long upRegionId) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceDataService.selectIotData(upRegionId));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询物联设备结果失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "下级上报环控设备信息入库")
    @RequestMapping(value = "/envData", method = RequestMethod.POST)
    public Result insertEnvData(@RequestBody List<EnvDeviceStatus> envDeviceStatusList) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceDataService.insertEnvData(envDeviceStatusList));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询物联设备结果失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "新增物联设备结果配置")
    @RequestMapping(value = "/addToRedis", method = RequestMethod.POST)
    @Logs(title = "新增物联设备结果配置", content = "新增物联设备结果配置", logType = 2)
    public Result addToRedis(@RequestBody List<IotDeviceDataEx> dataList) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceDataService.addToRedis(dataList));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增物联设备结果配置失败：", e);
        }
        return result;
    }

}
