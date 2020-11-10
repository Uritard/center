package com.yjh.accessrobot.module.command.controller;

import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.Analysis;
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
@Api(value = "/command", description = "机器人操作接口")
public class RobotController {

    @Autowired
    private final RobotService robotService;

    private Logger log = LoggerFactory.getLogger(RobotController.class);

    public RobotController(RobotService robotService) {
        this.robotService = robotService;
    }

    @Value("${netty.recognize.port}")
    private int recognizePort;//识别算法端口
    @Value("${netty.ai.port}")
    private int aiPort;//缺陷算法端口

    @ApiOperation(value = "控制接口")
    @RequestMapping(value = "/algorithm", method = RequestMethod.POST)
    public Result feignAlgorithm(@RequestBody Map<String, List<Analysis>> analysisMap) {
        Result result = new Result();
        try {
            if (Objects.isNull(analysisMap.get("list"))) {
                result.setMessage("参数为空");
                return result;
            }
            List<Analysis> analysisList = analysisMap.get("list");
            result.setData(robotService.feignRobotControl(analysisList, recognizePort));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人控制接口调用错误:", e);
        }
        return result;
    }

}
