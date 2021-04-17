package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TCfgUnionRule;
import com.yjh.platform.module.task.entity.TCfgUnionRuleDetail;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.entity.TCruisePlanCount;
import com.yjh.platform.module.task.service.TCfgUnionRuleService;
import com.yjh.platform.module.task.service.TCruisePlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lqh
 * @since 2020-09-18
 */
@RestController
@RequestMapping("/tCfgUnionRule/v1")
@Api(value = "/tCfgUnionRule", description = "联动规则表操作接口")
public class TCfgUnionRuleController {

    @Autowired
    private final TCfgUnionRuleService tCfgUnionRuleService;

    @Autowired
    private TCruisePlanService tCruisePlanService;

    private Logger log = LoggerFactory.getLogger(TCfgUnionRuleController.class);

    public TCfgUnionRuleController(TCfgUnionRuleService tCfgUnionRuleService) {
        this.tCfgUnionRuleService = tCfgUnionRuleService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增联动规则数据",content = "根据用户传递的参数新增联动规则数据",logType = 2,authority = "1234")
    public Result add(@Validated @RequestBody TCfgUnionRule tCfgUnionRule) {
        Result result = new Result();
        try {
            result.setData(tCfgUnionRuleService.add(tCfgUnionRule));
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
    @Logs(title = "删除联动规则数据",content = "根据用户传递的参数删除联动规则数据",logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "ruleId", required = true) Long ruleId) {
        Result result = new Result();
        try {
            result.setData(tCfgUnionRuleService.deleteByPrimaryId(ruleId));
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
    @Logs(title = "修改联动规则数据",content = "根据用户传递的参数修改联动规则数据",logType = 3,authority = "1234" )
    public Result update(@RequestBody TCfgUnionRule tCfgUnionRule) {
        Result result = new Result();
        try {
            result.setData(tCfgUnionRuleService.update(tCfgUnionRule));
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
    @Logs(title = "查询联动规则数据",content = "根据用户传递的参数查询联动规则信息",logType = 1,authority = "1234")
    public Result selectByPrimaryId(@RequestParam(value = "ruleId", required = true) Long ruleId) {
        Result result = new Result();
        try {
            TCfgUnionRule tCfgUnionRule = tCfgUnionRuleService.selectByPrimaryId(ruleId);
            result.setData(tCfgUnionRule);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询联动规则数据",content = "根据用户传递的参数查询联动规则信息",logType = 1)
    public Result select(@RequestParam(value = "ruleId", required = false) Long ruleId,
                            @RequestParam(value = "planId", required = false) Long planId,
                            @RequestParam(value = "ruleName", required = false) String ruleName,
                            @RequestParam(value = "ruleType", required = false) String ruleType,
                            @RequestParam(value = "ruleContent", required = false) String ruleContent,
                            @RequestParam(value = "ruleDelay", required = false) Integer ruleDelay,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "inputParam", required = false) String inputParam,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                         @RequestParam(value = "cameraId", required = false) Long cameraId,
                         @RequestParam(value = "presetId", required = false) Long presetId) {
        Result result = new Result();
        try {
            List<TCfgUnionRule> list = tCfgUnionRuleService.select(ruleId, planId, ruleName, ruleType, ruleContent, ruleDelay, description, inputParam, createTime, updateTime,cameraId,presetId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    @Logs(title = "查询联动规则数据",content = "根据用户传递的参数分页查询联动规则信息",logType = 1,authority = "1234")
    public Result selectByPage(@RequestParam(value = "ruleName", required = false) String ruleName,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCfgUnionRuleDetail> list = tCfgUnionRuleService.selectByPage(ruleName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入联动规则数据",content = "根据用户传递的参数批量插入数据联动规则信息",logType = 2)
    public Result batchAdd( @RequestBody List<TCfgUnionRule> list) {
        Result result = new Result();
        try {
        result.setData(tCfgUnionRuleService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    @Logs(title = "批量删除联动规则数据",content = "根据用户传递的参数批量删除联动规则数据",logType = 4)
    public Result batchDelete(@RequestParam(value = "ruleIds") String ruleIds) {
    Result result = new Result();
    try {
        result.setData(tCfgUnionRuleService.batchDelete(ruleIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }

    @ApiOperation(value = "查询四遥树信息")
    @RequestMapping(value = "/selectForTCfgMete", method = RequestMethod.GET)
    @Logs(title = "查询四遥树信息",content = "查询四遥树消息",logType = 1)
    public Result selectForTCfgMete() {
        Result result = new Result();
        try {
            result.setData(tCfgUnionRuleService.selectForTCfgMete());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询四遥树信息失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询预案信息")
    @RequestMapping(value = "/selectForTCPlan", method = RequestMethod.GET)
    @Logs(title = "查询预案信息",content = "查询预案信息",logType = 1,authority = "1234")
    public Result selectForTCPlan(@RequestParam(value = "planName", required = false) String planName,
                                  @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            TCruisePlan tCruisePlan =new TCruisePlan();
            tCruisePlan.setPlanName(planName);
            List<TCruisePlanCount> list = tCruisePlanService.selectByPage(tCruisePlan);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("分页查询失败描述：", e);
        }
        return result;
    }


}
