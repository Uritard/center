package com.yjh.accessrobot.module.command.controller;

import com.yjh.accessrobot.common.smUtil.Demo;
import com.yjh.accessrobot.commons.logs.Logs;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.Analysis;
import com.yjh.accessrobot.module.command.entity.RobotTaskInstanceInfo;
import com.yjh.accessrobot.module.command.entity.TCruiseResult;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    @Logs(title = "控制机器人",content = "根据用户传递的参数控制机器人",logType = 5)
    public Result feignRobotControl(HttpServletRequest request,
                                    @RequestParam(value = "robotCode") String robotCode,
                                    @RequestParam(value = "type") String type,
                                    @RequestParam(value = "command") String command,
                                    @RequestParam(value = "value",required = false) String value,
                                    @RequestParam(value = "key",required = false) String key,
                                    @RequestParam(value = "password",required = false) String password,
                                    @RequestParam(value = "direction",required = false) String direction) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode =map.get("content");
            if("true".equals(isDecode)) {
                password= Demo.decrypt(password);
            }
            result.setData(robotService.feignRobotControl(robotCode,type,command,value,direction,key,userId,password));
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
    @Logs(title = "模型同步",content = "根据用户传递的参数给机器人发送模型同步指令",logType = 5)
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
    @ApiOperation(value = "发送任务指令接口")
    @RequestMapping(value = "/taskIssued", method = RequestMethod.POST)
    @Logs(title = "机器人下发任务",content = "根据用户传递的参数给机器人下发任务",logType = 5)
    public Result feignRobotTaskIssued(@RequestBody Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap) {
        Result result = new Result();
        try {
            robotService.feignRobotTaskIssued(robotTaskInfoMap);
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
    @Logs(title = "机器人任务控制",content = "根据用户传递的参数给机器人发送任务控制指令",logType = 5)
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
    @ApiOperation(value = "站端命令下发")
    @RequestMapping(value = "/upSystemCommand", method = RequestMethod.POST)
    @Logs(title = "站端命令下发",content = "根据用户传递的参数控制机器人",logType = 5)
    public Result upSystemCommand(@RequestBody Map<String,List<XMLBaseModel>> map){
        Result result = new Result();
        try {
            XMLBaseModel xmlBaseModel = map.get("list").get(0);
            log.info("--站端控制数据--"+xmlBaseModel);
            result.setData(robotService.upSystemCommand(xmlBaseModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("站端命令下发错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视主机向机器人下发检修区域指令接口")
    @RequestMapping(value = "/deviceMaintenanceIssued", method = RequestMethod.POST)
    @Logs(title = "下发检修区域",content = "根据用户传递的参数给机器人下发检修区域指令",logType = 5)
    public Result deviceMaintenanceIssued(@RequestBody Map<String,Object> resMap){
        Result result = new Result();
        try {
            result.setData(robotService.deviceMaintenanceIssued(resMap));
            log.info("下发检修区域返回结果: "+result.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡视主机向机器人下发检修区域指令接口发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "巡视主机联动任务下给机器人的指令接口")
    @RequestMapping(value = "/taskIssued2", method = RequestMethod.POST)
    @Logs(title = "给机器人下发联动任务",content = "根据用户传递的参数给机器人下发联动任务指令",logType = 5)
    public Result feignRobotTaskIssued2(@RequestBody Map<String,Object> resMap){
        Result result = new Result();
        try {
            result.setData(robotService.feignRobotTaskIssued2(resMap));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡视主机联动任务下给机器人的指令接口发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "断开连接接口")
    @GetMapping(value = "/removeLink")
    @Logs(title = "断开连接",content = "根据用户传递的参数断开该机器人的连接",logType = 5)
    public Result removeLink(@RequestParam(value = "robotCode")  String robotCode,
                             @RequestParam(value = "robotId")  Long robotId){
        Result result = new Result();
        try {
            log.info("RemoveLink coming......");
            result.setData(robotService.removeLink(robotCode,robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("断开连接接口发生错误:", e);
        }
        return result;
    }
    /*用完就删*/
    @ApiOperation(value = "方法测试")
    @PostMapping(value = "/methodTest")
    public Result methodTest(@RequestParam(value = "taskId",required = false) String taskId){
        Result result = new Result();
        try {
//            result.setData(robotService.methodTest(taskId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("测试接口调用错误:", e);
        }
        return result;
    }

}
