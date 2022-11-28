package com.yjh.platform.module.patrol.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskStatus;
import com.yjh.platform.module.patrol.service.UPatrolDataResultService;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalyzeMeteInfo;
import com.yjh.platform.module.task.entity.FirAndPicInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author czh
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/uPatrolDataResult/v1")
@Api(value = "/deviceStaticsInfo", tags = {"巡检设备统计信息上报处理接口"})
public class UPatrolDeviceStaticsInfoController {


    private Logger log = LoggerFactory.getLogger(UPatrolDeviceStaticsInfoController.class);

    @Autowired
    private UPatrolDataResultService uPatrolDataResultService;
    @Autowired
    private TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdRegionService tStdRegionService;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private LogsRecord logsRecord;


    @ApiOperation(value = "巡视上报设备信息处理")
    @GetMapping(value = "/dealDeviceStaticsInfo")
    public Result selectCruiseResultAnalyze(@RequestBody List<Map<String,String>> statusList) {
        Result result = new Result();
        try {
            log.info("The statusList from accessRobot is=={}", statusList);
//            uPatrolTaskService.robotPatrolTaskStatus(statusList);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("接收并处理机器人/无人机/边缘节点任务状态错误:", e);
        }
        return result;
    }
}
