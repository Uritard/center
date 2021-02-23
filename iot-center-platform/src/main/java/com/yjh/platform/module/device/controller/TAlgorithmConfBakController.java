package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.service.TAlgorithmConfBakService;
import com.yjh.platform.module.device.entity.TAlgorithmConfBak;
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
 * @since 2020-12-22
 */
@RestController
@RequestMapping("/tAlgorithmConfBak/v1")
@Api(value = "/tAlgorithmConfBak", description = "算法配置表2操作接口")
public class TAlgorithmConfBakController {

    @Autowired
    private final TAlgorithmConfBakService tAlgorithmConfBakService;

    private Logger log = LoggerFactory.getLogger(TAlgorithmConfBakController.class);

    public TAlgorithmConfBakController(TAlgorithmConfBakService tAlgorithmConfBakService) {
        this.tAlgorithmConfBakService = tAlgorithmConfBakService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增算法配置信息",content = "根据用户传递的参数新增算法配置信息",logType = 2)
    public Result add(@Validated  @RequestBody TAlgorithmConfBak tAlgorithmConfBak) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfBakService.add(tAlgorithmConfBak));
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
    @Logs(title = "删除算法配置信息",content = "根据用户传递的参数删除算法配置信息",logType = 4)
    public Result delete(@RequestParam(value = "deviceMeteId", required = true) Long deviceMeteId) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfBakService.deleteByPrimaryId(deviceMeteId));
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
    @Logs(title = "更新算法配置数据",content = "根据用户传递的参数更新算法配置数据",logType = 3)
    public Result update(@RequestBody TAlgorithmConfBak tAlgorithmConfBak) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfBakService.update(tAlgorithmConfBak));
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
    @Logs(title = "查询算法配置信息",content = "根据用户传递的参数查询算法配置信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "deviceMeteId", required = true) Long deviceMeteId) {
        Result result = new Result();
        try {
            List<TAlgorithmConfBak> tAlgorithmConfBak = tAlgorithmConfBakService.selectByPrimaryId(deviceMeteId);
            result.setData(tAlgorithmConfBak);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询算法配置信息",content = "根据用户传递的参数查询算法配置信息",logType = 1)
    public Result select(@RequestParam(value = "deviceMeteId", required = false) Long deviceMeteId,
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
            List<TAlgorithmConfBak> list = tAlgorithmConfBakService.select(deviceMeteId, algorithmId, configName, status, ifDel, ifShow, picUrl, applyModule, createTime, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "分页查询算法配置信息",content = "根据用户传递的参数分页查询算法配置信息",logType = 1)
    public Result selectByPage(@RequestBody TAlgorithmConfBak tAlgorithmConfBak)
//                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
//                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize)
                                 {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            //Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            Page page = PageHelper.startPage(tAlgorithmConfBak.getPageNum()!=null?tAlgorithmConfBak.getPageNum():1, tAlgorithmConfBak.getPageSize()!=null?tAlgorithmConfBak.getPageSize():0,true,null,true);
            List<TAlgorithmConfBak> list = tAlgorithmConfBakService.selectByPage(tAlgorithmConfBak);
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
    @Logs(title = "批量插入算法配置信息",content = "根据用户传递的参数批量插入算法配置信息",logType = 2)
    public Result batchAdd(@Validated @RequestBody List<TAlgorithmConfBak> list) {
        Result result = new Result();
        try {
        result.setData(tAlgorithmConfBakService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    @Logs(title = "批量删除算法配置信息",content = "根据用户传递的参数批量删除算法配置信息",logType = 4)
    public Result batchDelete(@RequestParam(value = "deviceMeteIds") String deviceMeteIds) {
    Result result = new Result();
    try {
        result.setData(tAlgorithmConfBakService.batchDelete(deviceMeteIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }


    @ApiOperation(value = "获取已有的算法分析类型")
    @RequestMapping(value = "/selectAnalyseType", method = RequestMethod.GET)
    @Logs(title = "查询已有的算法分析类型",content = "根据用户传递的参数查询已有的算法分析类型",logType = 1)
    public Result selectAnalyseType() {
        Result result = new Result();
        try {
            result.setData(tAlgorithmConfBakService.selectAnalyseType());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
