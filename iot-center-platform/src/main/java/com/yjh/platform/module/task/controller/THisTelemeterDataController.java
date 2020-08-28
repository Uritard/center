package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.service.THisTelemeterDataService;
import com.yjh.platform.module.task.entity.THisTelemeterData;
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
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tHisTelemeterData/v1")
@Api(value = "/tHisTelemeterData", description = "遥测历史数据表操作接口")
public class THisTelemeterDataController {

    @Autowired
    private final THisTelemeterDataService tHisTelemeterDataService;

    private Logger log = LoggerFactory.getLogger(THisTelemeterDataController.class);

    public THisTelemeterDataController(THisTelemeterDataService tHisTelemeterDataService) {
        this.tHisTelemeterDataService = tHisTelemeterDataService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody THisTelemeterData tHisTelemeterData) {
        Result result = new Result();
        try {
            result.setData(tHisTelemeterDataService.insert(tHisTelemeterData));
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
    public Result delete(@RequestParam(value = "id")Long id) {
        Result result = new Result();
        try {
            result.setData(tHisTelemeterDataService.deleteByPrimaryId(id));
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
    public Result update(@RequestBody THisTelemeterData tHisTelemeterData) {
        Result result = new Result();
        try {
            result.setData(tHisTelemeterDataService.update(tHisTelemeterData));
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
    public Result selectByPrimaryId(@RequestParam(value = "id")Long id) {
        Result result = new Result();
        try {
            THisTelemeterData tHisTelemeterData = tHisTelemeterDataService.selectByPrimaryId(id);
            result.setData(tHisTelemeterData);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "id",required = false)Long id,
                            @RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "recordTime", required = false) Date recordTime,
                            @RequestParam(value = "meteKind", required = false) Integer meteKind,
                            @RequestParam(value = "meteValue", required = false) String meteValue,
                            @RequestParam(value = "lastMeteValue", required = false) String lastMeteValue) {
        Result result = new Result();
        try {
            List<THisTelemeterData> list = tHisTelemeterDataService.select(id, meteId, deviceId, recordTime, meteKind, meteValue, lastMeteValue);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody THisTelemeterData tHisTelemeterData,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<THisTelemeterData> list = tHisTelemeterDataService.selectByPage(tHisTelemeterData);
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
    public Result batchInsert(@RequestBody List<THisTelemeterData> list) {
        Result result = new Result();
        try {
        result.setData(tHisTelemeterDataService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
