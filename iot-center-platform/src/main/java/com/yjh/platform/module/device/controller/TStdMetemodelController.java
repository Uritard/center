package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import com.yjh.platform.module.device.service.TStdMetemodelService;
import com.yjh.platform.module.device.entity.TStdMeteModel;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.yjh.platform.module.user.entity.TCameraInfo;
import io.swagger.annotations.*;
import io.swagger.models.auth.In;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
 * @since 2020-08-07
 */
@RestController
@RequestMapping("/tStdMeteModel/v1")
@Api(value = "/tStdMeteModel", description = "系统测点模版表操作接口")
public class TStdMetemodelController {

    @Autowired
    private final TStdMetemodelService tStdMetemodelService;

    private Logger log = LoggerFactory.getLogger(TStdMetemodelController.class);

    public TStdMetemodelController(TStdMetemodelService tStdMetemodelService) {
        this.tStdMetemodelService = tStdMetemodelService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TStdMeteModel tStdMeteModel) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.add(tStdMeteModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点模版添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.deleteByPrimaryId(modelId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("系统测点模版删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点模版删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TStdMeteModel tStdMeteModel) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.update(tStdMeteModel));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("系统测点模版更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();
        try {
            TStdMeteModel tStdMeteModel = tStdMetemodelService.selectByPrimaryId(modelId);
            result.setData(tStdMeteModel);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "modelId", required = false) Long modelId,
                            @RequestParam(value = "modelName", required = false) String modelName,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TStdMeteModel> list = tStdMetemodelService.select(modelId, modelName, deviceType, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TStdMeteModel tStdMeteModel,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TStdMeteModel> list = tStdMetemodelService.selectByPage(tStdMeteModel);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TStdMeteModel> list) {
        Result result = new Result();
        try {
        result.setData(tStdMetemodelService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("系统测点模版批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据模版的设备类型查询对应的初始测点信息")
    @RequestMapping(value = "/selectMeteByDeviceType", method = RequestMethod.GET)
    public Result selectMeteByDeviceType(@RequestParam(value = "deviceType") Integer deviceType) {
        Result result = new Result();
        try {
            List<TStdMeteModelDetail> list = tStdMetemodelService.selectMeteByDeviceType(deviceType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询对应的初始测点信息失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入模版测点")
    @RequestMapping(value = "/batchAddModelMete", method = RequestMethod.POST)
    public Result batchAddModelMete(@RequestBody List<TStdMeteModelDetail> list) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.batchAddModelMete(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入模版测点失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete",method = RequestMethod.DELETE)
    public Result batchDetele(@RequestParam(value = "list")List<String> list){
        Result result=new Result();
        try{
            result.setData(tStdMetemodelService.batchDelete(list));
        }catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("系统测点模版删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点模版删除错误:", e);
        }
        return result;
    }


}
