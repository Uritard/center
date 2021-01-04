package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.entity.TCruiseTypeDetail;
import com.yjh.platform.module.task.service.TCruiseTypeService;
import com.yjh.platform.module.task.entity.TCruiseType;
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
 * @since 2020-11-17
 */
@RestController
@RequestMapping("/tCruiseType/v1")
@Api(value = "/tCruiseType", description = "巡视类型关联实例点表操作接口")
public class TCruiseTypeController {

    @Autowired
    private final TCruiseTypeService tCruiseTypeService;

    private Logger log = LoggerFactory.getLogger(TCruiseTypeController.class);

    public TCruiseTypeController(TCruiseTypeService tCruiseTypeService) {
        this.tCruiseTypeService = tCruiseTypeService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public Result add(@RequestParam(value = "cruiseType") Integer cruiseType,
                      @RequestParam(value = "instanceList") String instanceList,
                      @RequestParam(value = "subType",required = false) String remake) {
        Result result = new Result();
        try {
            result.setData(tCruiseTypeService.add(cruiseType,instanceList,remake));
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
    public Result delete(@RequestParam(value = "subType", required = false) Integer subType) {
        Result result = new Result();
        try {
            result.setData(tCruiseTypeService.deleteByPrimaryId(subType));
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
    public Result update(@RequestBody TCruiseType tCruiseType) {
        Result result = new Result();
        try {
            result.setData(tCruiseTypeService.update(tCruiseType));
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
    public Result selectByPrimaryId(@RequestParam(value = "subType", required = true) Integer subType) {
        Result result = new Result();
        try {
            TCruiseType tCruiseType = tCruiseTypeService.selectByPrimaryId(subType);
            result.setData(tCruiseType);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "subType", required = true) Integer subType,
                         @RequestParam(value = "pageNum", required = false) Integer pageNum,
                         @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(pageNum != null && pageSize != null){
                Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
                List<TCruiseTypeDetail> list = tCruiseTypeService.select(subType);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                resultMap.put("instanceIdList",tCruiseTypeService.selectIdList(subType));
                result.setData(resultMap);
            }else {
                List<TCruiseTypeDetail> list = tCruiseTypeService.select(subType);
                result.setData(list);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCruiseType tCruiseType,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCruiseTypeDetail> list = tCruiseTypeService.selectByPage(tCruiseType);
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
    public Result batchAdd(@RequestBody List<TCruiseType> list) {
        Result result = new Result();
        try {
        result.setData(tCruiseTypeService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "subTypes") String subTypes) {
    Result result = new Result();
    try {
        result.setData(tCruiseTypeService.batchDelete(subTypes));
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
