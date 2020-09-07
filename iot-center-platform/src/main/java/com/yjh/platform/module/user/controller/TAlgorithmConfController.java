package com.yjh.platform.module.user.controller;

import com.yjh.platform.module.user.entity.TAlgorithmConfDetail;
import com.yjh.platform.module.user.service.TAlgorithmConfService;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
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
 * @author lqh
 * @since 2020-09-07
 */
@RestController
@RequestMapping("/tAlgorithmConf/v1")
@Api(value = "/tAlgorithmConf", description = "算法配置表操作接口")
public class TAlgorithmConfController {

    @Autowired
    private final TAlgorithmConfService tAlgorithmConfService;

    private Logger log = LoggerFactory.getLogger(TAlgorithmConfController.class);

    public TAlgorithmConfController(TAlgorithmConfService tAlgorithmConfService) {
        this.tAlgorithmConfService = tAlgorithmConfService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TAlgorithmConf tAlgorithmConf) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfService.add(tAlgorithmConf));
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
    public Result delete(@RequestParam(value = "cameraId", required = true) Long cameraId) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfService.deleteByPrimaryId(cameraId));
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
    public Result update(@RequestBody TAlgorithmConf tAlgorithmConf) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfService.update(tAlgorithmConf));
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
    public Result selectByPrimaryId(@RequestParam(value = "cameraId", required = true) Long cameraId) {
        Result result = new Result();
        try {
            TAlgorithmConf tAlgorithmConf = tAlgorithmConfService.selectByPrimaryId(cameraId);
            result.setData(tAlgorithmConf);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "cameraId", required = false) Long cameraId,
                            @RequestParam(value = "algorithmId", required = false) Long algorithmId,
                            @RequestParam(value = "algorithmName", required = false) String algorithmName,
                            @RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "presetId", required = false) String presetId,
                            @RequestParam(value = "ifDel", required = false) Integer ifDel,
                            @RequestParam(value = "ifShow", required = false) Integer ifShow,
                            @RequestParam(value = "picUrl", required = false) String picUrl,
                            @RequestParam(value = "applyModule", required = false) Integer applyModule,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime) {
        Result result = new Result();
        try {
            List<TAlgorithmConf> list = tAlgorithmConfService.select(cameraId, algorithmId, algorithmName, status, presetId, ifDel, ifShow, picUrl, applyModule, createTime, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TAlgorithmConfDetail tAlgorithmConfDetail,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TAlgorithmConfDetail> list = tAlgorithmConfService.selectByPage(tAlgorithmConfDetail);
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
    public Result batchAdd(@RequestBody List<TAlgorithmConf> list) {
        Result result = new Result();
        try {
        result.setData(tAlgorithmConfService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
