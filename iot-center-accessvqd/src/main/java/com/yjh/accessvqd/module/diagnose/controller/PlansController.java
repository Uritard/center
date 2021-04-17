package com.yjh.accessvqd.module.diagnose.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.accessvqd.commons.result.Result;
import com.yjh.accessvqd.commons.result.ResultCodeEnum;
import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.yjh.accessvqd.module.diagnose.entity.Plans;
import com.yjh.accessvqd.module.diagnose.service.PlansService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;


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
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize,
                                   HttpServletRequest request ) {
        Result result = new Result();
        Map<String,Object> resultMap = new HashMap<>();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }
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

    @ApiOperation(value = "诊断任务Id查询")
    @RequestMapping(value = "/selectByPlanId", method = RequestMethod.GET)
    public Result selectByPlanId(@RequestParam(value = "diagnosePlanId")String diagnosePlanId,
                                 HttpServletRequest request ) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }
            result.setData(plansService.selectByPlanId(diagnosePlanId));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败" + e);
        }
        return result;
    }

    @ApiOperation(value = "诊断任务新增/修改--下发")
    @RequestMapping(value = "/diagnosePlanExecute", method = RequestMethod.POST)
    public Result diagnosePlanExecute(HttpServletRequest request, @RequestBody Plans plans) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }
            plans.setUserId(Long.valueOf(request.getHeader("userId").toString()));
            String status=plansService.diagnosePlanUpAdd(plans);
            if(status.equals("failed")){
                result.setMessage(3030,"任务下发失败");
            }else if(status.equals("success")){
                result.setData(status);
            }else {
                result.setMessage(800,status);
            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务下发失败" + e);
        }
        return result;
    }

    @ApiOperation(value = "任务删除")
    @RequestMapping(value = "/diagnosePlanDelete", method = RequestMethod.DELETE)
    public Result diagnosePlanDelete(@RequestParam List<String> planIds,HttpServletRequest request ) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }
            result.setData(plansService.diagnosePlanDelete(planIds));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除失败" + e);
        }
        return result;
    }


    @ApiModelProperty(value = "最后一次任务单")
    @RequestMapping(value = "/theLastPlanInfo",method = RequestMethod.GET)
    public Result theLastPlanInfo(HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }
           result.setData(plansService.selectTheLastPlan());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败" + e);
        }
        return result;
    }

    @ApiOperation(value = "接口测试")
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Result test(@RequestParam String time,HttpServletRequest request ) {

        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }
            result.setData(RandomStringUtils.randomAlphanumeric(15));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务下发失败" + e);
        }
        return result;
    }


    @ApiOperation(value = "查询NVR-channel树")
    @RequestMapping(value = "/selectNVRChannelTree",method = RequestMethod.GET)
    public Result selectNVRChannelTree(@RequestParam(value = "diagnosePlanId",required = false)String diagnosePlanId,HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }
            result.setData(plansService.selectNVRChannelTree(diagnosePlanId));
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("树查询失败：", e);
        }
        return result;
    }
}
