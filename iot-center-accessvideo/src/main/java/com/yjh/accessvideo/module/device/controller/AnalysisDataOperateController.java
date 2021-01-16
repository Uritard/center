package com.yjh.accessvideo.module.device.controller;


import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author czh
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/AnalysisDataOperate/v1")
@Api(value = "/AnalysisDataOperate", description = "算法结果处理操作接口")
public class AnalysisDataOperateController {
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;

    private Logger log= LoggerFactory.getLogger(AnalysisDataOperateController.class);

    @ApiOperation(value = "告警判断处理接口")
    @RequestMapping(value = "/warnInfo",method = RequestMethod.GET)
    public Result warnInfo(@RequestParam String value,
                           @RequestParam String stdDeviceMeteName,
                           @RequestParam String meteKind,
                           @RequestParam Integer alarmState,
                           @RequestParam String stateZero,
                           @RequestParam String stateOne,
                           @RequestParam Integer alarmLevel,
                           @RequestParam Float highLimit1,
                           @RequestParam Float lowLimit1,
                           @RequestParam Float highLimit2,
                           @RequestParam Float lowLimit2,
                           @RequestParam Float highLimit3,
                           @RequestParam Float lowLimit3,
                           @RequestParam Float highLimit4,
                           @RequestParam Float lowLimit4){


        Result result=new Result();
        try{
            Boolean isWarn=false;//是否告警
            Integer warnLevel=0;//告警级别
            String warnName=null;//告警名称
            String warnContent=null;//告警内容
            String outRange=null;//超越浮动值
            Date warnTime=null;//告警时间
            int flag=analyseDataOperateService.warnSettings(meteKind, stateZero, alarmState, highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
            if(flag==1){
                switch (meteKind){
                    case "1":
                        warnLevel=alarmLevel;
                        if(analyseDataOperateService.warnJudgementTelesignaling(value,stateZero,stateOne,alarmState)==1){
                            isWarn=true;
                            warnName=stdDeviceMeteName+"状态异常";
                            switch (alarmState){
                                case 0:
                                    warnContent="主设备告警"+":"+stdDeviceMeteName+"-"+stateZero+"状态告警";
                                    break;
                                case 1:
                                    warnContent="主设备告警"+":"+stdDeviceMeteName+"-"+stateOne+"状态告警";
                            }
                            warnTime=new Date();
                        }
                        break;
                    case "2":

                        int level=analyseDataOperateService.warnJudgement(Float.valueOf(value),highLimit1,lowLimit1,highLimit2,lowLimit2,highLimit3,lowLimit3,highLimit4,lowLimit4);
                        log.info("level-------------:"+level);
                        if(level>0){
                            isWarn=true;
                            warnName=stdDeviceMeteName+"数据异常";
                            warnTime=new Date();
                            Float resultValueMeter=Float.valueOf(value);
                            switch (level){
                                case 1:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "预警"));
                                    warnContent="主设备告警"+":"+stdDeviceMeteName+"-"+"预警";
                                    if (resultValueMeter >= highLimit1) {
                                        outRange=String.valueOf(resultValueMeter-highLimit1);
                                    } else {
                                        outRange=String.valueOf(lowLimit1-resultValueMeter);
                                    }
                                    break;
                                case 2:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                    warnContent="主设备告警"+":"+stdDeviceMeteName+"-"+"一般告警";
                                    if (resultValueMeter >= highLimit2) {
                                        outRange=String.valueOf(resultValueMeter-highLimit2);
                                    } else {
                                        outRange=String.valueOf(lowLimit2-resultValueMeter);
                                    }
                                    break;
                                case 3:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "严重告警"));
                                    warnContent="主设备告警"+":"+stdDeviceMeteName+"-"+"严重告警";
                                    if (resultValueMeter >= highLimit3) {
                                        outRange=String.valueOf(resultValueMeter-highLimit3);
                                    } else {
                                        outRange=String.valueOf(lowLimit3-resultValueMeter);
                                    }
                                    break;
                                case 4:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "危急告警"));
                                    warnContent="主设备告警"+":"+stdDeviceMeteName+"-"+"危急告警";
                                    if (resultValueMeter >= highLimit4) {
                                        outRange=String.valueOf(resultValueMeter-highLimit4);
                                    } else {
                                        outRange=String.valueOf(lowLimit4-resultValueMeter);
                                    }
                                    break;
                            }
                        }
                        break;
                }


            }
            Map<String,Object> resultMap=new HashMap<>();
            resultMap.put("isWarn",isWarn);
            resultMap.put("warnLevel",warnLevel);
            resultMap.put("warnName",warnName);
            resultMap.put("warnContent",warnContent);
            resultMap.put("outRange",outRange);
            resultMap.put("warnTime",warnTime);
            result.setData(resultMap);

        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
        }

        return result;
    }
}
