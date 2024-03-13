package com.yjh.platform.module.iot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.entity.TIotDeviceWarn;
import com.yjh.platform.module.iot.service.TIotDeviceWarnService;
import com.yjh.platform.module.task.entity.TWarnInfoDetail;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/12/19
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/tIotDeviceWarn/v1")
@Api(value = "tIotDeviceWarn", tags = "物联设备告警接口")
@Slf4j
public class TIotDeviceWarnController {

    @Resource
    private TIotDeviceWarnService tIotDeviceWarnService;

    @ApiOperation(value = "新增物联设备告警")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增物联设备告警", content = "新增物联设备告警", logType = 2)
    public Result add(@RequestBody TIotDeviceWarn tIotDeviceWarn) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceWarnService.save(tIotDeviceWarn));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增物联设备告警失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新物联设备告警")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "更新物联设备告警", content = "更新物联设备告警", logType = 3)
    public Result update(@RequestBody TIotDeviceWarn tIotDeviceWarn) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceWarnService.update(tIotDeviceWarn));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新物联设备告警失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "删除物联设备告警")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除物联设备告警", content = "删除物联设备告警", logType = 4)
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceWarnService.removeById(id));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除物联设备告警失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "处理上报的物联设备告警数据")
    @RequestMapping(value = "/robotIotWarn", method = RequestMethod.POST)
    public Result robotIotWarn(@RequestBody Map<String, String> iotWarn) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceWarnService.robotIotWarn(iotWarn));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("处理物联设备告警失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "物联设备告警弹框")
    @GetMapping(value = "/iotWarnPopUp")
    public Result iotWarnPopUp(@RequestParam(value = "warnId") Long warnId) {
        Result result = new Result();
        try {
            TWarnInfoDetail tWarnInfoDetail = tIotDeviceWarnService.selectIotDeviceWarn(warnId);
            result.setData(tWarnInfoDetail);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询机器人本体告警弹框内容失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询物联设备告警")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询物联设备告警", content = "查询物联设备告警", logType = 1, authority = "1235")
    public Result select(@RequestParam(value = "pointName", required = false) String pointName,
                         @RequestParam(value = "startTime", required = false) String startTime,
                         @RequestParam(value = "endTime", required = false) String endTime,
                         @RequestParam(value = "iotDeviceName", required = false) String iotDeviceName,
                         @RequestParam(value = "deleteFlag", required = false) Long deleteFlag,
                         @RequestParam(value = "iotDeviceType", required = false) Long iotDeviceType,
                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        Result result = new Result();
        try {
            DateTimeUtil.checkDate(startTime, endTime);

            QueryWrapper<TIotDeviceWarn> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotBlank(pointName)) {
                queryWrapper.like("point_name", pointName);
            }
            if (StringUtils.isNotBlank(iotDeviceName)) {
                queryWrapper.like("iot_device_name", iotDeviceName);
            }
            if (Objects.nonNull(deleteFlag)) {
                queryWrapper.eq("delete_flag", deleteFlag);
            }
            if (Objects.nonNull(iotDeviceType) && iotDeviceType != -1) {
                queryWrapper.eq("iot_device_type", iotDeviceType);
            }
            if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                queryWrapper.between("alarm_time", startTime, endTime);
            }
            queryWrapper.orderByAsc("delete_flag");
            queryWrapper.orderByDesc("alarm_time");
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TIotDeviceWarn> tIotDeviceWarnList = tIotDeviceWarnService.list(queryWrapper);
            DictConvertUtil.optional("iotDeviceType").covertToDict(tIotDeviceWarnList);
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", tIotDeviceWarnList);
            result.setData(resultMap);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException e) {
            result.setCode(e.getCode(), e.getMessage());
            log.error("日志统计失败：", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询物联设备测点失败：", e);
        }
        return result;
    }
}
