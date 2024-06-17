package com.yjh.platform.module.maintain.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.maintain.entity.DeviceMaintenanceInfo;
import com.yjh.platform.module.maintain.service.DeviceMaintenanceInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/4/25
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/deviceMaintenanceInfo/v1")
@Api(value = "deviceMaintenanceInfo", tags = "设备维护信息接口")
@Slf4j
public class DeviceMaintenanceInfoController {

    @Resource
    private DeviceMaintenanceInfoService deviceMaintenanceInfoService;

    @ApiOperation(value = "新增设备维护信息")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增设备维护信息", content = "新增设备维护信息", logType = 2, authority = "1234")
    public Result add(@RequestBody @Validated DeviceMaintenanceInfo deviceMaintenanceInfo) {
        Result result = new Result();
        try {
            deviceMaintenanceInfo.setStartTime(new Date())
                    .setRecordCode(String.valueOf(UUID.randomUUID()).replace("-", ""));
            result.setData(deviceMaintenanceInfoService.save(deviceMaintenanceInfo));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
            //有变动 同步模型
            if (Constant.updateSyncModel()) {
                String modelType = "10";
                Constant.modelUpload(modelType);
            }
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增设备维护信息失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新设备维护信息")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "更新设备维护信息", content = "新增设备维护信息", logType = 3, authority = "1234")
    public Result update(@RequestBody @Validated DeviceMaintenanceInfo deviceMaintenanceInfo) {
        Result result = new Result();
        try {
            if (deviceMaintenanceInfo.getMaintenanceStatus() == 1) {
                deviceMaintenanceInfo.setEndTime(new Date());
            }
            result.setData(deviceMaintenanceInfoService.updateById(deviceMaintenanceInfo));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
            //有变动 同步模型
            if (Constant.updateSyncModel()) {
                String modelType = "10";
                Constant.modelUpload(modelType);
            }
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新设备维护信息失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "删除设备维护信息")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除设备维护信息", content = "新增设备维护信息", logType = 4, authority = "1234")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(deviceMaintenanceInfoService.removeById(id));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
            //有变动 同步模型
            if (Constant.updateSyncModel()) {
                String modelType = "10";
                Constant.modelUpload(modelType);
            }
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除设备维护信息失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除设备维护信息")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST)
    @Logs(title = "删除设备维护信息", content = "新增设备维护信息", logType = 4, authority = "1234")
    public Result deleteSelectedDeviceInfo(@RequestParam(value = "ids", required = true) String ids) {
        Result result = new Result();
        try {
            result.setData(deviceMaintenanceInfoService.removeByIds(Arrays.asList(ids.split(","))));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
            //有变动 同步模型
            if (Constant.updateSyncModel()) {
                String modelType = "10";
                Constant.modelUpload(modelType);
            }
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量删除设备维护信息失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询设备维护信息")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询设备维护信息", content = "查询设备维护信息", authority = "1234")
    public Result select(@RequestParam(value = "deviceId", required = false) String deviceId,
                         @RequestParam(value = "deviceName", required = false) String deviceName,
                         @RequestParam(value = "deviceType", required = false, defaultValue = "-1") Integer deviceType,
                         @RequestParam(value = "maintenanceType", required = false, defaultValue = "-1") Integer maintenanceType,
                         @RequestParam(value = "startTime", required = false) String startTime,
                         @RequestParam(value = "endTime", required = false) String endTime,
                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                         @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        try {
            QueryWrapper<DeviceMaintenanceInfo> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotBlank(deviceId)) {
                queryWrapper.eq("device_id", deviceId);
            }
            if (StringUtils.isNotBlank(deviceName)) {
                queryWrapper.like("patroldevice_name", deviceName);
            }
            if (Objects.nonNull(deviceType) && deviceType != -1) {
                queryWrapper.eq("patroldevice_type", deviceType);
            }
            if (Objects.nonNull(maintenanceType) && maintenanceType != -1) {
                queryWrapper.eq("maintenance_type", maintenanceType);
            }
            if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                queryWrapper.between("start_time", startTime, endTime);
            }
            queryWrapper.orderByDesc("start_time");
            Page<DeviceMaintenanceInfo> page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<DeviceMaintenanceInfo> deviceMaintenanceInfoList = deviceMaintenanceInfoService.list(queryWrapper);
            DictConvertUtil.optional("cruiseDeviceType", "patroldeviceType", "patroldeviceTypeName")
                    .add("maintenanceType").covertToDict(deviceMaintenanceInfoList);
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", deviceMaintenanceInfoList);
            result.setData(resultMap);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询设备维护信息失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询最近一次设备维护信息")
    @RequestMapping(value = "/selectLastTime", method = RequestMethod.GET)
    @Logs(title = "查询最近一次设备维护信息", content = "查询最近一次设备维护信息", authority = "1234")
    public Result select(@RequestParam(value = "deviceId", required = false) Long deviceId) {
        Result result = new Result();
        try {
            result.setData(deviceMaintenanceInfoService.selectLastTime(deviceId));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询设备维护信息失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视设备树(机器人 无人机 摄像机 录像机 声纹)查询")
    @RequestMapping(value = "/selectCruiseDeviceTree", method = RequestMethod.GET)
    @Logs(title = "巡视设备树查询",content = "巡视设备树查询", authority = "1234")
    public Result selectCruiseDeviceTree(@RequestParam(value = "name", required = false) String name) {
        Result result = new Result();
        try {
            List<AreaInfo> devTreeList = deviceMaintenanceInfoService.selectCruiseDeviceTree();
            if (StringUtils.isNotEmpty(name)) {
                deviceMaintenanceInfoService.filter(devTreeList, name);
            }
            result.setData(devTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡视设备树查询失败：", e);
        }
        return result;
    }
}
