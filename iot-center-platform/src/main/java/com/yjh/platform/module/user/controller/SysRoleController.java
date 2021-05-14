package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.service.SysRoleService;
import com.yjh.platform.module.user.entity.SysRole;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
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
@RequestMapping("/sysRole/v1")
@Api(value = "/sysRole", description = "角色表操作接口")
public class SysRoleController {

    @Autowired
    private final SysRoleService sysRoleService;

    private Logger log = LoggerFactory.getLogger(SysRoleController.class);

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @ApiOperation(value = "插入用户")
    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @Logs(title = "新增角色数据",content = "根据用户传递的参数新增角色数据",logType = 2)
    public Result insert(@Validated @RequestBody  SysRole sysRole) {

        Result result = new Result();
        try {
            result.setData(sysRoleService.insert(sysRole));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除角色数据",content = "根据用户传递的参数删除角色数据",logType = 4)
    public Result delete(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            result.setData(sysRoleService.deleteByPrimaryId(roleId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("角色删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("角色删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改角色数据",content = "根据用户传递的参数修改角色数据",logType = 3)
    public Result update(@Validated @RequestBody SysRole sysRole) {
        Result result = new Result();
        try {
            result.setData(sysRoleService.update(sysRole));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("角色更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("角色更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询角色数据",content = "根据用户传递的参数查询角色信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            SysRole sysRole = sysRoleService.selectByPrimaryId(roleId);
            result.setData(sysRole);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询角色数据",content = "根据用户传递的参数查询角色信息",logType = 1,authority = "1234")
    public Result select(@RequestParam(value = "roleId", required = false) Long roleId,
                            @RequestParam(value = "roleName", required = false) String roleName,
                            @RequestParam(value = "createTime", required = false) Date createTime,
                            @RequestParam(value = "creatorId", required = false) Long creatorId,
                            @RequestParam(value = "sysState", required = false) Integer sysState) {
        Result result = new Result();
        try {
            List<SysRole> list = sysRoleService.select(roleId, roleName, createTime, creatorId, sysState);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询角色数据",content = "根据用户传递的参数分页查询角色信息",logType = 1)
    public Result selectByPage(@RequestBody SysRole sysRole ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(sysRole.getPageNum()!=null?sysRole.getPageNum():1, sysRole.getPageSize()!=null?sysRole.getPageSize():0,true,null,true);
            List<SysRole> list = sysRoleService.selectByPage(sysRole);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据角色ID查询关联的菜单信息")
    @RequestMapping(value = "/selectRelationMenu", method = RequestMethod.GET)
    @Logs(title = "根据角色ID查询关联的菜单信息",content = "根据用户传递的参数查询角色关联的菜单信息",logType = 1,authority = "1234")
    public Result selectRelationMenu(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            List<String> list = sysRoleService.selectRelationMenu(roleId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据角色ID查询关联的区域信息")
    @RequestMapping(value = "/selectRelationRegion", method = RequestMethod.GET)
    @Logs(title = "根据角色ID查询关联的区域信息",content = "根据用户传递的参数查询角色id关联的区域信息",logType = 1)
    public Result selectRelationRegion(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            List<Map<String, Object>> list = sysRoleService.selectRelationRegion(roleId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据角色ID查询关联的相机信息")
    @RequestMapping(value = "/selectRelationCamera", method = RequestMethod.GET)
    @Logs(title = "根据角色ID查询关联的相机信息",content = "根据用户传递的参数查询角色关联的相机信息",logType = 1)
    public Result selectRelationCamera(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            List<Map<String, Object>> list = sysRoleService.selectRelationCamera(roleId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据角色ID查询关联的设备信息")
    @RequestMapping(value = "/selectRelationDevice", method = RequestMethod.GET)
    @Logs(title = "根据角色ID查询关联的设备信息",content = "根据用户传递的参数查询角色关联的设备信息",logType = 1)
    public Result selectRelationDevice(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            List<Map<String, Object>> list = sysRoleService.selectRelationDevice(roleId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据角色ID查询关联的权限信息")
    @RequestMapping(value = "/selectRelationAuthor", method = RequestMethod.GET)
    @Logs(title = "根据角色ID查询关联的权限信息",content = "根据用户传递的参数查询角色关联的权限信息",logType = 1,authority = "1234")
    public Result selectRelationAuthor(@RequestParam(value = "roleId", required = true) Long roleId) {
        Result result = new Result();
        try {
            List<String> list = sysRoleService.selectRelationAuthor(roleId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询关联权限失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "修改角色区域设备权限")
    @RequestMapping(value = "/updateRoleDeviceRight", method = RequestMethod.POST)
    @Logs(title = "修改角色区域设备权限",content = "根据用户传递的参数修改角色区域树的设备权限",logType = 3)
    public Result updateRoleDeviceRight(@RequestBody Map<String, Object> req) {
        Result result = new Result();
        try {
            result.setData(sysRoleService.updateRoleDeviceRight(req));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "修改角色菜单权限")
    @RequestMapping(value = "/updateRoleMenuRight", method = RequestMethod.POST)
    @Logs(title = "修改角色菜单权限",content = "根据用户传递的参数修改角色菜单",logType = 3)
    public Result updateRoleMenuRight(@RequestBody Map<String, Object> req) {
        Result result = new Result();
        try {
            result.setData(sysRoleService.updateRoleMenuRight(req));
        } catch (BusinessException e) {
            result.setCode(209,e.getMessage());
        }catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_sys_role_name") != -1) {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), "业务描述");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("失败描述：", e);
        }
        return result;
    }

}
