package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.TWiringConfig;
import com.yjh.platform.module.user.entity.TWiringConfigVo;
import com.yjh.platform.module.user.service.TWiringConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author 丫C
 * @since 2023-06-19
 */
@RestController
@RequestMapping("/tWiringConfig/v1")
@Api(value = "/tWiringConfig", tags = "主接线图与设备关联表操作接口")
public class TWiringConfigController {

    private final TWiringConfigService tWiringConfigService;

    private final Logger log = LoggerFactory.getLogger(TWiringConfigController.class);
    private static final String USERID = "userId";

    public TWiringConfigController(TWiringConfigService tWiringConfigService) {
        this.tWiringConfigService = tWiringConfigService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    public Result insert(@RequestBody List<TWiringConfigVo> tWiringConfigList) {
        Result result = new Result();
        try {
            result.setData(tWiringConfigService.insert(tWiringConfigList));
        } catch (BusinessException e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("增加设备与主接线图的关联异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("增加设备与主接线图的关联错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @PostMapping(value = "/delete")
    public Result delete(@RequestParam(value = "wiringConfigId") Long wiringConfigId) {
        Result result = new Result();
        try {
            result.setData(tWiringConfigService.deleteByPrimaryId(wiringConfigId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("取消设备与主接线图的关联异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("取消设备与主接线图的关联错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @PostMapping(value = "/update")
    public Result update(@RequestBody List<TWiringConfig> tWiringConfigList,
                         HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader(USERID));
            result.setData(tWiringConfigService.update(tWiringConfigList, userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("更新设备与主接线图的关联异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新设备与主接线图的关联错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @GetMapping(value = "/selectByPrimaryId")
    public Result selectByPrimaryId(@RequestParam(value = "wiringConfigId") Long wiringConfigId) {
        Result result = new Result();
        try {
            result.setData(tWiringConfigService.selectByPrimaryId(wiringConfigId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "条件查询")
    @GetMapping(value = "/selectByCondition")
    public Result selectByCondition(@RequestParam(value = "regionId", required = false) Long regionId) {
        Result result = new Result();
        try {
            result.setData(tWiringConfigService.selectByCondition(regionId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("条件查询失败描述：", e);
        }
        return result;
    }

}
