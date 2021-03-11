package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.service.SysRoleCameraService;
import com.yjh.platform.module.user.entity.SysRoleCamera;

import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;


/**
 * @author tt
 * @since 2020-07-23
 */
@RestController
@RequestMapping("/sysRoleCamera/v1")
@Api(value = "/sysRoleCamera", description = "角色和摄像机关联表操作接口")
public class SysRoleCameraController {

    @Autowired
    private final SysRoleCameraService sysRoleCameraService;

    private Logger log = LoggerFactory.getLogger(SysRoleCameraController.class);

    public SysRoleCameraController(SysRoleCameraService sysRoleCameraService) {
        this.sysRoleCameraService = sysRoleCameraService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增角色和摄像机关联数据",content = "根据用户传递的参数新增角色和摄像机关联数据",logType = 2)
    public Result insert(@Validated @RequestBody SysRoleCamera sysRoleCamera) {

        Result result = new Result();
        try {
            result.setData(sysRoleCameraService.insert(sysRoleCamera));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加相机角色关系错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除角色和摄像机关联数据",content = "根据用户传递的参数删除角色和摄像机关联数据",logType = 4)
    public Result delete(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            result.setData(sysRoleCameraService.deleteByPrimaryId(roleId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除相机角色关系异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除相机角色关系错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改角色和摄像机关联数据",content = "根据用户传递的参数修改角色和摄像机关联数据",logType = 3)
    public Result update(@RequestBody SysRoleCamera sysRoleCamera) {
        Result result = new Result();
        try {
            result.setData(sysRoleCameraService.update(sysRoleCamera));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新相机角色关系异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新相机角色关系错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询角色和摄像机关联数据",content = "根据用户传递的参数查询角色和摄像机关联信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            SysRoleCamera sysRoleCamera = sysRoleCameraService.selectByPrimaryId(roleId);
            result.setData(sysRoleCamera);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询角色和摄像机关联数据",content = "根据用户传递的参数查询角色和摄像机关联信息",logType = 1)
    public Result select(@RequestParam(value = "roleId", required = false) Long roleId,
                         @RequestParam(value = "cameraId", required = false) Long cameraId) {
        Result result = new Result();
        try {
            List<SysRoleCamera> list = sysRoleCameraService.select(roleId, cameraId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询角色和摄像机关联数据",content = "根据用户传递的参数分页查询角色和摄像机关联信息",logType = 1)
    public Result selectByPage(@RequestBody SysRoleCamera sysRoleCamera ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(sysRoleCamera.getPageNum()!=null?sysRoleCamera.getPageNum():1, sysRoleCamera.getPageSize()!=null?sysRoleCamera.getPageSize():0, true, null, true);
            List<SysRoleCamera> list = sysRoleCameraService.selectByPage(sysRoleCamera);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量插入角色和摄像机关联数据",content = "根据用户传递的参数批量插入角色和摄像机关联数据",logType = 2)
    public Result batchInsert(@RequestBody List<SysRoleCamera> list) {
        Result result = new Result();
        try {
            result.setData(sysRoleCameraService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入相机角色关联关系失败：" + e);
            log.error("失败描述：", e);
        }
        return result;
    }

}
