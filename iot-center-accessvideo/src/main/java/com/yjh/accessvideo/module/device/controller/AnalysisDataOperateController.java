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
            int flag=analyseDataOperateService.warnSettings(meteKind, stateZero, alarmState, highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
            if(flag==1){
                switch (meteKind){
                    case "1":
                        warnLevel=alarmLevel;
                        if(analyseDataOperateService.warnJudgementTelesignaling(value,stateZero,stateOne,alarmState)==1)
                            isWarn=true;
                        break;
                    case "2":
                        int level=analyseDataOperateService.warnJudgement(Float.valueOf(value),highLimit1,lowLimit1,highLimit2,lowLimit2,highLimit3,lowLimit3,lowLimit4,lowLimit4);
                        if(level>0){
                            switch (level){
                                case 1:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "预警"));
                                    break;
                                case 2:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                    break;
                                case 3:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "严重告警"));
                                    break;
                                case 4:
                                    warnLevel=Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "危急告警"));
                                    break;
                            }
                        }
                        break;
                }


            }
            Map<String,Object> resultMap=new HashMap<>();
            resultMap.put("isWarn",isWarn);
            resultMap.put("warnLevel",warnLevel);
            result.setData(resultMap);

        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
        }

        return result;
    }
}
