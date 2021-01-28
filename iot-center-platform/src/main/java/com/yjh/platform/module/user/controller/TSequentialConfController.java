package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.service.TSequentialConfService;
import com.yjh.platform.module.user.entity.TSequentialConf;
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
 * @since 2021-01-21
 */
@RestController
@RequestMapping("/tSequentialConf/v1")
@Api(value = "/tSequentialConf", description = "顺控配置表操作接口")
public class TSequentialConfController {

    @Autowired
    private final TSequentialConfService tSequentialConfService;

    private Logger log = LoggerFactory.getLogger(TSequentialConfController.class);

    public TSequentialConfController(TSequentialConfService tSequentialConfService) {
        this.tSequentialConfService = tSequentialConfService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增",content = "根据用户传递的参数新增顺控配置信息",logType = 2)
    public Result add(@RequestBody TSequentialConf tSequentialConf) {
        Result result = new Result();
        try {
            int i = tSequentialConfService.add(tSequentialConf);
            if(i == -1){
                result.setCode(209,"此摄像机已绑定其他监控量");

            }else {
                result.setData(i);
            }

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
    @Logs(title = "删除",content = "根据用户传递的参数删除顺控配置信息",logType = 4)
    public Result delete(@RequestParam(value = "cfgDeviceId", required = true) String cfgDeviceId) {
        Result result = new Result();
        try {
            result.setData(tSequentialConfService.deleteByPrimaryId(cfgDeviceId));
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
    @Logs(title = "修改",content = "根据用户传递的参数修改顺控配置信息",logType = 3)
    public Result update(@RequestBody TSequentialConf tSequentialConf) {
        Result result = new Result();
        try {
            int i = tSequentialConfService.update(tSequentialConf);
            if(i ==-1){
                result.setCode(209,"此摄像机已绑定其他监控量");
            }else {
                result.setData(i);
            }

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
    @Logs(title = "查询",content = "根据用户传递的参数查询顺控配置信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "cfgDeviceId", required = true) String cfgDeviceId) {
        Result result = new Result();
        try {
            TSequentialConf tSequentialConf = tSequentialConfService.selectByPrimaryId(cfgDeviceId);
            result.setData(tSequentialConf);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询顺控配置信息",logType = 1)
    public Result select(@RequestParam(value = "cfgDeviceId", required = false) String cfgDeviceId,
                            @RequestParam(value = "cfgMeteId", required = false) String cfgMeteId,
                            @RequestParam(value = "presetId", required = false) Long presetId,
                            @RequestParam(value = "identifyResult", required = false) String identifyResult) {
        Result result = new Result();
        try {
            List<TSequentialConf> list = tSequentialConfService.select(cfgDeviceId, cfgMeteId, presetId, identifyResult);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数分页查询顺控配置信息",logType = 1)
    public Result selectByPage(@RequestParam(value = "cfgDeviceName", required = false) String cfgDeviceName,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "0") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TSequentialConf> list = tSequentialConfService.selectByPage(cfgDeviceName);
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
    @Logs(title = "批量插入",content = "根据用户传递的参数批量插入顺控配置信息",logType = 2)
    public Result batchAdd(@RequestBody List<TSequentialConf> list) {
        Result result = new Result();
        try {
        result.setData(tSequentialConfService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    @Logs(title = "批量删除",content = "根据用户传递的参数批量删除顺控配置信息",logType = 4)
    public Result batchDelete(@RequestParam(value = "cfgDeviceIds") String cfgDeviceIds) {
    Result result = new Result();
    try {
        result.setData(tSequentialConfService.batchDelete(cfgDeviceIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }


    @ApiOperation(value = "查询顺控设备树")
    @RequestMapping(value = "/sequenceControlResponse", method = RequestMethod.GET)
    //@Logs(title = "查询",content = "根据用户传递的参数查询",logType = 1)
    public Result sequenceControlResponse(@RequestParam(value = "map", required = false) Map<String,List<Long>> map) {
        Result result = new Result();
        try {

            //List<AreaInfo> list = tSequentialConfService.selectForCfgDeviceTree(cfgDeviceName);
            result.setData(1);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询顺控设备树")
    @RequestMapping(value = "/selectForCfgDeviceTree", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询顺控设备树",logType = 1)
    public Result selectForCfgDeviceTree(@RequestParam(value = "cfgDeviceName", required = false) String cfgDeviceName) {
        Result result = new Result();
        try {
            List<AreaInfo> list = tSequentialConfService.selectForCfgDeviceTree(cfgDeviceName);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "触发顺控")
    @RequestMapping(value = "/sequential",method = RequestMethod.GET)
    public Result sequential(@RequestParam Map<String,String> meteId){
        Result result=new Result();
        try {
            result.setData(tSequentialConfService.sequential(meteId.get("meteId")));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "顺控信息")
    @RequestMapping(value = "/sequentialInfo",method = RequestMethod.GET)
    public Result sequentialInfo(@RequestParam(value = "cfgDeviceId",required = false) String cfgDeviceId){
        Result result=new Result();
        try {
            result.setData(tSequentialConfService.sequentialInfo(cfgDeviceId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


}
