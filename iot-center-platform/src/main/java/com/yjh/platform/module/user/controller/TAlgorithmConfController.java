package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.entity.TAlgorithmConfDetail;
import com.yjh.platform.module.user.service.TAlgorithmConfService;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
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
    @Logs(title = "新增算法配置数据",content = "根据用户传递的参数新增算法配置数据",logType = 2)
    public Result add(@Validated @RequestBody TAlgorithmConf tAlgorithmConf) {

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
    @Logs(title = "删除算法配置数据",content = "根据用户传递的参数删除算法配置数据",logType = 4)
    public Result delete(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfService.deleteByPrimaryId(presetId));
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
    @Logs(title = "修改算法配置数据",content = "根据用户传递的参数修改算法配置数据",logType = 3)
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
    @Logs(title = "查询算法配置数据",content = "根据用户传递的参数查询算法配置信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TAlgorithmConf tAlgorithmConf = tAlgorithmConfService.selectByPrimaryId(presetId);
            result.setData(tAlgorithmConf);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询算法配置数据",content = "根据用户传递的参数查询算法配置信息",logType = 1)
    public Result select(@RequestParam(value = "presetId", required = false) Long presetId,
                            @RequestParam(value = "algorithmId", required = false) Long algorithmId,
                            @RequestParam(value = "configName", required = false) String configName,
                            @RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "ifDel", required = false) Integer ifDel,
                            @RequestParam(value = "ifShow", required = false) Integer ifShow,
                            @RequestParam(value = "picUrl", required = false) String picUrl,
                            @RequestParam(value = "applyModule", required = false) Integer applyModule,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "updateTime", required = false) Date updateTime) {
        Result result = new Result();
        try {
            List<TAlgorithmConf> list = tAlgorithmConfService.select(presetId, algorithmId, configName, status, ifDel, ifShow, picUrl, applyModule, createTime, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询算法配置数据",content = "根据用户传递的参数分页查询算法配置信息",logType = 1)
    public Result selectByPage(@RequestBody TAlgorithmConfDetail tAlgorithmConfDetail
                              ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tAlgorithmConfDetail.getPageNum()!=null?tAlgorithmConfDetail.getPageNum():1, tAlgorithmConfDetail.getPageSize()!=null?tAlgorithmConfDetail.getPageSize():0,true,null,true);
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
    @Logs(title = "批量插入算法配置数据",content = "根据用户传递的参数批量插入算法配置信息",logType = 2)
    public Result batchAdd(@Validated @RequestBody List<TAlgorithmConf> list) {
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
