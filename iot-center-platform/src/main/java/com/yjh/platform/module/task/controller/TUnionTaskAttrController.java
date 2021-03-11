package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.service.TUnionTaskAttrService;
import com.yjh.platform.module.task.entity.TUnionTaskAttr;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/tUnionTaskAttr/v1")
@Api(value = "/tUnionTaskAttr", description = "联合巡视预案属性表操作接口")
public class TUnionTaskAttrController {

    @Autowired
    private final TUnionTaskAttrService tUnionTaskAttrService;

    private Logger log = LoggerFactory.getLogger(TUnionTaskAttrController.class);

    public TUnionTaskAttrController(TUnionTaskAttrService tUnionTaskAttrService) {
        this.tUnionTaskAttrService = tUnionTaskAttrService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增联合巡视预案属性数据",content = "根据用户传递的参数新增联合巡视预案属性数据",logType = 2)
    public Result insert(@Validated @RequestBody TUnionTaskAttr tUnionTaskAttr) {
        Result result = new Result();
        try {
            result.setData(tUnionTaskAttrService.insert(tUnionTaskAttr));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加联合巡视预案属性错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除联合巡视预案属性数据",content = "根据用户传递的参数删除联合巡视预案属性数据",logType = 4)
    public Result delete(@RequestParam(value = "unionId", required = true) String unionId) {
        Result result = new Result();
        try {
            result.setData(tUnionTaskAttrService.deleteByPrimaryId(unionId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除联合巡视预案属性异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改联合巡视预案属性数据",content = "根据用户传递的参数修改联合巡视预案属性数据",logType = 3)
    public Result update(@RequestBody TUnionTaskAttr tUnionTaskAttr) {
        Result result = new Result();
        try {
            result.setData(tUnionTaskAttrService.update(tUnionTaskAttr));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新联合巡视预案属性异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询联合巡视预案属性数据",content = "根据用户传递的参数查询联合巡视预案属性信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "unionId", required = true) String unionId) {
        Result result = new Result();
        try {
            TUnionTaskAttr tUnionTaskAttr = tUnionTaskAttrService.selectByPrimaryId(unionId);
            result.setData(tUnionTaskAttr);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询联合巡视预案属性数据",content = "根据用户传递的参数查询联合巡视预案属性信息",logType = 1)
    public Result select(@RequestParam(value = "unionId", required = false) String unionId,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
                            @RequestParam(value = "deviceCustomId", required = false) String deviceCustomId,
                            @RequestParam(value = "pointTaskId", required = false) Long pointTaskId,
                            @RequestParam(value = "ifRobot", required = false) Integer ifRobot,
                            @RequestParam(value = "ifVideo", required = false) Integer ifVideo,
                            @RequestParam(value = "ifInferad", required = false) Integer ifInferad,
                            @RequestParam(value = "ifArtificial", required = false) Integer ifArtificial) {
        Result result = new Result();
        try {
            List<TUnionTaskAttr> list = tUnionTaskAttrService.select(unionId, instanceId, deviceMeteId, deviceCustomId, pointTaskId, ifRobot, ifVideo, ifInferad, ifArtificial);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询联合巡视预案属性数据",content = "根据用户传递的参数发查询联合巡视预案属性信息",logType = 1)
    public Result selectByPage(@RequestBody TUnionTaskAttr tUnionTaskAttr
                              ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tUnionTaskAttr.getPageNum()!=null?tUnionTaskAttr.getPageNum():1, tUnionTaskAttr.getPageSize()!=null?tUnionTaskAttr.getPageSize():0,true,null,true);
            List<TUnionTaskAttr> list = tUnionTaskAttrService.selectByPage(tUnionTaskAttr);
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
    @Logs(title = "批量插入联合巡视预案属性数据",content = "根据用户传递的参数批量插入联合巡视预案属性数据",logType = 2)
    public Result batchInsert( @RequestBody List<TUnionTaskAttr> list) {
        Result result = new Result();
        try {
        result.setData(tUnionTaskAttrService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
