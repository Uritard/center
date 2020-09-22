package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.TCfgUnionRuleDetail;
import com.yjh.platform.module.task.service.TCfgUnionRuleService;
import com.yjh.platform.module.task.entity.TCfgUnionRule;
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
 * @author lqh
 * @since 2020-09-18
 */
@RestController
@RequestMapping("/tCfgUnionRule/v1")
@Api(value = "/tCfgUnionRule", description = "联动规则表操作接口")
public class TCfgUnionRuleController {

    @Autowired
    private final TCfgUnionRuleService tCfgUnionRuleService;

    private Logger log = LoggerFactory.getLogger(TCfgUnionRuleController.class);

    public TCfgUnionRuleController(TCfgUnionRuleService tCfgUnionRuleService) {
        this.tCfgUnionRuleService = tCfgUnionRuleService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TCfgUnionRule tCfgUnionRule) {
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
    public Result select(@RequestParam(value = "ruleId", required = false) Long ruleId,
                            @RequestParam(value = "planId", required = false) Long planId,
                            @RequestParam(value = "ruleName", required = false) String ruleName,
                            @RequestParam(value = "ruleType", required = false) String ruleType,
                            @RequestParam(value = "ruleContent", required = false) String ruleContent,
                            @RequestParam(value = "ruleDelay", required = false) Integer ruleDelay,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "inputParam", required = false) String inputParam,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime) {
        Result result = new Result();
        try {
            List<TCfgUnionRule> list = tCfgUnionRuleService.select(ruleId, planId, ruleName, ruleType, ruleContent, ruleDelay, description, inputParam, createTime, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public Result selectByPage(@RequestParam(value = "ruleName", required = false) String ruleName,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
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
    public Result batchAdd(@RequestBody List<TCfgUnionRule> list) {
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


}
