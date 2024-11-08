package com.yjh.platform.module.patrol.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.patrol.service.VoiceHttpWarnService;
import com.yjh.platform.module.task.entity.VoiceAlarm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <功能描述>
 *
 * 土星声纹厂家上报告警数据
 *
 * @author huyuhang
 * @date 2024/11/7
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RestController
@RequestMapping("api/v1")
@Api(value = "/", tags = "接受http声纹告警")
public class VoiceHttpWarnController {

    @Resource
    private VoiceHttpWarnService voiceHttpWarnService;

    @ApiOperation(value = "声纹告警添加")
    @PostMapping(value = "/alarm")
    @Logs(title = "新增声纹告警信息数据",content = "根据用户传递的参数新增声纹告警信息数据",logType =2)
    public Result voiceAlarm(@Validated @RequestBody VoiceAlarm voiceAlarm) {
        Result result = new Result();
        try {
            result.setData(voiceHttpWarnService.alarm(voiceAlarm));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }
}
