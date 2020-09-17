package com.yjh.platform.module.task.controller;

import com.google.gson.internal.$Gson$Preconditions;
import com.yjh.platform.module.task.entity.BrokenLineInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalMeteInfo;
import com.yjh.platform.module.task.service.TCruiseDataResultService;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
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
                            @RequestParam(value = "pointType", required = false) Integer pointType,
                            @RequestParam(value = "resultDesc", required = false) String resultDesc,
                            @RequestParam(value = "resultNum", required = false) String resultNum,
                            @RequestParam(value = "modifyNum", required = false) String modifyNum,
                            @RequestParam(value = "picpath", required = false) String picpath,
                            @RequestParam(value = "personcheck", required = false) String personcheck,
                            @RequestParam(value = "origpic", required = false) String origpic,
                            @RequestParam(value = "state", required = false) Integer state,
                            @RequestParam(value = "evaluationState", required = false) String evaluationState,
                            @RequestParam(value = "identifyState", required = false) Integer identifyState,
                            @RequestParam(value = "identifyResult", required = false) Integer identifyResult,
                            @RequestParam(value = "createtime", required = false) Date createtime,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TCruiseDataResult> list = tCruiseDataResultService.select(cruiseDataId, cruiseResultId, cruiseId, pointType, resultDesc, resultNum, modifyNum, picpath, personcheck, origpic, state, evaluationState, identifyState, identifyResult, createtime, remark);
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
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
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


    @ApiOperation(value = "全量标准测点及第一条巡检结果查询-分页无条件")
    @RequestMapping(value = "/selectCruiseResultAnalAll",method =RequestMethod.GET)
    public Result selectCruiseResultAnalAll(@RequestParam(value = "pageNum" ,required = false,defaultValue = "1")int pageNum,
                                            @RequestParam(value = "pageSize",required = false,defaultValue = "6")int pageSize){
        Result result=new Result();
        Map<String,Object>resultMap=new HashMap<>();
        try {
            Page page=PageHelper.startPage(pageNum, pageSize);
            List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos=tCruiseDataResultService.selectCruiseResultAnalAll();
            resultMap.put("count",page.getTotal());
            resultMap.put("list",cruiseResultAnalMeteInfos);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视结果分析--测点查询")
    @RequestMapping(value = "/selectCruiseResultAnal",method = RequestMethod.GET)
    public Result selectCruiseResultAnal(@RequestParam Long deviceId,
                                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "6") int pageSize){
        Result result=new Result();
        Map<String,Object> resultMap=new HashMap<>();
        try {
            Page page=PageHelper.startPage(pageNum, pageSize);
            List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos=tCruiseDataResultService.selectCruiseResultAnal(deviceId);
            resultMap.put("count",page.getTotal());
            resultMap.put("list",cruiseResultAnalMeteInfos);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视结果分析--巡检点结果列表")
    @RequestMapping(value = "/selectCruiseDataResultByList",method = RequestMethod.POST)
    public Result selectCruiseDataResultByList(@RequestBody CruiseResultAnalInfo cruiseResultAnalInfo,
                                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "4") int pageSize){
        Result result=new Result();
        Map<String,Object> resultMap=new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<CruiseResultAnalInfo> list=(tCruiseDataResultService.selectCruiseDataResultByList(cruiseResultAnalInfo));
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
    @RequestMapping(value = "/selectBrokenLine",method = RequestMethod.POST)
    public Result selectBrokenLine(@RequestBody BrokenLineInfo brokenLineInfo){
        Result result=new Result();
        try {
            result.setData(tCruiseDataResultService.selectBrokenLine(brokenLineInfo));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
}
