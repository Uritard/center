package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TCruisePlanCountByPage;
import com.yjh.platform.module.user.entity.RoutePlan;
import com.yjh.platform.module.user.service.ThreeDimensionalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/8/10
 */
@RestController
@RequestMapping("/threeDimensional/v1")
@Api(value = "threeDimensional",description = "三维融合功能接口")
public class ThreeDimensionalController {

    private final ThreeDimensionalService threeDimensionalService;

    private Logger log = LoggerFactory.getLogger(ThreeDimensionalController.class);

    @Autowired
    public ThreeDimensionalController(ThreeDimensionalService threeDimensionalService) {
        this.threeDimensionalService = threeDimensionalService;
    }

    @ApiOperation(value = "三维浏览-搜索定位聚焦")
    @GetMapping(value = "/searchAndLocationFocus")
    @Logs(title = "三维浏览搜索",content = "根据搜索结果定位聚焦",logType = 1)
    public Result searchAndLocationFocus (@RequestParam(value = "deviceName") String deviceName,
                                          @RequestParam(value = "deviceType") Integer deviceType){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.searchAndLocationFocus(deviceName,deviceType));
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维浏览-搜索定位聚焦发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维浏览-搜索定位聚焦发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维浏览-漫游路径保存")
    @PostMapping(value = "/roamingPathSave")
    @Logs(title = "三维漫游路径保存",content = "根据传入的参数保存沉浸式漫游路径",logType = 1)
    public Result roamingPathSave (@RequestBody List<RoutePlan> routePlanList){
        Result result = new Result();
        try {
            log.info("routePlanList=="+routePlanList);
            result.setData(threeDimensionalService.roamingPathSave(routePlanList));
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维浏览-漫游路径保存发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维浏览-漫游路径保存发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维浏览-漫游路径规划")
    @GetMapping(value = "/roamingPathPlanning")
    @Logs(title = "三维漫游路径规划",content = "根据返回的参数规划沉浸式漫游路径",logType = 1)
    public Result roamingPathPlanning (){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.roamingPathPlanning());
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维浏览-漫游路径规划发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维浏览-漫游路径规划发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维交互-测点信息查看")
    @GetMapping(value = "/viewPointInformation")
    public Result viewPointInformation (@RequestParam(value = "modelName") String modelName){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.viewPointInformation(modelName));
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维交互-测点信息查看发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维交互-测点信息查看发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维交互-测点信息查看2")
    @GetMapping(value = "/viewPointInfo")
    public Result viewPointInfo (@RequestParam(value = "modelName") String modelName){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.viewPointInfo(modelName));
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维交互-测点信息查看发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维交互-测点信息查看发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维交互-告警信息查看")
    @GetMapping(value = "/viewAlarmInfo")
    public Result viewAlarmInfo (@RequestParam(value = "modelName") String modelName){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.viewAlarmInfo(modelName));
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维交互-告警信息查看发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维交互-告警信息查看发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维交互-设备台账详情查看")
    @GetMapping(value = "/viewCameraInfo")
    @Logs(title = "三维设备台账详情查看",content = "设备台账详情查看",logType = 1)
    public Result viewCameraInfo (@RequestParam(value = "modelName") String modelName){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.viewCameraInfo(modelName));
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维交互-设备台账详情查看发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维交互-设备台账详情查看发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维交互-设备台账信息查看")
    @GetMapping(value = "/viewDeviceInfo")
    @Logs(title = "设备台账信息查看",content = "设备台账信息",logType = 1)
    public Result viewDeviceInfo (@RequestParam(value = "modelName") String modelName){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.viewDeviceInfo(modelName));
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维交互-设备台账信息查看发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维交互-设备台账信息查看发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维浏览-告警信息统计")
    @GetMapping(value = "/statisticsAlarmInfo")
    @Logs(title = "三维告警信息统计",content = "统计各种未处理的告警次数",logType = 1)
    public Result statisticsAlarmInfo (){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.statisticsAlarmInfo());
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维浏览-告警信息统计发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维浏览-告警信息统计发生错误");
        }
        return result;
    }
    @ApiOperation(value = "三维浏览-查询所有模型名称和设备名称")
    @GetMapping(value = "/selectAllModelNameAndName")
    @Logs(title = "查询所有模型名称和设备名称",content = "查询所有模型名称和设备名称",logType = 1)
    public Result selectAllModelNameAndName (){
        Result result = new Result();
        try {
            result.setData(threeDimensionalService.selectAllModelNameAndName());
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维浏览-查询所有模型名称发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维浏览-查询所有模型名称发生错误");
        }
        return result;
    }

    @ApiOperation(value = "三维浏览-查询巡检计划")
    @PostMapping(value = "/selectByPlanPage")
    @Logs(title = "三维巡检计划查询",content = "根据三维模版查询巡检计划",logType = 1)
    public Result selectByPlanPage (@RequestBody Map<String, Object> planMap){
        Result result = new Result();
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Page page = PageHelper.startPage((int)planMap.getOrDefault("pageNum", 1), (int)planMap.getOrDefault("pageSize", 0),true,null,true);
            List<TCruisePlanCountByPage> list = threeDimensionalService.selectByPlanPage(planMap);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        }catch (BusinessException e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("三维浏览-搜索定位聚焦发生异常");
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("三维浏览-搜索定位聚焦发生错误");
        }
        return result;
    }
}
