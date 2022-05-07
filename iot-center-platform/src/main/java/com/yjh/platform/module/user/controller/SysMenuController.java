package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.service.SysMenuService;
import com.yjh.platform.module.user.entity.SysMenu;

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
@RequestMapping("/sysMenu/v1")
@Api(value = "/sysMenu", description = "菜单表操作接口")
public class SysMenuController {

    @Autowired
    private final SysMenuService sysMenuService;

    private Logger log = LoggerFactory.getLogger(SysMenuController.class);

    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增菜单数据",content = "根据用户传递的参数新增菜单数据",logType = 2)
    public Result insert(@Validated @RequestBody SysMenu sysMenu) {

        Result result = new Result();
        try {
            result.setData(sysMenuService.insert(sysMenu));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加菜单错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除菜单数据",content = "根据用户传递的参数删除菜单数据",logType = 4)
    public Result delete(@RequestParam(value = "menuId", required = true) Long menuId) {
        Result result = new Result();
        try {
            result.setData(sysMenuService.deleteByPrimaryId(menuId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除菜单异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除菜单错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改菜单数据",content = "根据用户传递的参数修改菜单数据",logType = 3)
    public Result update(@Validated @RequestBody SysMenu sysMenu) {
        Result result = new Result();
        try {
            result.setData(sysMenuService.update(sysMenu));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新菜单异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新菜单错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询菜单数据",content = "根据用户传递的参数查询菜单信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "menuId", required = true) Long menuId) {
        Result result = new Result();
        try {
            SysMenu sysMenu = sysMenuService.selectByPrimaryId(menuId);
            result.setData(sysMenu);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询菜单数据",content = "根据用户传递的参数查询菜单信息",logType = 1)
    public Result select(@RequestParam(value = "menuId", required = false) Long menuId,
                         @RequestParam(value = "menuName", required = false) String menuName,
                         @RequestParam(value = "menuCode", required = false) String menuCode,
                         @RequestParam(value = "upId", required = false) Long upId,
                         @RequestParam(value = "iconCode", required = false) String iconCode,
                         @RequestParam(value = "iconUrl", required = false) String iconUrl,
                         @RequestParam(value = "menuType", required = false) Integer menuType,
                         @RequestParam(value = "menuLevel", required = false) Integer menuLevel,
                         @RequestParam(value = "elementCode", required = false) String elementCode,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "sort", required = false) Integer sort,
                         @RequestParam(value = "linkType", required = false) Integer linkType,
                         @RequestParam(value = "url", required = false) String url,
                         @RequestParam(value = "creatorId", required = false) Long creatorId,
                         @RequestParam(value = "sysState", required = false) Integer sysState) {
        Result result = new Result();
        try {
            List<SysMenu> list = sysMenuService.select(menuId, menuName, menuCode, upId, iconCode, iconUrl, menuType, menuLevel, elementCode, state, sort, linkType, url, creatorId, sysState);
            result.setData(list);
            Constant.refreshPacketLog();
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询菜单数据",content = "根据用户传递的参数分页查询菜单信息",logType = 1)
    public Result selectByPage(@RequestBody SysMenu sysMenu) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(sysMenu.getPageNum()!=null?sysMenu.getPageNum():1, sysMenu.getPageSize()!=null?sysMenu.getPageSize():0, true, null, true);
            List<SysMenu> list = sysMenuService.selectByPage(sysMenu);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
