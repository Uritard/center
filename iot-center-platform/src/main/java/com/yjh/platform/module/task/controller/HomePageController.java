package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.service.HomePageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lqh
 * @since 2020/12/14
 */
@RestController
@RequestMapping("/homePage/v1")
@Api(value = "/homePage", description = "首页相关接口")
public class HomePageController {

    private Logger log = LoggerFactory.getLogger(HomePageController.class);

    @Autowired
    private HomePageService homePageService;

    @ApiOperation(value = "巡视任务数据概览")
    @RequestMapping(value = "/taskInfo", method = RequestMethod.GET)
    public Result taskInfo(@RequestParam(value = "date", required = true) Integer date) {
        Result result = new Result();
        try {
            result.setData(homePageService.taskInfo(date));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取巡视任务数据概览错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "统计缺陷")
    @RequestMapping(value = "/countByDefectLevel", method = RequestMethod.GET)
    public Result countByDefectLevel() {
        Result result = new Result();
        try {
            result.setData(homePageService.countByDefectLevel());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取巡视任务数据概览错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "正在执行的任务")
    @RequestMapping(value = "/taskOnExecute", method = RequestMethod.GET)
    public Result taskOnExecute() {
        Result result = new Result();
        try {
            result.setData(homePageService.taskOnExecute());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取正在执行的任务错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "告警级别数据")
    @RequestMapping(value = "/countByAlarmLevel", method = RequestMethod.GET)
    public Result countByAlarmLevel() {
        Result result = new Result();
        try {
            result.setData(homePageService.countByAlarmLevel());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取告警级别数据错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "告警内容数据")
    @RequestMapping(value = "/warnInfo", method = RequestMethod.GET)
    public Result warnInfo(@RequestParam(value = "alarmLevel", required = false) Integer alarmLevel) {
        Result result = new Result();
        try {
            result.setData(homePageService.selectThereWarn(alarmLevel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取告警内容数据错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "机器人数据")
    @RequestMapping(value = "/robotInfoForHomePage", method = RequestMethod.GET)
    public Result robotInfoForHomePage(@RequestParam(value = "robotPosition", required = false) String robotPosition) {
        Result result = new Result();
        try {
            result.setData(homePageService.robotInfoForHomePage(robotPosition));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取机器人数据错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "变电站概况数据")
    @RequestMapping(value = "/stationInfo", method = RequestMethod.GET)
    public Result stationInfo() {
        Result result = new Result();
        try {
            result.setData(homePageService.stationInfo());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取变电站概况数据错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "获取摄像机分组信息")
    @RequestMapping(value = "/getCameraGroupInfo", method = RequestMethod.GET)
    public Result getCameraGroupInfo() {
        Result result = new Result();
        try {
            result.setData(homePageService.getCameraGroupInfo());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取摄像机分组信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取摄像机id信息")
    @RequestMapping(value = "/getCameraIdInfo", method = RequestMethod.GET)
    public Result getCameraIdInfo(@RequestParam(value = "groupId", required = false) Long groupId) {
        Result result = new Result();
        try {
            if(groupId == null){
                result.setData(homePageService.getCameraIdInfo());
            }else {
                result.setData(homePageService.getCameraIdInfoForGroup(groupId));
            }

        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取摄像机id信息错误:", e);
        }
        return result;
    }
}
