package com.yjh.accessrobot.module.command.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.smUtil.Demo;
import com.yjh.accessrobot.commons.logs.Logs;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.commons.utils.file.FileUtil;
import com.yjh.accessrobot.module.command.entity.EnvDeviceStatus;
import com.yjh.accessrobot.module.command.entity.RobotTaskInstanceInfo;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/robot/v1")
@Api(value = "/operator", tags = "机器人操作接口")
public class RobotController {

    @Autowired
    private final RobotService robotService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Demo demo;

    private Logger log = LoggerFactory.getLogger(RobotController.class);

    public RobotController(RobotService robotService) { this.robotService = robotService; }

    @ApiOperation(value = "发送控制指令接口")
    @GetMapping(value = "/command")
    @Logs(title = "控制机器人",content = "根据用户传递的参数控制机器人",logType = 5, authority = "1235")
    public Result feignRobotControl(HttpServletRequest request,
                                    @RequestParam(value = "robotCode") String robotCode,
                                    @RequestParam(value = "type") String type,
                                    @RequestParam(value = "command") String command,
                                    @RequestParam(value = "value",required = false) String value,
                                    @RequestParam(value = "key",required = false) String key,
                                    @RequestParam(value = "pCode",required = false) String password,
                                    @RequestParam(value = "direction",required = false) String direction,
                                    @RequestParam(value = "content",required = false) String content,
                                    @RequestParam(value = "identifier",required = false) String identifier) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode =map.get("content");
            if("true".equals(isDecode)) {
                password= demo.decryptIdentifier(password,identifier);
                redisTemplate.delete("pubk:" + identifier);
            }
            result.setData(robotService.feignRobotControl(robotCode, type, command, value, direction, key, userId, password, request, content));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "发送模型同步指令接口")
    @GetMapping(value = "/fileTransfer")
    @Logs(title = "模型同步",content = "根据用户传递的参数给机器人发送模型同步指令",logType = 5,authority = "1234")
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
    @PostMapping(value = "/taskIssued")
    @Logs(title = "机器人下发任务",content = "根据用户传递的参数给机器人下发任务",logType = 10)
    public Result feignRobotTaskIssued(@RequestBody Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap) {
        Result result = new Result();
        try {
            log.info("platform传来的robotTaskInfoMap是=={}", robotTaskInfoMap);
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
    @PostMapping(value = "/taskControl")
    @Logs(title = "机器人任务控制",content = "根据用户传递的参数给机器人发送任务控制指令",logType = 11)
    public Result feignRobotTaskControl(@RequestBody Map<String, Object> robotTaskControlMap) {
        Result result = new Result();
        try {
            log.info("platform传来的robotTaskControlMap是=={}", robotTaskControlMap);
            result = robotService.feignRobotTaskControl(robotTaskControlMap);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发送任务控制接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "发送确认消息指令接口")
    @RequestMapping(value = "/sendConfirmMsg", method = RequestMethod.POST)
    @Logs(title = "确认消息",content = "根据用户传递的参数给机器人发送确认消息指令",logType = 5)
    public Result sendConfirmMsg(@RequestBody Map<String,Object> confirmMessageMap) {
        Result result = new Result();
        try {
            result.setData(robotService.sendConfirmMsg(confirmMessageMap));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发送确认消息接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "站端命令下发")
    @PostMapping(value = "/upSystemCommand")
    @Logs(title = "站端命令下发",content = "根据用户传递的参数控制机器人",logType = 5)
    public Result upSystemCommand(@RequestBody Map<String,List<XMLBaseModel>> map){
        Result result = new Result();
        try {
            XMLBaseModel xmlBaseModel = map.get("list").get(0);
            log.info("--站端控制数据--{}",xmlBaseModel);
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
    @PostMapping(value = "/deviceMaintenanceIssued")
    @Logs(title = "下发检修区域",content = "根据用户传递的参数给机器人下发检修区域指令",logType = 5)
    public Result deviceMaintenanceIssued(@RequestBody Map<String,Object> resMap){
        Result result = new Result();
        try {
            log.info("platform传来的map是==={}", resMap);
            result.setData(robotService.deviceMaintenanceIssued(resMap));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("巡视主机向机器人下发检修区域指令接口发生错误:", e);
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
            result.setData(robotService.removeLink(robotCode, robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("断开连接接口发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "上传至ftps服务器去")
    @GetMapping(value = "/uploadFile")
    public Result uploadFile() {
        Result result = new Result();
        try {
            robotService.uploadFile("D:\\code\\qhTest\\66666\\task_file.xml","a/b/c/task_file.xml");
            result.setData(66);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "离线情况同步机器人模型信息-上传文件")
    @PostMapping(value = "/upLoadRobotModel")
    @Logs(title = "导入文件",content = "导入机器人模型信息文件",logType = 8)
    public Result upLoadRobotModel(@RequestParam(value="file", required=false) MultipartFile file,
                                   @RequestParam(value="robotCode", required=false) String robotCode){
        Result result = new Result();
        try {
            result = robotService.upLoadRobotModel(file, robotCode);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("离线情况同步机器人模型文件-上传文件接口发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "离线情况同步机器人设备文件-上传文件")
    @PostMapping(value = "/upLoadRobotDevice")
    @Logs(title = "导入文件",content = "导入机器人设备点位文件",logType = 8)
    public Result upLoadRobotDevice(@RequestParam(value="file", required=false) MultipartFile file,
                                    @RequestParam(value="robotCode", required=false) String robotCode){
        Result result = new Result();
        try {
            robotService.upLoadRobotDevice(file, robotCode);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("离线情况同步机器人模型信息-上传文件接口发生错误:", e);
        }
        return result;
    }


    /**
     * 智能环境设备控制
     * 1.deviceId   环控设备序列号
     * 2.deviceStatus  1. 打开  2.关闭
     * 3. deviceAttr  类型空调  1.制冷  2. 制热
     * 4.deviceType 环控设备类型 type
     * 5.tpe= 23 环境设备开关控制
     */
    @ApiOperation(value = "智能环境设备控制")
    @PostMapping(value = "/envDeviceControl")
    @Logs(title = "智能环境设备控制",content = "根据用户传递的参数控制智能环境设备",logType = 5, authority = "1235")
    public Result envDeviceControl(@RequestBody JSONObject jsonObject) {
        log.info("智能环境设备控制");
        Result result = new Result();
        try {
            if (Objects.nonNull(jsonObject) && jsonObject.size() > 0) {
                String regionId = jsonObject.get("regionId").toString();
                String envDataJson = getRedisMapString("Weather", regionId);
                List<EnvDeviceStatus> weather;
                if (StringUtils.isNotEmpty(envDataJson)) {
                    JSONArray objects = JSONArray.parseArray(envDataJson);
                    weather = objects.toJavaList(EnvDeviceStatus.class);
                    String robotCode = weather.get(0).getRobotCode();
                    String deviceId = jsonObject.getString("deviceId");
                    String deviceStatus = jsonObject.getString("deviceStatus");
                    String deviceAttr = jsonObject.getString("deviceAttr");
                    String deviceType = jsonObject.getString("deviceType");
                    String type = "26";
                    HashMap<String, String> map = new HashMap<>();
                    map.put("robotCode", robotCode);
                    map.put("deviceId", deviceId);
                    map.put("deviceStatus", deviceStatus);
                    map.put("deviceAttr", deviceAttr);
                    map.put("type", type);
                    map.put("deviceType", deviceType);
                    result = robotService.robotControl(map);
                }
            } else {
                result.setCode(ResultCodeEnum.PARAMERROR.getCode(), ResultCodeEnum.PARAMERROR.getName());
                return result;
            }
        } catch (Exception e) {
            log.info("智能环境设备控制异常", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }


    @ApiOperation(value = "低优先级任务继续")
    @GetMapping(value = "/lowTaskGoOn")
    public Result lowTaskGoOn(@RequestParam(value = "tsakId") String tsakId) {
        Result result = new Result();
        try {
            robotService.lowTaskGoOn(tsakId);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败：", e);
        }
        return result;
    }

    /**
     * 获取redis集合值
     *
     * @param key
     * @return
     */
    private <T> T getRedisMapString(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }


    @ApiOperation(value = "测试")
    @GetMapping(value = "/test")
    public Result test(@RequestParam(value = "source") String source,
                       @RequestParam(value = "desc") String desc) {
        Result result = new Result();
        try {
            FileUtil.copyFileUsingStream(source, desc);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("测试失败：", e);
        }
        return result;
    }

}
