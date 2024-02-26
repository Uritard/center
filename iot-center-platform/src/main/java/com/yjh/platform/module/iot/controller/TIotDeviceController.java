package com.yjh.platform.module.iot.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.yjh.platform.module.iot.entity.TIotDeviceExtend;
import com.yjh.platform.module.iot.service.TIotDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
@RequestMapping("/tIotDevice/v1")
@Api(value = "tIotDevice", tags = "物联设备接口")
@Slf4j
public class TIotDeviceController {

    @Resource
    private TIotDeviceService tIotDeviceService;

    @Resource
    private TStdRegionService tStdRegionService;

    @ApiOperation(value = "新增物联设备配置")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增物联设备配置", content = "新增物联设备配置", logType = 2, authority = "1234")
    public Result add(@RequestBody @Validated TIotDeviceExtend tIotDevice) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceService.save(tIotDevice));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增物联设备配置失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新物联设备配置")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "更新物联设备配置", content = "新增物联设备配置", logType = 3, authority = "1234")
    public Result update(@RequestBody @Validated TIotDeviceExtend tIotDevice) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceService.updateById(tIotDevice));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新物联设备配置失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "删除物联设备")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除物联设备", content = "新增物联设备配置", logType = 4, authority = "1234")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(tIotDeviceService.removeById(id));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除物联设备失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询物联设备")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询物联设备", content = "查询物联设备配置", logType = 1, authority = "1234")
    public Result select(@RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "regionId", required = false) Long regionId,
                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                         @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        try {
            QueryWrapper<TIotDevice> queryWrapper = new QueryWrapper<>();
            List<Long> list = tStdRegionService.selectDownId(regionId);
            if (CollectionUtils.isNotEmpty(list)) {
                list.add(regionId);
                queryWrapper.in("up_region_id", list);
            }
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("device_name", name);
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TIotDevice> tIotDeviceList = tIotDeviceService.list(queryWrapper);
            DictConvertUtil.optional("iotDeviceType").add("protocolModel").add("frequency", "collectionFrequency", "collectionFrequencyName").covertToDict(tIotDeviceList);
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", tIotDeviceList);
            result.setData(resultMap);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询物联设备失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "物联设备树查询(level：5-区域，6-设备)")
    @RequestMapping(value = "/selectDevTree", method = RequestMethod.GET)
    @Logs(title = "物联设备树查询", content = "物联设备树查询", logType = 1)
    public Result selectDevTree(@RequestParam(value = "level", required = false) String level,
                                @RequestParam(value = "name", required = false) String name,
                                @RequestParam(value = "id", required = false) Long id) {
        Result result = new Result();
        try {
            List<AreaInfo> devTreeList = tIotDeviceService.selectDevTree(level, id, name);
            result.setData(devTreeList);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("物联设备树查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "智能环境设备控制")
    @PostMapping(value = "/envDeviceControl")
    @Logs(title = "智能环境设备控制",content = "根据用户传递的参数控制智能环境设备",logType = 5, authority = "1235")
    public Result envDeviceControl(@RequestBody Map<String, Object> map) {
        log.info("智能环境设备控制");
        Result result = new Result();
        try {
            if (MapUtils.isNotEmpty(map)) {
                return tIotDeviceService.envDeviceControl(map);
            } else {
                result.setCode(ResultCodeEnum.PARAMERROR.getCode(), ResultCodeEnum.PARAMERROR.getName());
                return result;
            }
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("智能环境设备控制异常", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

}
