package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.handler.JurisdictionException;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdDeviceMeteDetail;
import com.yjh.platform.module.device.service.TStdDevicemeteService;
import com.yjh.platform.module.device.entity.TStdDeviceMete;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
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

import javax.servlet.http.HttpServletRequest;


/**
 * @author tt
 * @since 2020-08-08
 */
@RestController
@RequestMapping("/tStdDeviceMete/v1")
@Api(value = "/tStdDeviceMete", description = "标准设备测点表操作接口")
public class TStdDevicemeteController {

    @Autowired
    private final TStdDevicemeteService tStdDevicemeteService;

    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;

    private Logger log = LoggerFactory.getLogger(TStdDevicemeteController.class);

    public TStdDevicemeteController(TStdDevicemeteService tStdDevicemeteService) {
        this.tStdDevicemeteService = tStdDevicemeteService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@Validated  @RequestBody TStdDeviceMeteDetail tStdDeviceMeteDetail, HttpServletRequest request)  {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            if (userId.intValue() != 10001) {
                if (tStdDeviceMeteDetail.getLowLimit1() != null || tStdDeviceMeteDetail.getLowLimit2() != null || tStdDeviceMeteDetail.getLowLimit3() != null || tStdDeviceMeteDetail.getLowLimit4() != null
                        || tStdDeviceMeteDetail.getHighLimit1() != null || tStdDeviceMeteDetail.getHighLimit2() != null || tStdDeviceMeteDetail.getHighLimit3() != null || tStdDeviceMeteDetail.getHighLimit4() != null
                        || tStdDeviceMeteDetail.getAlarmState() != null) {
                    throw new JurisdictionException();
                }
            }
            result.setData(tStdDevicemeteService.add(tStdDeviceMeteDetail));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准设备测点添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceMeteId") Long deviceMeteId) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.deleteByPrimaryId(deviceMeteId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("标准设备测点删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准设备测点删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TStdDeviceMeteDetail tStdDeviceMeteDetail, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            if (userId.intValue() != 10001) {
                TStdDeviceMete tStdDeviceMete = tStdDevicemeteService.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());
                if (tStdDeviceMeteDetail.getLowLimit1() != tStdDeviceMete.getLowLimit1() || tStdDeviceMeteDetail.getLowLimit2() != tStdDeviceMete.getLowLimit2() || tStdDeviceMeteDetail.getLowLimit3() != tStdDeviceMete.getLowLimit3() || tStdDeviceMeteDetail.getLowLimit4() != tStdDeviceMete.getLowLimit4()
                        || tStdDeviceMeteDetail.getHighLimit1() != tStdDeviceMete.getHighLimit1() || tStdDeviceMeteDetail.getHighLimit2() != tStdDeviceMete.getHighLimit2()  || tStdDeviceMeteDetail.getHighLimit3() != tStdDeviceMete.getHighLimit3()  || tStdDeviceMeteDetail.getHighLimit4() != tStdDeviceMete.getHighLimit4()
                        || tStdDeviceMeteDetail.getAlarmState() != tStdDeviceMete.getAlarmState()) {
                    throw new JurisdictionException();
                }
            }
            result.setData(tStdDevicemeteService.update(tStdDeviceMeteDetail));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("标准设备测点更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceMeteId") Long deviceMeteId) {
        Result result = new Result();
        try {
            TStdDeviceMete tStdDeviceMete = tStdDevicemeteService.selectByPrimaryId(deviceMeteId);
            result.setData(tStdDeviceMete);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                         @RequestParam(value = "deviceId", required = false) Long deviceId,
                         @RequestParam(value = "customId", required = false) String customId,
                         @RequestParam(value = "meteId", required = false) Long meteId,
                         @RequestParam(value = "meteKind", required = false) String meteKind,
                         @RequestParam(value = "meteType", required = false) String meteType,
                         @RequestParam(value = "meteName", required = false) String meteName,
                         @RequestParam(value = "deviceType", required = false) Integer deviceType,
                         @RequestParam(value = "positionType", required = false) String positionType,
                         @RequestParam(value = "analyseType", required = false) Integer analyseType,
                         @RequestParam(value = "unit", required = false) String unit,
                         @RequestParam(value = "alarmNote", required = false) String alarmNote,
                         @RequestParam(value = "alarmType", required = false) String alarmType,
                         @RequestParam(value = "upEffect", required = false) Float upEffect,
                         @RequestParam(value = "downEffect", required = false) Float downEffect,
                         @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                         @RequestParam(value = "highLimit1", required = false) Float highLimit1,
                         @RequestParam(value = "lowLimit1", required = false) Float lowLimit1,
                         @RequestParam(value = "highLimit2", required = false) Float highLimit2,
                         @RequestParam(value = "lowLimit2", required = false) Float lowLimit2,
                         @RequestParam(value = "alarmDelay", required = false) Integer alarmDelay,
                         @RequestParam(value = "alarmCnt", required = false) Integer alarmCnt,
                         @RequestParam(value = "thresholdAbs", required = false) BigDecimal thresholdAbs,
                         @RequestParam(value = "thresholdPer", required = false) BigDecimal thresholdPer,
                         @RequestParam(value = "modulus", required = false) Integer modulus,
                         @RequestParam(value = "remark", required = false) String remark,
                         @RequestParam(value = "stateZero", required = false) String stateZero,
                         @RequestParam(value = "stateOne", required = false) String stateOne,
                         @RequestParam(value = "alarmState", required = false) Integer alarmState,
                         @RequestParam(value = "meterType", required = false) Integer meterType,
                         @RequestParam(value = "appearanceType", required = false) Integer appearanceType) {
        Result result = new Result();
        try {
            List<TStdDeviceMete> list = tStdDevicemeteService.select(deviceMeteId, deviceId, customId, meteId, meteKind, meteType, meteName, deviceType, positionType, analyseType, unit, alarmNote, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark, stateZero, stateOne, alarmState, meterType, appearanceType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TStdDeviceMeteDetail tStdDeviceMeteDetail,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Long> upRegionIds = tStdRegionDao.selectRegionIds(tStdDeviceMeteDetail.getUpRegionId());
            if (upRegionIds.size() == 0) {
                upRegionIds.add(tStdDeviceMeteDetail.getUpRegionId());
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TStdDeviceMeteDetail> list = tStdDevicemeteService.selectByPage(tStdDeviceMeteDetail);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@Validated @RequestBody List<TStdDeviceMete> list, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            if (userId.intValue() != 10001) {
                for (TStdDeviceMete e : list) {
                    if (e.getLowLimit1() != null || e.getLowLimit2() != null || e.getLowLimit3() != null || e.getLowLimit4() != null
                            || e.getHighLimit1() != null || e.getHighLimit2() != null || e.getHighLimit3() != null || e.getHighLimit4() != null
                            || e.getAlarmState() != null) {
                        throw new JurisdictionException();
                    }
                }
            }
            result.setData(tStdDevicemeteService.batchAdd(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准设备测点批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备模版ID查询对应测点")
    @RequestMapping(value = "/selectDevMeteByModelId", method = RequestMethod.GET)
    public Result selectDevMeteByModelId(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();
        try {
            List<TStdDeviceMete> list = tStdDevicemeteService.selectDevMeteByModelId(modelId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "新增/修改设备测点")
    @RequestMapping(value = "/batchUpdateDevMete", method = RequestMethod.POST)
    public Result batchUpdateDevMete(@RequestBody List<TStdDeviceMete> list, HttpServletRequest request) {
        Result result = new Result();
        try {
            Integer userId = Integer.valueOf(request.getHeader("userId"));
            if (userId.intValue() != 10001) {
                for (TStdDeviceMete e : list) {
                    if (e.getLowLimit1() != null || e.getLowLimit2() != null || e.getLowLimit3() != null || e.getLowLimit4() != null
                            || e.getHighLimit1() != null || e.getHighLimit2() != null || e.getHighLimit3() != null || e.getHighLimit4() != null
                            || e.getAlarmState() != null) {
                        throw new JurisdictionException();
                    }
                }
            }
            result.setData(tStdDevicemeteService.batchUpdateDevMete(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增/修改设备测点失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备Id删除设备测点")
    @RequestMapping(value = "/deleteByDevId", method = RequestMethod.DELETE)
    public Result deleteByDevId(@RequestParam(value = "deviceId") Long deviceId) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.deleteByDevId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("根据设备Id删除设备测点异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("根据设备Id删除设备测点错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "设备ID与部位ID查询设备测点")
    @RequestMapping(value = "/selectByDevCus", method = RequestMethod.GET)
    public Result selectByDevCus(@RequestParam(value = "deviceId") Long deviceId, @RequestParam(value = "customType") String customType) {
        Result result = new Result();
        try {
            List<TStdDeviceMete> tStdDeviceMete = tStdDevicemeteService.selectByDevCus(deviceId, customType);
            result.setData(tStdDeviceMete);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "根据模板ID生成模板化测点信息")
    @RequestMapping(value = "/selectByModelId", method = RequestMethod.GET)
    public Result selectByModelId(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();


        return result;
    }

    @ApiOperation(value = "查询生成预定义模板测点信息表")
    @RequestMapping(value = "/selectPreDeviceMete", method = RequestMethod.GET)
    public Result selectPreDeviceMete(@RequestParam(value = "modelId") Long modelId,
                                      @RequestParam(value = "deviceId") Long deviceId,
                                      @RequestParam(value = "customType") Long customType) {

        Result result = new Result();
        try {
            List<TStdDeviceMete> tStdDeviceMete = tStdDevicemeteService.selectPreDeviceMete(modelId, deviceId, customType);
            result.setData(tStdDeviceMete);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "deviceMeteIds") String deviceMeteIds) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.batchDelete(deviceMeteIds));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("根据设备Id删除设备测点异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("根据设备Id删除设备测点错误:", e);
        }
        return result;
    }


}