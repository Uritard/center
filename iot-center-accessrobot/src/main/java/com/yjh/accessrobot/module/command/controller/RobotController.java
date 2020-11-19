package com.yjh.accessrobot.module.command.controller;

import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;


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

    private Logger log = LoggerFactory.getLogger(RobotController.class);

    public RobotController(RobotService robotService) { this.robotService = robotService; }

    @ApiOperation(value = "发送控制指令接口")
    @RequestMapping(value = "/command", method = RequestMethod.POST)
    public Result feignRobotControl(@RequestBody XMLBaseModel xmlBaseModel) {
        Result result = new Result();
//        log.info("前端送来的xmlBaseModel:"+xmlBaseModel);
        try {
            result.setData(robotService.feignRobotControl(xmlBaseModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "发送模型同步指令接口")
    @RequestMapping(value = "/fileTransfer", method = RequestMethod.POST)
    public Result feignRobotTransfer() {
        Result result = new Result();
        try {
            result.setData(robotService.feignRobotTransfer());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "发送任务/联动任务指令接口")
    @RequestMapping(value = "/taskIssued", method = RequestMethod.POST)
    public Result feignRobotTaskIssued(@RequestBody XMLBaseModel xmlBaseModel) {
        Result result = new Result();
        try {
            result.setData(robotService.feignRobotTaskIssued(xmlBaseModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "发送任务控制指令接口")
    @RequestMapping(value = "/taskControl", method = RequestMethod.POST)
    public Result feignRobotTaskControl(@RequestBody XMLBaseModel xmlBaseModel) {
        Result result = new Result();
        try {
            result.setData(robotService.feignRobotTaskControl(xmlBaseModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }

}
