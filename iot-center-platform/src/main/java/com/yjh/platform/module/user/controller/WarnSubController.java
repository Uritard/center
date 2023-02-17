package com.yjh.platform.module.user.controller;


import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.WarnSub;
import com.yjh.platform.module.user.service.WarnSubService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 告警订阅信息 前端控制器
 * </p>
 *
 * @author lqh
 * @since 2022-08-26
 */
@RestController
@Slf4j
@RequestMapping("/warn-sub/v1")
public class WarnSubController {

    @Autowired
    private WarnSubService warnSubService;

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增告警订阅信息",content = "根据用户传递的参数新增新增告警订阅信息",logType = 2,authority = "1235")
    public Result insert(@Validated @RequestBody WarnSub warnSub) {
        Result result = new Result();
        try {
            int res= warnSubService.add(warnSub);
            result.setData(res);
        }catch (Exception e)
        {
            log.error("新增告警订阅失败：",e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
    @ApiOperation(value = "查询")
    @RequestMapping(value = "/selectById", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询",logType = 4,authority = "1235")
    public Result selectByUserId(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
           result.setData(warnSubService.selectByUserId(userId));
        } catch (Exception e) {
            log.error("查询告警订阅失败：",e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/isPop", method = RequestMethod.GET)
    @Logs(title = "查询",content = "根据用户传递的参数查询",logType = 4,authority = "1235")
    public Result isPop(@RequestParam(value = "userId", required = true) Long userId,
                         @RequestParam(value = "type", required = true) String type,
                         @RequestParam(value = "warnId", required = true) Long warnId,
                         @RequestParam(value = "level", required = true) String level) {
        Result result = new Result();
        try {
            result.setData(warnSubService.select(userId,type,level,warnId));
        } catch (Exception e) {
            log.error("查询告警订阅失败：",e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

}
