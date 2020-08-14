package com.yjh.platform.module.user.controller;

import com.yjh.platform.module.user.service.TAlgorithmInfoService;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
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
 * @author tt
 * @since 2020-08-06
 */
@RestController
@RequestMapping("/tAlgorithmInfo/v1")
@Api(value = "/tAlgorithmInfo", description = "算法表操作接口")
public class TAlgorithmInfoController {

    @Autowired
    private final TAlgorithmInfoService tAlgorithmInfoService;

    private Logger log = LoggerFactory.getLogger(TAlgorithmInfoController.class);

    public TAlgorithmInfoController(TAlgorithmInfoService tAlgorithmInfoService) {
        this.tAlgorithmInfoService = tAlgorithmInfoService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TAlgorithmInfo tAlgorithmInfo) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmInfoService.insert(tAlgorithmInfo));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "algorithmId", required = true) Long algorithmId) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmInfoService.deleteByPrimaryId(algorithmId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除算法异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除算法错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TAlgorithmInfo tAlgorithmInfo) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmInfoService.update(tAlgorithmInfo));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新算法异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新算法错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "algorithmId", required = true) Long algorithmId) {
        Result result = new Result();
        try {
            TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoService.selectByPrimaryId(algorithmId);
            result.setData(tAlgorithmInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询算法失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "algorithmId", required = false) Long algorithmId,
                            @RequestParam(value = "algorithmName", required = false) String algorithmName,
                            @RequestParam(value = "algorithmType", required = false) String algorithmType,
                            @RequestParam(value = "describel", required = false) String describel,
                            @RequestParam(value = "algorithmCode", required = false) Integer algorithmCode,
                            @RequestParam(value = "analysType", required = false) String analysType) {
        Result result = new Result();
        try {
            List<TAlgorithmInfo> list = tAlgorithmInfoService.select(algorithmId, algorithmName, algorithmType, describel, algorithmCode, analysType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TAlgorithmInfo tAlgorithmInfo,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TAlgorithmInfo> list = tAlgorithmInfoService.selectByPage(tAlgorithmInfo);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("分页查询算法失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TAlgorithmInfo> list) {
        Result result = new Result();
        try {
        result.setData(tAlgorithmInfoService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入算法失败：" + e);
        }
        return result;
    }

}
