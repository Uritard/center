package com.yjh.accessrobot.module.command.controller;

import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.RobotTaskInstanceInfo;
import com.yjh.accessrobot.module.command.entity.TCruiseResult;
import com.yjh.accessrobot.module.command.service.RobotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/robot/v1")
@Api(value = "/operator", description = "机器人操作接口")
public class RobotController {

    @Autowired
    private final RobotService robotService;
    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(RobotController.class);

    public RobotController(RobotService robotService) { this.robotService = robotService; }

    @ApiOperation(value = "发送控制指令接口")
    @RequestMapping(value = "/command", method = RequestMethod.GET)
    public Result feignRobotControl(HttpServletRequest request,
                                    @RequestParam(value = "robotCode") String robotCode,
                                    @RequestParam(value = "type") String type,
                                    @RequestParam(value = "type") String key,
                                    @RequestParam(value = "command") String command,
                                    @RequestParam(value = "value",required = false) String value,
                                    @RequestParam(value = "direction",required = false) String direction) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            result.setData(robotService.feignRobotControl(robotCode,type,command,value,direction,key,userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "发送模型同步指令接口")
    @RequestMapping(value = "/fileTransfer", method = RequestMethod.GET)
    public Result feignRobotTransfer(@RequestParam(value = "robotCode") String robotCode) {
        Result result = new Result();
        try {
            result.setData(robotService.feignRobotTransfer(robotCode));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发送模型同步接口调用错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "发送任务/联动任务指令接口")
    @RequestMapping(value = "/taskIssued", method = RequestMethod.POST)
    public Result feignRobotTaskIssued(@RequestBody Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap) {
        Result result = new Result();
        try {
            result.setData(robotService.feignRobotTaskIssued(robotTaskInfoMap));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发送任务接口调用错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "发送任务控制指令接口")
    @RequestMapping(value = "/taskControl", method = RequestMethod.POST)
    public Result feignRobotTaskControl(@RequestBody Map<String, Object> robotTaskControlMap) {
        Result result = new Result();
        try {
            result.setData(robotService.feignRobotTaskControl(robotTaskControlMap));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发送任务控制接口调用错误:", e);
        }
        return result;
    }
    /*用完就删*/
    @ApiOperation(value = "方法测试")
    @RequestMapping(value = "/xixixi", method = RequestMethod.POST)
    public Result xixixi(@RequestBody TCruiseResult tCruiseResult){
        Result result = new Result();
        try {
            result.setData(robotService.updateTCruiseResult(tCruiseResult));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }
}
