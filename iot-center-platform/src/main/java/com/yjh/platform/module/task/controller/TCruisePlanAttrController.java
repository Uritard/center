package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.entity.TCruisePlanAttrDetail;
import com.yjh.platform.module.task.service.TCruisePlanAttrService;
import com.yjh.platform.module.task.entity.TCruisePlanAttr;
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
 * @author tt
 * @since 2020-09-04
 */
@RestController
@RequestMapping("/tCruisePlanAttr/v1")
@Api(value = "/tCruisePlanAttr", description = "巡检预案属性表操作接口")
public class TCruisePlanAttrController {

    @Autowired
    private final TCruisePlanAttrService tCruisePlanAttrService;

    private Logger log = LoggerFactory.getLogger(TCruisePlanAttrController.class);

    public TCruisePlanAttrController(TCruisePlanAttrService tCruisePlanAttrService) {
        this.tCruisePlanAttrService = tCruisePlanAttrService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增",content = "根据用户传递的参数新增巡检预案属性数据",logType = 2)
    public Result insert(@RequestBody TCruisePlanAttr tCruisePlanAttr) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanAttrService.insert(tCruisePlanAttr));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加预案属性错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除",content = "根据用户传递的参数删除巡检预案属性数据",logType = 4)
    public Result delete(@RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanAttrService.deleteByPrimaryId(planId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除预案属性异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改",content = "根据用户传递的参数修改巡检预案属性数据",logType = 3)
    public Result update(@RequestBody TCruisePlanAttr tCruisePlanAttr) {
        Result result = new Result();
        try {
            result.setData(tCruisePlanAttrService.update(tCruisePlanAttr));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新预案属性异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询巡检预案属性信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "planId", required = true) Long planId) {
        Result result = new Result();
        try {
            List<TCruisePlanAttrDetail> tCruisePlanAttrList = tCruisePlanAttrService.selectByPrimaryId(planId);
            result.setData(tCruisePlanAttrList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询巡检预案属性信息",logType = 1)
    public Result select(@RequestParam(value = "planId", required = false) Long planId,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "pointType", required = false) Integer pointType,
                            @RequestParam(value = "areaId", required = false) String areaId,
                            @RequestParam(value = "cruiseRegionIds", required = false) String cruiseRegionIds,
                            @RequestParam(value = "exceptionType", required = false) Integer exceptionType,
                            @RequestParam(value = "robotId", required = false) Long robotId,
                            @RequestParam(value = "position", required = false) String position,
                            @RequestParam(value = "algorithmId", required = false) Long algorithmId,
                            @RequestParam(value = "inferadAnalyze", required = false) String inferadAnalyze,
                            @RequestParam(value = "irTempBox", required = false) String irTempBox,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime,
                         @RequestParam(value = "subType", required = false) Integer subType) {
        Result result = new Result();
        try {
            List<TCruisePlanAttr> list = tCruisePlanAttrService.select(planId, instanceId, pointType, areaId, cruiseRegionIds, exceptionType, robotId, position, algorithmId, inferadAnalyze, irTempBox, createTime, updateTime,subType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询",content = "根据用户传递的参数分页查询巡检预案属性信息",logType = 1)
    public Result selectByPage(@RequestBody TCruisePlanAttr tCruisePlanAttr,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCruisePlanAttr> list = tCruisePlanAttrService.selectByPage(tCruisePlanAttr);
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
    @Logs(title = "批量插入",content = "根据用户传递的参数批量插入巡检预案属性数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCruisePlanAttr> list) {
        Result result = new Result();
        try {
        result.setData(tCruisePlanAttrService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
