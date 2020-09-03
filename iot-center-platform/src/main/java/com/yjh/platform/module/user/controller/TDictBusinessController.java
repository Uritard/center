package com.yjh.platform.module.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.StringUtils;
import com.yjh.platform.module.user.service.TDictBusinessService;
import com.yjh.platform.module.user.entity.TDictBusiness;

import java.util.ArrayList;
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

import javax.annotation.PostConstruct;


/**
 * @author tt
 * @since 2020-08-04
 */
@RestController
@RequestMapping("/tDictBusiness/v1")
@Api(value = "/tDictBusiness", description = "业务字典表操作接口")
public class TDictBusinessController {

    @Autowired
    private final TDictBusinessService tDictBusinessService;

    private Logger log = LoggerFactory.getLogger(TDictBusinessController.class);

    public TDictBusinessController(TDictBusinessService tDictBusinessService) {
        this.tDictBusinessService = tDictBusinessService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TDictBusiness tDictBusiness) {
        Result result = new Result();
        try {
            result.setData(tDictBusinessService.insert(tDictBusiness));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_dictcode") != -1) {
                result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), "字典表编码重复");
            } else {
                result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
                log.error("业务字典添加错误:", e);
            }
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "dictId", required = true) Integer dictId) {
        Result result = new Result();
        try {
            result.setData(tDictBusinessService.deleteByPrimaryId(dictId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除业务字典异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("业务字典删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TDictBusiness tDictBusiness) {
        Result result = new Result();
        try {
            result.setData(tDictBusinessService.update(tDictBusiness));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("业务字典更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("业务字典更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "dictId", required = true) Integer dictId) {
        Result result = new Result();
        try {
            TDictBusiness tDictBusiness = tDictBusinessService.selectByPrimaryId(dictId);
            result.setData(tDictBusiness);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("业务字典主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "dictId", required = false) Integer dictId,
                            @RequestParam(value = "dictCode", required = false) String dictCode,
                            @RequestParam(value = "colName", required = false) String colName,
                            @RequestParam(value = "dictNote", required = false) String dictNote,
                            @RequestParam(value = "upDict", required = false) Integer upDict,
                            @RequestParam(value = "remark", required = false) String remark,
                            @RequestParam(value = "sort", required = false) Long sort) {
        Result result = new Result();
        try {
            List<TDictBusiness> list = tDictBusinessService.select(dictId, dictCode, colName, dictNote, upDict, remark, sort);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("业务字典查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询，根据dictNote模糊查找")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TDictBusiness tDictBusiness,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TDictBusiness> list = tDictBusinessService.selectByPage(tDictBusiness);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("业务字典分页失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TDictBusiness> list) {
        Result result = new Result();
        try {
        result.setData(tDictBusinessService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("业务字典批量插入失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "多类型查询")
    @RequestMapping(value = "/selectQuery", method = RequestMethod.GET)
    public Result selectQuery( @RequestParam(value = "colNames", required = false) String colName) {
        Result result = new Result();
        try {
        String [] splitColNames=colName.split("'|,");
        List<String> colNames = new ArrayList<>();
        for(String ColName : splitColNames) {
            colNames.add(ColName);
        }
        result.setData(tDictBusinessService.selectQuery(colNames));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("业务字典查询失败描述：", e);
        }
        return result;
    }

}
