package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.configuration.UserManager;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.service.SysUserService;
import com.yjh.platform.module.user.service.TCameraOperateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YC
 * @date 2020/8/13 - 13:32
 */
@RestController
@RequestMapping("/sysUserSS/v1")
@Api(value = "/sysUser", description = "摄像机操作接口")
public class TCameraOperateController {

    @Autowired
    private final TCameraOperateService tCameraOperateService;
    @Autowired
    private UserManager userManager;

    private Logger log = LoggerFactory.getLogger(TCameraOperateController.class);

    public TCameraOperateController(TCameraOperateService tCameraOperateService) {
        this.tCameraOperateService = tCameraOperateService;
    }

    //摄像机预置位查询
    @ApiOperation(value = "摄像机预置位查询")
    @RequestMapping(value = "/selectAllByTCPid", method = RequestMethod.GET)
    public Result selectAllByTCPid(@RequestParam(value = "presetId", required = false) Long presetId) {
        Result result = new Result();
        try {
            List<TCameraPreset> list = this.tCameraOperateService.selectAllByTCPid(presetId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    //摄像头预置位删除
    @ApiOperation(value = "摄像机预置位查询")
    @RequestMapping(value = "/deleteByTCPid", method = RequestMethod.GET)
    public Result deleteByTCPid(@RequestParam(value = "presetId", required = false) Long presetId) {
        Result result = new Result();
        try {
            result.setData(tCameraOperateService.deleteByTCPid(presetId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除摄像机异常:", e);
        } catch (Exception e) {
             result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
             log.error("删除摄像机错误:", e);
        }
            return result;
        }
    @ApiOperation(value = "摄像机预置位插入")
    @RequestMapping(value = "/addTCP", method = RequestMethod.POST)
    public Result insertTCP(@RequestBody TCameraPreset tCameraPreset) {
        Result result = new Result();
        try {
            result.setData(tCameraOperateService.insertTCP(tCameraPreset));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增摄像机错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "摄像机预置位更新")
    @RequestMapping(value = "/updateTCP", method = RequestMethod.PUT)
    public Result updateTCP(@RequestBody TCameraPreset tCameraPreset) {
        Result result = new Result();
        try {
            result.setData(tCameraOperateService.updateTCP(tCameraPreset));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新摄像机异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新摄像机错误:", e);
        }
        return result;
    }
}
