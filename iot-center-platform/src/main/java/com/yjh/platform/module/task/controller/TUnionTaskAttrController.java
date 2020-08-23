package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TUnionTaskAttr;
import com.yjh.platform.module.task.service.TUnionTaskAttrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wf
 * @since 2020-08-19
 */
@RestController
@RequestMapping("/tUnionTaskAttr/v1")
@Api(value = "/tUnionTaskAttr", description = "联合巡视预案属性操作接口")
public class TUnionTaskAttrController {

    @Autowired
    private final TUnionTaskAttrService tUnionTaskAttrService;

    private Logger log = LoggerFactory.getLogger(TUnionTaskAttrController.class);

    public TUnionTaskAttrController(TUnionTaskAttrService tUnionTaskAttrService) {
        this.tUnionTaskAttrService = tUnionTaskAttrService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TUnionTaskAttr tUnionTaskAttr) {
        Result result = new Result();

        try {
            result.setData(tUnionTaskAttrService.insert(tUnionTaskAttr));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            log.error("添加联合巡视预案属性错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "UnionId", required = true) String UnionId,
                         @RequestParam(value = "InstanceId", required = true) Long InstanceId) {
        Result result = new Result();
        Map<String, Object> map = new HashMap<>();
        map.put("UnionId", UnionId);
        map.put("InstanceId", InstanceId);

        try {
            result.setData(tUnionTaskAttrService.deleteByPrimaryId(map));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.DELETEERROR.getCode(), e.getMessage());
            log.error("联合巡视预案属性删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.DELETEERROR.getCode(), ResultCodeEnum.DELETEERROR.getName());
            log.error("联合巡视预案属性删除错误:", e);
        }
        return result;
    }

    //更新
    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TUnionTaskAttr tUnionTaskAttr) {
        Result result = new Result();
        try {
            result.setData(tUnionTaskAttrService.update(tUnionTaskAttr));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新巡检预案参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新巡检预案参数错误:", e);
        }
        return result;
    }

    //查询 根据主键ID查询
    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "UnionId", required = true) String UnionId){
        Result result = new Result();

        try {
            result.setData(tUnionTaskAttrService.selectByPrimaryId(UnionId));
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //查询
    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "UnionId", required = false) String UnionId,
                         @RequestParam(value = "InstanceId", required = false) Long InstanceId,
                         @RequestParam(value = "DeviceMeteId", required = false) Long DeviceMeteId,
                         @RequestParam(value = "DeviceCustomIId", required = false) String DeviceCustomIId,
                         @RequestParam(value = "PointTaskId", required = false) String PointTaskId,
                         @RequestParam(value = "IfRobot", required = false) Integer IfRobot,
                         @RequestParam(value = "IfVideo", required = false) Integer IfVideo,
                         @RequestParam(value = "IfInferad", required = false) Integer IfInferad,
                         @RequestParam(value = "IfArtificial", required = false) Integer IfArtificial
    ){
        Result result = new Result();
        try {
            List<TUnionTaskAttr> list = this.tUnionTaskAttrService.select(UnionId,InstanceId,DeviceMeteId,DeviceCustomIId,
                    PointTaskId,IfRobot,IfVideo,IfInferad,IfArtificial);
            result.setData(list);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    //分页查询
    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TUnionTaskAttr tUnionTaskAttr,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize){
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TUnionTaskAttr> list = tUnionTaskAttrService.selectByPage(tUnionTaskAttr);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
