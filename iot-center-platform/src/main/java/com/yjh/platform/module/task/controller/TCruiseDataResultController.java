package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.task.entity.CruiseResultAnalInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalMeteInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeInfo;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.task.service.TCruiseDataResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCruiseDataResult/v1")
@Api(value = "/tCruiseDataResult", description = "巡检点数据表操作接口")
public class TCruiseDataResultController {

    @Autowired
    private final TCruiseDataResultService tCruiseDataResultService;

    private Logger log = LoggerFactory.getLogger(TCruiseDataResultController.class);

    @Autowired
    private TStdDeviceService tStdDeviceService;

    public TCruiseDataResultController(TCruiseDataResultService tCruiseDataResultService) {
        this.tCruiseDataResultService = tCruiseDataResultService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCruiseDataResult tCruiseDataResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.insert(tCruiseDataResult));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "cruiseDataId", required = true) Long cruiseDataId) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.deleteByPrimaryId(cruiseDataId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCruiseDataResult tCruiseDataResult) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.update(tCruiseDataResult));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "cruiseDataId", required = true) Long cruiseDataId) {
        Result result = new Result();
        try {
            TCruiseDataResult tCruiseDataResult = tCruiseDataResultService.selectByPrimaryId(cruiseDataId);
            result.setData(tCruiseDataResult);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "cruiseDataId", required = false) Long cruiseDataId,
                         @RequestParam(value = "cruiseResultId", required = false) String cruiseResultId,
                         @RequestParam(value = "cruiseId", required = false) Long cruiseId,
                         @RequestParam(value = "cruiseName", required = false) String cruiseName,
                         @RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                         @RequestParam(value = "cruiseAbnormal", required = false) Integer cruiseAbnormal,
                         @RequestParam(value = "resultDesc", required = false) String resultDesc,
                         @RequestParam(value = "resultNum", required = false) String resultNum,
                         @RequestParam(value = "modifyNum", required = false) String modifyNum,
                         @RequestParam(value = "picpath", required = false) String picpath,
                         @RequestParam(value = "personCheck", required = false) String personCheck,
                         @RequestParam(value = "origpic", required = false) String origpic,
                         @RequestParam(value = "evaluationState", required = false) String evaluationState,
                         @RequestParam(value = "identifyState", required = false) Integer identifyState,
                         @RequestParam(value = "identifyResult", required = false) Integer identifyResult,
                         @RequestParam(value = "createtime", required = false) Date createtime,
                         @RequestParam(value = "remark", required = false) String remark,
                         @RequestParam(value = "checkUser", required = false) String checkUser,
                         @RequestParam(value = "checkDate", required = false) Date checkDate,
                         @RequestParam(value = "isWarn", required = false) Integer isWarn,
                         @RequestParam(value = "cruiseResult", required = false) Integer cruiseResult) {

        Result result = new Result();
        try {
            List<TCruiseDataResult> list = tCruiseDataResultService.select(cruiseDataId, cruiseResultId, cruiseId, cruiseName, cruiseType, cruiseAbnormal, resultDesc, resultNum, modifyNum, picpath,
                    personCheck, origpic, evaluationState, identifyState, identifyResult, createtime, remark,
                    checkUser, checkDate, isWarn, cruiseResult);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruiseDataResult tCruiseDataResult,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TCruiseDataResult> list = tCruiseDataResultService.selectByPage(tCruiseDataResult);
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
    public Result batchInsert(@RequestBody List<TCruiseDataResult> list) {
        Result result = new Result();
        try {
            result.setData(tCruiseDataResultService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "巡视结果分析--测点查询")
    @RequestMapping(value = "/selectCruiseResultAnal", method = RequestMethod.GET)
    public Result selectCruiseResultAnal(@RequestParam(value = "deviceId", required = false) Long deviceId,
                                         @RequestParam(value = "resultSwitch") String resultSwitch,
                                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "6") int pageSize) {
        Result result = new Result();
        List<Long> regionIds = new ArrayList<>();
        List<Long> deviceIds = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(Objects.nonNull(deviceId)){
                if (Objects.nonNull(tStdDeviceService.selectByPrimaryId(deviceId))) {
                    deviceIds.add(deviceId);
                } else {
                    regionIds = tStdDeviceService.selectRegionIdTree(deviceId);
                    deviceIds = tStdDeviceService.selectDeviceIdsByRegion(regionIds);
                }
              log.info("regionIds-----------:"+regionIds);
            }else {
                deviceIds.add(Long.valueOf(996));
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos = tCruiseDataResultService.selectCruiseResultAnal(deviceIds, resultSwitch);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", cruiseResultAnalMeteInfos);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视结果分析--测点查询2")
    @RequestMapping(value = "/selectCruiseResultAnalyze", method = RequestMethod.GET)
    public Result selectCruiseResultAnalyze(@RequestParam(value = "regionId", required = false) Long regionId,
                                         @RequestParam(value = "deviceType", required = false) Integer deviceType,
                                         @RequestParam(value = "meteType", required = false) String meteType,
                                         @RequestParam(value = "meterType", required = false) Integer meterType,
                                         @RequestParam(value = "cruiseRes", required = false) Integer cruiseRes,
                                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "6") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
//            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            resultMap = tCruiseDataResultService.selectCruiseResultAnalyze(regionId,deviceType,meteType,meterType,cruiseRes,pageNum,pageSize);

//            List<CruiseResultAnalMeteInfo> list = tCruiseDataResultService.selectCruiseResultAnalyze(regionId,deviceType,meteType,meterType,cruiseRes);
//            resultMap.put("count", page.getTotal());
//            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }

        return result;
    }
    @ApiOperation(value = "巡视报表")
    @RequestMapping(value = "/selectCruiseDataReport", method = RequestMethod.GET)
    public Result selectCruiseDataReport(@RequestParam(value = "cType", required = false) Integer cType,
                                        @RequestParam(value = "meteType", required = false) String meteType,
                                        @RequestParam(value = "meterType", required = false) Integer meterType,
                                        @RequestParam(value = "regionId", required = false) Long regionId,
                                        @RequestParam(value = "instanceName", required = false) String instanceName,
                                        @RequestParam(value = "endTime", required = false) String endTime,
                                        @RequestParam(value = "startTime", required = false) String startTime,
                                        @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                        @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
            resultMap = (tCruiseDataResultService.selectCruiseDataReport(cType,meteType,meterType,endTime, startTime,regionId,instanceName,pageNum,pageSize));
            result.setData(resultMap);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视结果分析--巡检点结果列表")
    @RequestMapping(value = "/selectCruiseDataResultByList2", method = RequestMethod.GET)
    public Result selectCruiseDataResultByList2(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                               @RequestParam(value = "cType", required = false) Integer cType,
                                               @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                                                @RequestParam(value = "meteType", required = false) String meteType,
                                                @RequestParam(value = "meterType", required = false) Integer meterType,
                                                @RequestParam(value = "endTime", required = false) String endTime,
                                               @RequestParam(value = "startTime", required = false) String startTime,
                                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
            List<CruiseResultAnalyzeInfo>  list = tCruiseDataResultService.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId, meteType,meterType,endTime, startTime,pageNum,pageSize);
//            resultMap = tCruiseDataResultService.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId, meteType,meterType,endTime, startTime,pageNum,pageSize);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视结果分析--巡检点结果列表")
    @RequestMapping(value = "/selectCruiseDataResultByList", method = RequestMethod.GET)
    public Result selectCruiseDataResultByList(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                               @RequestParam(value = "cType", required = false) Integer cType,
                                               @RequestParam(value = "deviceMeteId") Long deviceMeteId,
                                               @RequestParam(value = "endDate", required = false) String endDate,
                                               @RequestParam(value = "startDate", required = false) String startDate,
                                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "4") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date endDateTemp;
        Date startDateTemp;
        try {
            if (endDate.equals("") && startDate.equals("")) {
                endDateTemp = null;
                startDateTemp = null;
            } else {
                endDateTemp = dateFormat.parse(endDate);
                startDateTemp = dateFormat.parse(startDate);
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<CruiseResultAnalInfo> list = (tCruiseDataResultService.selectCruiseDataResultByList(cruiseType, cType, deviceMeteId, endDateTemp, startDateTemp));
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "获取折线图元素信息")
    @RequestMapping(value = "/selectBrokenLine", method = RequestMethod.GET)
    public Result selectBrokenLine(@RequestParam(value = "cruiseType", required = false) Integer cruiseType,
                                   @RequestParam(value = "cType", required = false) Integer cType,
                                   @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                                   @RequestParam(value = "meteType", required = false) String meteType,
                                   @RequestParam(value = "meterType", required = false) Integer meterType,
                                   @RequestParam(value = "endTime", required = false) String endTime,
                                   @RequestParam(value = "startTime", required = false) String startTime) {
        Result result = new Result();
        try {
            if (Objects.isNull(startTime) || "".equals(startTime)){
                startTime = null;
            }
            if (Objects.isNull(endTime) || "".equals(endTime)){
                endTime = null;
            }
            result.setData(tCruiseDataResultService.selectBrokenLine(cruiseType, cType, deviceMeteId, startTime, endTime,meteType,meterType));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
}
