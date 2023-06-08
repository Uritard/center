package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.TSequentialConf;
import com.yjh.platform.module.user.service.TSequentialConfService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lqh
 * @since 2021-01-21
 */
@RestController
@RequestMapping("/tSequentialConf/v1")
@Api(value = "/tSequentialConf", tags = "顺控配置表操作接口")
public class TSequentialConfController {

    @Autowired
    private final TSequentialConfService tSequentialConfService;
    @Autowired
    private ApplicationProperties applicationProperties;

    private final Logger log = LoggerFactory.getLogger(TSequentialConfController.class);

    public TSequentialConfController(TSequentialConfService tSequentialConfService) {
        this.tSequentialConfService = tSequentialConfService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "新增顺控配置信息",content = "根据用户传递的参数新增顺控配置信息",logType = 2,authority = "1234")
    public Result add(@Validated @RequestBody TSequentialConf tSequentialConf) {
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
    @PostMapping(value = "/delete")
    @Logs(title = "删除顺控配置信息",content = "根据用户传递的参数删除顺控配置信息",logType = 4,authority = "1234")
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
    @PostMapping(value = "/update")
    @Logs(title = "修改顺控配置信息",content = "根据用户传递的参数修改顺控配置信息",logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody TSequentialConf tSequentialConf) {
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
    @GetMapping(value = "/selectByPrimaryId")
    @Logs(title = "查询顺控配置信息",content = "根据用户传递的参数查询顺控配置信息",logType = 1,authority = "1234")
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
    @GetMapping(value = "/select")
    @Logs(title = "查询顺控配置信息",content = "根据用户传递的参数查询顺控配置信息",logType = 1)
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
    @GetMapping(value = "/selectByPage")
    @Logs(title = "查询顺控配置信息",content = "根据用户传递的参数分页查询顺控配置信息",logType = 1,authority = "1234")
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
    @PostMapping(value = "/batchAdd")
    @Logs(title = "批量插入顺控配置信息",content = "根据用户传递的参数批量插入顺控配置信息",logType = 2)
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
    @PostMapping(value = "/batchDelete")
    @Logs(title = "批量删除顺控配置信息",content = "根据用户传递的参数批量删除顺控配置信息",logType = 4)
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
    @GetMapping(value = "/selectForCfgDeviceTree")
    @Logs(title = "查询顺控设备树",content = "根据用户传递的参数查询顺控设备树",logType = 1)
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

    @ApiOperation(value = "触发顺控视频聚焦")
    @GetMapping(value = "/sequential")
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

    @ApiOperation(value = "手动干预一键顺控结果")
    @GetMapping(value = "/sequentialResult")
    public Result sequentialResult(@RequestParam(value = "resultNum", required = false) Integer resultNum){
        Result result=new Result();
        try {
            result.setData(tSequentialConfService.setSequentialResult(resultNum));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "手动干预一静默监视结果")
    @GetMapping(value = "/silentMonitorResult")
    public Result silentMonitorResult(@RequestParam(value = "type", required = false) Integer type){
        Result result=new Result();
        try {
            result.setData(tSequentialConfService.setSilentMonitorResult(type));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "触发顺控抓图识别")
    @GetMapping(value = "/sequentialRec")
    public Result sequentialRec(@RequestParam Map<String,String> meteId){
        Result result=new Result();
        try {
            result.setData(tSequentialConfService.sequentialRec(meteId.get("meteId")));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "触发顺控抓图识别返回值处理")
    @PostMapping(value = "/sequentialRecBack")
    public Result sequentialRecBack(@RequestBody Map<String,String> recBack){
        Result result=new Result();
        try {
            log.info("一键顺控：{}", recBack);
            /*// 测试数据
            recBack.put("meteId","1001");
            recBack.put("code","200");
            recBack.put("desc","合");*/
            if(applicationProperties.getSequentialConfig().getSequentialResultFlag() != null &&
                    "1".equals(applicationProperties.getSequentialConfig().getSequentialResultFlag())){
                recBack.put("code","200");
                recBack.put("desc",String.valueOf(Constant.sequentialState.get("meteResult")));
            }
            result.setData(tSequentialConfService.sequentialRecBack(recBack));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "顺控信息")
    @GetMapping(value = "/sequentialInfo")
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

    @ApiOperation(value = "反向联动任务下发")
    @GetMapping(value = "/unionTask")
    public Result unionTask(@RequestParam(value = "cfgDeviceId",required = false) String cfgDeviceId,
                            @RequestParam(value = "order",required = false) String order){
        Result result=new Result();
        try {
            result.setData(tSequentialConfService.unionTask(cfgDeviceId,order));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "获取顺控处于那一步")
    @GetMapping(value = "/getStep")
    public Result getStep(){
        Result result=new Result();
        try {
            Integer step = (Integer) Constant.sequentialState.get("state");
            if(step == null){
                Constant.sequentialState.put("state",-1);
                Constant.sequentialState.put("cfgDeviceId","");
                result.setData(Constant.sequentialState);
            }else {
                result.setData(Constant.sequentialState);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
