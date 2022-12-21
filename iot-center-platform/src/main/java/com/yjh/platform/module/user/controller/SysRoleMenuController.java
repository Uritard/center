package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.service.SysRoleMenuService;
import com.yjh.platform.module.user.entity.SysRoleMenu;
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
@RequestMapping("/sysRoleMenu/v1")
@Api(value = "/sysRoleMenu", description = "角色菜单表操作接口")
public class SysRoleMenuController {

    @Autowired
    private final SysRoleMenuService sysRoleMenuService;

    private Logger log = LoggerFactory.getLogger(SysRoleMenuController.class);

    public SysRoleMenuController(SysRoleMenuService sysRoleMenuService) {
        this.sysRoleMenuService = sysRoleMenuService;
    }

    @ApiOperation(value = "角色菜单表插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增角色菜单数据",content = "根据用户传递的参数新增角色菜单数据",logType = 2)
    public Result insert(@Validated @RequestBody  SysRoleMenu sysRoleMenu) {

        Result result = new Result();
        try {
            result.setData(sysRoleMenuService.insert(sysRoleMenu));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加角色菜单错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "角色菜单表删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除角色菜单数据",content = "根据用户传递的参数删除角色菜单数据",logType = 4)
    public Result delete(@RequestParam(value = "rpId", required = true) Long rpId) {
        Result result = new Result();
        try {
            result.setData(sysRoleMenuService.deleteByPrimaryId(rpId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除角色菜单异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除角色菜单错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "根据角色ID删除角色菜单")
    @RequestMapping(value = "/deleteByRoleId", method = RequestMethod.POST)
    @Logs(title = "删除角色菜单数据",content = "根据用户传递的参数删除角色菜单",logType = 4)
    public Result deleteByRoleId(@RequestParam(value = "RoleId", required = true) Long RoleId) {
        Result result = new Result();
        try {
            result.setData(sysRoleMenuService.deleteByRoleId(RoleId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("根据角色ID删除角色菜单异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("根据角色ID删除角色菜单错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "角色菜单表更新")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改角色菜单数据",content = "根据用户传递的参数修改角色菜单数据",logType = 3)
    public Result update(@Validated @RequestBody SysRoleMenu sysRoleMenu) {
        Result result = new Result();
        try {
            result.setData(sysRoleMenuService.update(sysRoleMenu));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新角色菜单异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新角色菜单错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "角色菜单表主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询角色菜单数据",content = "根据用户传递的参数查询角色菜单信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "rpId", required = true) Long rpId) {
        Result result = new Result();
        try {
            SysRoleMenu sysRoleMenu = sysRoleMenuService.selectByPrimaryId(rpId);
            result.setData(sysRoleMenu);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "角色菜单表查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询角色菜单数据",content = "根据用户传递的参数查询角色菜单信息",logType = 1)
    public Result select(@RequestParam(value = "rpId", required = false) Long rpId,
                            @RequestParam(value = "menuCode", required = false) String menuCode,
                            @RequestParam(value = "sort", required = false) Integer sort,
                            @RequestParam(value = "elementCode", required = false) String elementCode,
                            @RequestParam(value = "roleId", required = false) Long roleId) {
        Result result = new Result();
        try {
            List<SysRoleMenu> list = sysRoleMenuService.select(rpId, menuCode, sort, elementCode, roleId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "角色菜单表分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询角色菜单数据",content = "根据用户传递的参数分页查询角色菜单信息",logType = 1)
    public Result selectByPage(@RequestBody SysRoleMenu sysRoleMenu ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(sysRoleMenu.getPageNum()!=null?sysRoleMenu.getPageNum():1, sysRoleMenu.getPageSize()!=null?sysRoleMenu.getPageSize():0,true,null,true);
            List<SysRoleMenu> list = sysRoleMenuService.selectByPage(sysRoleMenu);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "角色菜单表批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量插入角色菜单数据",content = "根据用户传递的参数批量插入角色菜单信息",logType = 2)
    public Result batchInsert(@RequestBody List<SysRoleMenu> list) {
        Result result = new Result();
        try {
            result.setData(sysRoleMenuService.batchInsert(list));
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_sys_role_name") != -1) {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), "业务描述");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("角色菜单表批量失败描述：", e);
        }
        return result;
    }



    @ApiOperation(value = "根据角色查询菜单权限树")
    @GetMapping(value = "/selectRoleMenuTree/{roleId}")
    @Logs(title = "根据角色查询菜单权限树",content = "根据角色查询菜单权限树",logType = 2)
    public Result selectRoleMenuTree(@PathVariable("roleId")Long roleId) {
        Result result = new Result();
        try {
            result.setData(sysRoleMenuService.selectRoleMenuTree(roleId));
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_sys_role_name") != -1) {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), "业务描述");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("角色菜单表批量失败描述：", e);
        }
        return result;
    }



}
