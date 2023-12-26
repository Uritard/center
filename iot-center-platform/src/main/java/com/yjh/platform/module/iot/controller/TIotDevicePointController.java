package com.yjh.platform.module.iot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.service.TIotDevicePointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
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
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/tIotDevicePoint/v1")
@Api(value = "tIotDevicePoint", tags = "物联设备测点接口")
@Slf4j
public class TIotDevicePointController {

    @Resource
    private TIotDevicePointService tIotDevicePointService;

    @ApiOperation(value = "新增物联设备测点配置")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增物联设备测点配置", content = "新增物联设备测点配置", logType = 2, authority = "1234")
    public Result add(@RequestBody @Validated TIotDevicePoint tIotDevicePoint) {
        Result result = new Result();
        try {
            QueryWrapper<TIotDevicePoint> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("channel_num", tIotDevicePoint.getChannelNum()).eq("iot_device_id", tIotDevicePoint.getIotDeviceId());
            int count = tIotDevicePointService.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(209, "该物联设备通道号已使用");
            }
            result.setData(tIotDevicePointService.save(tIotDevicePoint));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增物联设备测点配置失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新物联设备测点配置")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "更新物联设备测点配置", content = "新增物联设备测点配置", logType = 3, authority = "1234")
    public Result update(@RequestBody @Validated TIotDevicePoint tIotDevicePoint) {
        Result result = new Result();
        try {
            result.setData(tIotDevicePointService.updateById(tIotDevicePoint));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新物联设备测点配置失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "删除物联设备测点")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除物联设备测点", content = "新增物联设备测点配置", logType = 4, authority = "1234")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(tIotDevicePointService.removeById(id));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除物联设备测点失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询物联设备测点")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询物联设备测点", content = "新增物联设备测点配置", logType = 1, authority = "1234")
    public Result select(@RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "iotDeviceId", required = false) Long iotDeviceId,
                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        Result result = new Result();
        try {
            QueryWrapper<TIotDevicePoint> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("point_name", name);
            }
            if (Objects.nonNull(iotDeviceId)) {
                queryWrapper.eq("iot_device_id", iotDeviceId);
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TIotDevicePoint> tIotDeviceList = tIotDevicePointService.list(queryWrapper);
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", tIotDeviceList);
            result.setData(resultMap);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询物联设备测点失败：", e);
        }
        return result;
    }
}
