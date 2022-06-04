package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.service.SysRoleRegionService;
import com.yjh.platform.module.user.entity.SysRoleRegion;
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
 * @since 2020-07-31
 */
@RestController
@RequestMapping("/sysRoleRegion/v1")
@Api(value = "/sysRoleRegion", description = "角色和机器人关联表操作接口")
public class SysRoleRegionController {

    @Autowired
    private final SysRoleRegionService sysRoleRegionService;

    private Logger log = LoggerFactory.getLogger(SysRoleRegionController.class);

    public SysRoleRegionController(SysRoleRegionService sysRoleRegionService) {
        this.sysRoleRegionService = sysRoleRegionService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增角色和机器人关联数据",content = "根据用户传递的参数新增角色和机器人关联数据",logType = 2)
    public Result insert(@Validated @RequestBody  SysRoleRegion sysRoleRegion) {

        Result result = new Result();
        try {
            result.setData(sysRoleRegionService.insert(sysRoleRegion));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加区域角色关系错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除角色和机器人关联数据",content = "根据用户传递的参数删除角色和机器人关联数据",logType = 4)
    public Result delete(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            result.setData(sysRoleRegionService.deleteByPrimaryId(roleId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除区域角色关系异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除区域角色关系错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改角色和机器人关联数据",content = "根据用户传递的参数修改角色和机器人关联数据",logType = 3)
    public Result update(@Validated @RequestBody SysRoleRegion sysRoleRegion) {
        Result result = new Result();
        try {
            result.setData(sysRoleRegionService.update(sysRoleRegion));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新区域角色关系异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新区域角色关系错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询角色和机器人关联数据",content = "根据用户传递的参数查询角色和机器人关联信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            SysRoleRegion sysRoleRegion = sysRoleRegionService.selectByPrimaryId(roleId);
            result.setData(sysRoleRegion);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询角色和机器人关联数据",content = "根据用户传递的参数查询角色和机器人关联信息",logType = 1)
    public Result select(@RequestParam(value = "roleId", required = false) Long roleId,
                            @RequestParam(value = "regionId", required = false) Long regionId) {
        Result result = new Result();
        try {
            List<SysRoleRegion> list = sysRoleRegionService.select(roleId, regionId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询角色和机器人关联数据",content = "根据用户传递的参数分页查询角色和机器人关联信息",logType = 1)
    public Result selectByPage(@RequestBody SysRoleRegion sysRoleRegion ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(sysRoleRegion.getPageNum()!=null?sysRoleRegion.getPageNum():1, sysRoleRegion.getPageSize()!=null?sysRoleRegion.getPageSize():0,true,null,true);
            List<SysRoleRegion> list = sysRoleRegionService.selectByPage(sysRoleRegion);
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
    @Logs(title = "批量插入角色和机器人关联数据",content = "根据用户传递的参数批量插入角色和机器人关联数据",logType = 2)
    public Result batchInsert(@RequestBody List<SysRoleRegion> list) {
        Result result = new Result();
        try {
            result.setData(sysRoleRegionService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入区域角色关联关系失败：" + e);
            log.error("失败描述：", e);
        }
        return result;
    }

}
