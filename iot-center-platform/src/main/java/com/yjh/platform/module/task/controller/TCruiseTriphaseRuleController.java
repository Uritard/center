package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TCruiseTriphaseRule;
import com.yjh.platform.module.task.service.TCruiseTriphaseRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lqh
 * @since 2023-03-18
 */
@RestController
@RequestMapping("/t-cruise-triphase-rule/v1")
@Api(value = "/t-cruise-triphase-rule", description = "三相告警配置实例表操作接口")
public class TCruiseTriphaseRuleController {

    @Autowired
    private final TCruiseTriphaseRuleService tCruiseTriphaseRuleService;

    private Logger log = LoggerFactory.getLogger(TCruiseTriphaseRuleController.class);

    public TCruiseTriphaseRuleController(TCruiseTriphaseRuleService tCruiseTriphaseRuleService) {
        this.tCruiseTriphaseRuleService = tCruiseTriphaseRuleService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TCruiseTriphaseRule tCruiseTriphaseRule) {
        Result result = new Result();
        try {
            result.setData(tCruiseTriphaseRuleService.add(tCruiseTriphaseRule));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public Result delete(@RequestParam(value = "triphaseId", required = true) Long triphaseId) {
        Result result = new Result();
        try {
            result.setData(tCruiseTriphaseRuleService.deleteByPrimaryId(triphaseId));
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Result update(@RequestBody TCruiseTriphaseRule tCruiseTriphaseRule) {
        Result result = new Result();
        try {
            result.setData(tCruiseTriphaseRuleService.update(tCruiseTriphaseRule));
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
    public Result selectByPrimaryId(@RequestParam(value = "triphaseId", required = true) Long triphaseId) {
        Result result = new Result();
        try {
            TCruiseTriphaseRule tCruiseTriphaseRule = tCruiseTriphaseRuleService.selectByPrimaryId(triphaseId);
            result.setData(tCruiseTriphaseRule);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "triphaseId", required = false) Long triphaseId,
                            @RequestParam(value = "triphaseName", required = false) String triphaseName,
                            @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "customId", required = false) String customId,
                            @RequestParam(value = "instanceOneId", required = false) Long instanceOneId,
                            @RequestParam(value = "instanceOneName", required = false) String instanceOneName,
                            @RequestParam(value = "oneCruiseDeviceName", required = false) String oneCruiseDeviceName,
                            @RequestParam(value = "instanceTwoId", required = false) Long instanceTwoId,
                            @RequestParam(value = "instanceTwoName", required = false) String instanceTwoName,
                            @RequestParam(value = "twoCruiseDeviceName", required = false) String twoCruiseDeviceName,
                            @RequestParam(value = "instanceTriId", required = false) Long instanceTriId,
                            @RequestParam(value = "instanceTriName", required = false) String instanceTriName,
                            @RequestParam(value = "triCruiseDeviceName", required = false) String triCruiseDeviceName,
                            @RequestParam(value = "identifyType", required = false) Integer identifyType,
                            @RequestParam(value = "identifySonType", required = false) Integer identifySonType,
                            @RequestParam(value = "triphaseType", required = false) Integer triphaseType,
                            @RequestParam(value = "warnThreshold", required = false) String warnThreshold,
                            @RequestParam(value = "warnLevel", required = false) Integer warnLevel) {
        Result result = new Result();
        try {
            List<TCruiseTriphaseRule> list = tCruiseTriphaseRuleService.select(triphaseId, triphaseName, deviceMeteId, deviceId, customId, instanceOneId, instanceOneName, oneCruiseDeviceName, instanceTwoId, instanceTwoName, twoCruiseDeviceName, instanceTriId, instanceTriName, triCruiseDeviceName, identifyType, identifySonType, triphaseType, warnThreshold, warnLevel);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruiseTriphaseRule tCruiseTriphaseRule,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
//            List<TCruiseTriphaseRule> list = tCruiseTriphaseRuleService.selectByPage(tCruiseTriphaseRule.getTriphaseName());
            resultMap.put("count", page.getTotal());
//            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TCruiseTriphaseRule> list) {
        Result result = new Result();
        try {
        result.setData(tCruiseTriphaseRuleService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "triphaseIds") String triphaseIds) {
    Result result = new Result();
    try {
        result.setData(tCruiseTriphaseRuleService.batchDelete(triphaseIds));
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
