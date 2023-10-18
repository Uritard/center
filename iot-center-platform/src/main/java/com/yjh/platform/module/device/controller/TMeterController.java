package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.TMeter;
import com.yjh.platform.module.device.service.TMeterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/tMeter/v1")
@Api(value = "tMeter", description = "电表操作接口")
@Slf4j
public class TMeterController {
    @Autowired
    private TMeterService tMeterService;

    @ApiOperation(value = "查询电表信息")
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @Logs(title = "查询电表信息", content = "根据用户传递的参数查询电表信息", logType = 2, authority = "1235")
    public Result query(@RequestParam("upRegionId") Long upRegionId, @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                        @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        try {
            result = tMeterService.select(upRegionId, pageNum, pageSize);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除电表失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "新增电表配置")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增电表配置", content = "新增电表配置", logType = 2, authority = "1234")
    public Result add(@RequestBody TMeter tMeter) {
        Result result = new Result();
        try {
            result.setData(tMeterService.insert(tMeter));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增电表配置失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "删除电表")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @Logs(title = "删除电表", content = "新增电表配置", logType = 2, authority = "1234")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(tMeterService.deleteByPrimaryKey(id));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除电表失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "采集电量")
    @RequestMapping(value = "/collect", method = RequestMethod.GET)
    @Logs(title = "采集电量", content = "采集电量", logType = 2, authority = "1234")
    public Result collectData(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result = tMeterService.collectData(id);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("采集电量失败", e);
        }
        return result;
    }

    @ApiOperation(value = "电量记录历史")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @Logs(title = "电量记录历史", content = "电量记录历史", logType = 2, authority = "1234")
    public Result list(@RequestBody TMeter tMeter) {
        Result result = new Result();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (StringUtils.isNotEmpty(tMeter.getStartTime()) && StringUtils.isNotEmpty(tMeter.getEndTime())) {
                LocalDate startTime = LocalDate.parse(tMeter.getStartTime(), formatter);
                LocalDate endTime = LocalDate.parse(tMeter.getEndTime(), formatter);
                if (ChronoUnit.DAYS.between(startTime, endTime) > 30) {
                    throw new RuntimeException("时间范围不可超过30天");
                }
            } else {
                tMeter.setStartTime(LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN).format(formatter));
                tMeter.setEndTime(LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(formatter));
            }
            result.setData(tMeterService.list(tMeter));
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("统计微气象数据信息:", e);
        }
        return result;

    }
}
