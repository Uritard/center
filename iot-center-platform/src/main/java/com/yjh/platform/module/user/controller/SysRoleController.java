package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.service.SysRoleService;
import com.yjh.platform.module.user.entity.SysRole;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
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
    public Result insert(@RequestBody SysRole sysRole) {
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
    public Result update(@RequestBody SysRole sysRole) {
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
    public Result selectByPage(@RequestBody SysRole sysRole,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
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
    public Result updateRoleMenuRight(@RequestBody Map<String, Object> req) {
        Result result = new Result();
        try {
            result.setData(sysRoleService.updateRoleMenuRight(req));
        } catch (Exception e) {
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
