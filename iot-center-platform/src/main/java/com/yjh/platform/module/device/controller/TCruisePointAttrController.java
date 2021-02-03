package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.service.TCruisePointAttrService;
import com.yjh.platform.module.device.entity.TCruisePointAttr;
import java.util.HashMap;
import java.util.List;

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
 * @since 2020-10-13
 */
@RestController
@RequestMapping("/t-cruise-point-attr/v1")
@Api(value = "/t-cruise-point-attr", description = "巡检点属性表操作接口")
public class TCruisePointAttrController {

    @Autowired
    private final TCruisePointAttrService tCruisePointAttrService;

    private Logger log = LoggerFactory.getLogger(TCruisePointAttrController.class);

    public TCruisePointAttrController(TCruisePointAttrService tCruisePointAttrService) {
        this.tCruisePointAttrService = tCruisePointAttrService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增巡检点数据",content = "根据用户传递的参数插入巡检点数据",logType = 2)
    public Result add(@RequestBody TCruisePointAttr tCruisePointAttr) {
        Result result = new Result();
        try {
            result.setData(tCruisePointAttrService.add(tCruisePointAttr));
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
    @Logs(title = "删除巡检点数据",content = "根据用户传递的参数删除巡检点数据",logType = 4)
    public Result delete(@RequestParam(value = "instanceId", required = true) Long instanceId) {
        Result result = new Result();
        try {
            result.setData(tCruisePointAttrService.deleteByPrimaryId(instanceId));
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
    @Logs(title = "更新巡检点数据",content = "根据用户传递的参数修改巡检点数据",logType = 3)
    public Result update(@RequestBody TCruisePointAttr tCruisePointAttr) {
        Result result = new Result();
        try {
            result.setData(tCruisePointAttrService.update(tCruisePointAttr));
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
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数查询巡检点数据",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "instanceId", required = true) Long instanceId) {
        Result result = new Result();
        try {
            TCruisePointAttr tCruisePointAttr = tCruisePointAttrService.selectByPrimaryId(instanceId);
            result.setData(tCruisePointAttr);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数查询巡检点数据",logType = 1)
    public Result select(@RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "instanceName", required = false) String instanceName,
                            @RequestParam(value = "attrName", required = false) String attrName,
                            @RequestParam(value = "attrValue", required = false) String attrValue,
                            @RequestParam(value = "remark1", required = false) String remark1) {
        Result result = new Result();
        try {
            List<TCruisePointAttr> list = tCruisePointAttrService.select(instanceId, instanceName, attrName, attrValue, remark1);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询巡检点数据",content = "根据用户传递的参数分页查询巡检点数据",logType = 1)
    public Result selectByPage(@RequestBody TCruisePointAttr tCruisePointAttr,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCruisePointAttr> list = tCruisePointAttrService.selectByPage(tCruisePointAttr);
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
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入巡检点数据",content = "根据用户传递的参批量插入巡检点数据",logType = 2)
    public Result batchAdd(@RequestBody List<TCruisePointAttr> list) {
        Result result = new Result();
        try {
        result.setData(tCruisePointAttrService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    @Logs(title = "批量删除巡检点数据",content = "根据用户传递的参数批量删除巡检点数据",logType = 4)
    public Result batchDelete(@RequestParam(value = "instanceIds") String instanceIds) {
    Result result = new Result();
    try {
        result.setData(tCruisePointAttrService.batchDelete(instanceIds));
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
