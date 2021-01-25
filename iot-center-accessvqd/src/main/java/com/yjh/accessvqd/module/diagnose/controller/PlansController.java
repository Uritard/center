package com.yjh.accessvqd.module.diagnose.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.accessvqd.commons.result.Result;
import com.yjh.accessvqd.commons.result.ResultCodeEnum;
import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.yjh.accessvqd.module.diagnose.entity.Plans;
import com.yjh.accessvqd.module.diagnose.service.PlansService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author czh
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/plansOperate/v1")
@Api(value = "/plansOperate", description = "诊断任务操作")
public class PlansController {
    private Logger log = LoggerFactory.getLogger(PlansController.class);

    @Autowired
    private PlansService plansService;

    @Autowired
    private RedisTemplate redisTemplate;



    @ApiOperation(value = "查询任务")
    @RequestMapping(value = "/diagnosePlanInfo", method = RequestMethod.GET)
    public Result diagnosePlanInfo(@RequestParam(value = "planName",required = false) String planName,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String,Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<Plans> list = plansService.getPlanList(planName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败" + e);
        }
        return result;
    }

    @ApiOperation(value = "诊断任务新增/修改--下发")
    @RequestMapping(value = "/diagnosePlanExecute", method = RequestMethod.PUT)
    public Result diagnosePlanExecute(@RequestBody Plans plans) {
        Result result = new Result();
        try {
            result.setData(plansService.diagnosePlanUpAdd(plans));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务下发失败" + e);
        }
        return result;
    }

    @ApiOperation(value = "任务删除")
    @RequestMapping(value = "/diagnosePlanDelete", method = RequestMethod.DELETE)
    public Result diagnosePlanDelete(@RequestParam List<String> planIds) {
        Result result = new Result();
        try {
            result.setData(plansService.diagnosePlanDelete(planIds));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除失败" + e);
        }
        return result;
    }


    @ApiOperation(value = "接口测试")
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Result test(@RequestParam String time) {
        Result result = new Result();
        try {
            result.setData("---");
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务下发失败" + e);
        }
        return result;
    }

}
