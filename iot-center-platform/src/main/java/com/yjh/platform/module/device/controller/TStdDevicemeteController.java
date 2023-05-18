package com.yjh.platform.module.device.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.ModelExcelListener;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.ExcelEntity;
import com.yjh.platform.module.device.entity.LockPresetCommand;
import com.yjh.platform.module.device.entity.TStdDeviceMeteDetail;
import com.yjh.platform.module.device.service.TStdDevicemeteService;
import com.yjh.platform.module.device.entity.TStdDeviceMete;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
    @Resource
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TStdDevicemeteController.class);

    public TStdDevicemeteController(TStdDevicemeteService tStdDevicemeteService) {
        this.tStdDevicemeteService = tStdDevicemeteService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增标准设备测点",content = "根据用户传递的参数新增标准设备测点",logType = 2,authority = "1234")
    public Result add(@Validated @RequestBody TStdDeviceMeteDetail tStdDeviceMeteDetail, HttpServletRequest request)  {
        Result result = new Result();
        try {
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
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除标准设备测点",content = "根据用户传递的参数删除标准设备测点",logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "deviceMeteId") Long deviceMeteId) {
        Result result = new Result();
        try {
            // 当前测点配置巡视点 ？ 不允许删除 ： 删除逻辑
            List<Long> haveList = tStdDevicemeteDao.selectHave(deviceMeteId);
            if (CollectionUtils.isNotEmpty(haveList)){
                result.setMessage(209,"测点已配置为巡视点,操作无法生效");
            }else {
                result.setData(tStdDevicemeteService.deleteByPrimaryId(deviceMeteId));
            }
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改标准设备测点",content = "根据用户传递的参数修改标准设备测点",logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody TStdDeviceMeteDetail tStdDeviceMeteDetail, HttpServletRequest request) {
        Result result = new Result();
        try {
            int i = tStdDevicemeteService.update(tStdDeviceMeteDetail);
            if (i==0) {
                result.setMessage("AI判别不能和识别算法或者AI缺陷同时选择！");
                result.setCode(10102);
            } else { result.setData(i); }
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
    @Logs(title = "查询标准设备测点",content = "根据用户传递的参数查询标准设备测点",logType = 1)
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
    @Logs(title = "查询标准设备测点",content = "根据用户传递的参数查询标准设备测点",logType = 1)
    public Result select(@RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                         @RequestParam(value = "deviceId", required = false) Long deviceId,
                         @RequestParam(value = "devicePointId", required = false) String devicePointId,
                         @RequestParam(value = "customId", required = false) String customId,
                         @RequestParam(value = "meteId", required = false) Long meteId,
                         @RequestParam(value = "meteKind", required = false) String meteKind,
                         @RequestParam(value = "meteType", required = false) String meteType,
                         @RequestParam(value = "meteName", required = false) String meteName,
                         @RequestParam(value = "deviceType", required = false) Integer deviceType,
                         @RequestParam(value = "inspectionType", required = false) Integer inspectionType,
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
            List<TStdDeviceMete> list = tStdDevicemeteService.select(deviceMeteId, deviceId, devicePointId, customId, meteId, meteKind, meteType, meteName, deviceType, inspectionType, positionType, analyseType, unit, alarmNote, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark, stateZero, stateOne, alarmState, meterType, appearanceType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询标准设备测点",content = "根据用户传递的参数查询标准设备测点",logType = 1,authority = "1234,1235")
    public Result selectByPage(@RequestBody TStdDeviceMeteDetail tStdDeviceMeteDetail
                              ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        long start = System.currentTimeMillis();
        try {
//            List<Long> upRegionIds = tStdRegionDao.selectRegionIds(tStdDeviceMeteDetail.getUpRegionId());
//            if (upRegionIds.size() == 0) {
//                upRegionIds.add(tStdDeviceMeteDetail.getUpRegionId());
//            }
//            Page page = PageHelper.startPage(tStdDeviceMeteDetail.getPageNum()!=null?tStdDeviceMeteDetail.getPageNum():1, tStdDeviceMeteDetail.getPageSize()!=null?tStdDeviceMeteDetail.getPageSize():0, true, null, true);
//            List<Long> listForPage=tStdDevicemeteService.selectForPage(tStdDeviceMeteDetail);
//            List<TStdDeviceMeteDetail> list = new ArrayList<>();
//            if(listForPage != null &&listForPage.size()>0){
//                list = tStdDevicemeteService.selectByPage(tStdDeviceMeteDetail,listForPage);
//            }
//            resultMap.put("count", page.getTotal());
//            resultMap.put("list", list);
//            result.setData(resultMap);
            result = tStdDevicemeteService.selectByPage(tStdDeviceMeteDetail,new ArrayList<>());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("标准设备测点分页查询失败描述：", e);
        }
        long end = System.currentTimeMillis();
        log.info("时间3："+(end-start)/1000);
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入标准设备测点",content = "根据用户传递的参数批量插入标准设备测点",logType = 2)
    public Result batchAdd(@RequestBody List<TStdDeviceMete> list, HttpServletRequest request) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.batchAdd(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标准设备测点批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备模版ID查询对应测点")
    @RequestMapping(value = "/selectDevMeteByModelId", method = RequestMethod.GET)
    @Logs(title = "查询标准设备测点",content = "根据设备模板查询对应测点",logType = 1)
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
    @Logs(title = "修改标准设备测点",content = "根据用户传递的参数新增标准设备测点",logType = 2)
    public Result batchUpdateDevMete(@RequestBody List<TStdDeviceMete> list, HttpServletRequest request) {
        Result result = new Result();
        try {
            result.setData(tStdDevicemeteService.batchUpdateDevMete(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增/修改设备测点失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据设备Id删除设备测点")
    @RequestMapping(value = "/deleteByDevId", method = RequestMethod.POST)
    @Logs(title = "删除标准设备测点",content = "根据用户传递的参数删除标准设备测点",logType = 4)
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
    @Logs(title = "查询标准设备测点",content = "根据设备与部位查询测点标准设备测点",logType = 1)
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
    @Logs(title = "根据模板ID生成模板化测点信息",content = "根据模板ID生成模板化测点信息",logType = 5)
    public Result selectByModelId(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();


        return result;
    }

    @ApiOperation(value = "查询生成预定义模板测点信息表")
    @RequestMapping(value = "/selectPreDeviceMete", method = RequestMethod.GET)
    @Logs(title = "查询生成预定义模板测点信息表",content = "查询与预定义模板测点信息",logType = 1)
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
    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST)
    @Logs(title = "批量删除设备测点",content = "根据用户传递的参数批量删除设备测点",logType = 4)
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

    @ApiOperation(value = "excel导入测点")
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @Logs(title = "excel导入测点",content = "excel导入测点",logType = 1)
    public Result importExcel(MultipartFile file) {
        Result result = new Result();
        try {
            InputStream inputStream = file.getInputStream();
            ModelExcelListener modelExcelListener = new ModelExcelListener();
            ReadSheet readSheet = new ReadSheet(0);
            EasyExcelFactory.read(inputStream, ExcelEntity.class,modelExcelListener).headRowNumber(1).build().read(readSheet);
            List<ExcelEntity> excelEntities = modelExcelListener.getExcelEntities();

            if (CollectionUtils.isNotEmpty(excelEntities)) {
                tStdDevicemeteService.importExcel(excelEntities);
            }
        }catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("发生异常:", e);
        }
        return result;
    }
    @ApiOperation(value = "测点预置位查询")
    @RequestMapping(value = "/queryPresetByCameraAndMete", method = RequestMethod.GET)
    @Logs(title = "excel导入测点",content = "excel导入测点",logType = 1)
    public Result queryPresetByCameraAndMete(@RequestParam("cameraId")Long cameraId,@RequestParam("meteId")Long meteId) {
        Result result = new Result();
        try {
            tStdDevicemeteService.queryPresetByCameraAndMete(cameraId,meteId);
        }catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("发生异常:", e);
        }
        return result;
    }
       @ApiOperation(value = "测点预置位自锁定")
    @RequestMapping(value = "/linkOrEditPreset", method = RequestMethod.GET)
    @Logs(title = "excel导入测点",content = "excel导入测点",logType = 1)
    public Result linkOrEditPreset(@RequestBody LockPresetCommand lockPresetCommand) {
        Result result = new Result();
        try {
            result = tStdDevicemeteService.linkOrEditPreset(lockPresetCommand);
        }catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("发生异常:", e);
        }
        return result;
    }

}
