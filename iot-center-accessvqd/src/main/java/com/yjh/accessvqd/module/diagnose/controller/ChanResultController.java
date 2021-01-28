package com.yjh.accessvqd.module.diagnose.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.accessvqd.commons.result.BusinessException;
import com.yjh.accessvqd.commons.result.Result;
import com.yjh.accessvqd.commons.result.ResultCodeEnum;
import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.yjh.accessvqd.module.diagnose.entity.DiagnoseResultDetail;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author czh
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/chanResultOperate/v1")
@Api(value = "/chanResultOperate", description = "诊断结果操作")
public class ChanResultController {
    private Logger log = LoggerFactory.getLogger(ChanResultController.class);
    @Autowired
    private final ChanResultService chanResultService;

    public ChanResultController(ChanResultService chanResultService) {
        this.chanResultService = chanResultService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody ChanResult chanResult) {
        Result result = new Result();
        try {
            result.setData(chanResultService.insert(chanResult));
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
    public Result delete(@RequestParam(value = "diagnoseResultId", required = true) Long diagnoseResultId) {
        Result result = new Result();
        try {
            result.setData(chanResultService.deleteByPrimaryId(diagnoseResultId));
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
    public Result update(@RequestBody ChanResult chanResult) {
        Result result = new Result();
        try {
            result.setData(chanResultService.update(chanResult));
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
    public Result selectByPrimaryId(@RequestParam(value = "diagnoseResultId", required = true) Long diagnoseResultId) {
        Result result = new Result();
        try {
            ChanResult chanResult = chanResultService.selectByPrimaryId(diagnoseResultId);
            result.setData(chanResult);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

//    @ApiOperation(value = "查询")
//    @RequestMapping(value = "/select", method = RequestMethod.GET)
//    public Result select(@RequestParam(value = "diagnoseResultId", required = false) Long diagnoseResultId,
//                         @RequestParam(value = "channelId", required = false) String channelId,
//                         @RequestParam(value = "ip", required = false) String ip,
//                         @RequestParam(value = "chanIndex", required = false) String chanIndex,
//                         @RequestParam(value = "checkTime", required = false) String checkTime,
//                         @RequestParam(value = "channelResult", required = false) Integer channelResult,
//                         @RequestParam(value = "signalResult", required = false) Integer signalResult,
//                         @RequestParam(value = "blurResult", required = false) Integer blurResult,
//                         @RequestParam(value = "contrastResult", required = false) Integer contrastResult,
//                         @RequestParam(value = "brightResult", required = false) Integer brightResult,
//                         @RequestParam(value = "darkResult", required = false) Integer darkResult,
//                         @RequestParam(value = "chromaResult", required = false) Integer chromaResult,
//                         @RequestParam(value = "monoResult", required = false) Integer monoResult,
//                         @RequestParam(value = "noiseResult", required = false) Integer noiseResult,
//                         @RequestParam(value = "streakResult", required = false) Integer streakResult,
//                         @RequestParam(value = "freezeResult", required = false) Integer freezeResult,
//                         @RequestParam(value = "shakeResult", required = false) Integer shakeResult,
//                         @RequestParam(value = "flashResult", required = false) Integer flashResult,
//                         @RequestParam(value = "sceneResult", required = false) Integer sceneResult,
//                         @RequestParam(value = "coverResult", required = false) Integer coverResult,
//                         @RequestParam(value = "ptzResult", required = false) Integer ptzResult,
//                         @RequestParam(value = "snapshotUlt", required = false) String snapshotUlt,
//                         @RequestParam(value = "resultContent", required = false) String resultContent) {
//        Result result = new Result();
//        try {
//            List<ChanResult> list = chanResultService.select(diagnoseResultId, channelId, ip, chanIndex, checkTime, channelResult, signalResult, blurResult, contrastResult, brightResult, darkResult, chromaResult, monoResult, noiseResult, streakResult, freezeResult, shakeResult, flashResult, sceneResult, coverResult, ptzResult, snapshotUlt, resultContent);
//            result.setData(list);
//        } catch (Exception e) {
//            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
//            log.error("失败描述：", e);
//        }
//        return result;
//    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody ChanResult chanResult,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String,Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<ChanResult> list = chanResultService.selectByPage(chanResult);
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
    public Result batchInsert(@RequestBody List<ChanResult> list) {
        Result result = new Result();
        try {
            result.setData(chanResultService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "获取任务信息List")
    @RequestMapping(value = "/findQueryPlan",method = RequestMethod.GET)
    public Result findQueryPlan(){
        Result result=new Result();
        try {
            result.setData(chanResultService.findQueryItems(0));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "获取监测点信息List")
    @RequestMapping(value = "/findQueryChannel",method = RequestMethod.GET)
    public Result findQueryChannel(){
        Result result=new Result();
        try {
            result.setData(chanResultService.findQueryItems(1));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "分页条件查询诊断结果详细信息")
    @RequestMapping(value = "/selectDiagnoseResultByPage", method = RequestMethod.GET)
    public Result selectDiagnoseResultByPage(@RequestParam(value = "planName",required = false)String planName,
                                             @RequestParam(value = "channelName",required = false)String channelName,
                                             @RequestParam(value = "startTime",required = false)Date startTime,
                                             @RequestParam(value = "endTime",required = false)Date endTime,
                                             @RequestParam(value = "diagnosePlanId",required = false)String diagnosePlanId,
                                             @RequestParam(value = "status",required = false)String status,
                                             @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                             @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String,Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<DiagnoseResultDetail> list = chanResultService.selectDiagnoseResultByPage(planName, channelName, startTime,endTime,diagnosePlanId,status);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }



    @ApiOperation(value = "故障点数统计")
    @RequestMapping(value = "/faultCount", method = RequestMethod.GET)
    public Result faultCount(@RequestParam(value = "planName",required = false)String planName,
                             @RequestParam(value = "channelName",required = false)String channelName) {
        Result result = new Result();
        try {
            result.setData(chanResultService.faultCounts(planName, channelName));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "监测点状态点数统计")
    @RequestMapping(value = "/channelStatusCount", method = RequestMethod.GET)
    public Result channelStatusCount(@RequestParam(value = "planName",required = false)String planName,
                                     @RequestParam(value = "channelName",required = false)String channelName) {
        Result result = new Result();
        try {
            result.setData(chanResultService.statusTypeChannel(planName, channelName));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "故障信息树")
    @RequestMapping(value = "/faultInfoTree", method = RequestMethod.GET)
    public Result faultInfoTree() {
        Result result = new Result();
        try {
            result.setData(chanResultService.faultTree());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "统计分析")
    @RequestMapping(value = "/staticalAnalysis", method = RequestMethod.POST)
    public Result staticalAnalysis(@RequestBody List<Map<String,String>> checkedList,
                                   @RequestParam(value = "startTime",required = false)String startTime,
                                   @RequestParam(value = "endTime",required = false)String endTime) {
        Result result = new Result();
        try {
            result.setData(chanResultService.staticalAnalysis(checkedList, startTime, endTime));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败：" + e);
        }
        return result;
    }


}
