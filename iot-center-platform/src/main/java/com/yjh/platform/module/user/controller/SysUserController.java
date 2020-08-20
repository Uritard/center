package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.configuration.UserManager;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.service.SysUserService;
import com.yjh.platform.module.user.entity.SysUser;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

import javax.servlet.http.HttpServletRequest;


/**
 * @author tt
 * @since 2020-07-23
 */
@RestController
@RequestMapping("/sysUser/v1")
@Api(value = "/sysUser", description = "系统用户表操作接口")
public class SysUserController {

    @Autowired
    private final SysUserService sysUserService;
    @Autowired
    private UserManager userManager;

    private Logger log = LoggerFactory.getLogger(SysUserController.class);

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @ApiOperation(value = "系统用户表插入")
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Result insert(@RequestBody SysUser sysUser) {
        Result result = new Result();
        try {
            result.setData(sysUserService.insert(sysUser));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增用户错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "系统用户表删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            result.setData(sysUserService.deleteByPrimaryId(userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除用户异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除用户错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "系统用户表更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody SysUser sysUser) {
        Result result = new Result();
        try {
            result.setData(sysUserService.update(sysUser));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新用户异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新用户错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "系统用户表主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            SysUser sysUser = sysUserService.selectByPrimaryId(userId);
            result.setData(sysUser);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "用户状态查询")
    @RequestMapping(value = "/selectByUserState", method = RequestMethod.GET)
    public Result selectByUserState(@RequestParam(value ="state",required = true)Integer state){
        Result result =new Result();
        try{
            List<SysUser> sysUser =sysUserService.selectByUserState(state);
            result.setData(sysUser);
        }catch(Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "用户名查询")
    @RequestMapping(value = "/selectByUserName", method = RequestMethod.GET)
    public Result selectByUserName(@RequestParam(value = "userName",required = true)String userName){
        Result result=new Result();
        try{
            List<SysUser> sysUsers =sysUserService.selectByUserName(userName);
            result.setData(sysUsers);
        }catch (Exception e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "系统用户表查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "password", required = false) String password,
                         @RequestParam(value = "trueName", required = false) String trueName,
                         @RequestParam(value = "userType", required = false) Integer userType,
                         @RequestParam(value = "sex", required = false) Integer sex,
                         @RequestParam(value = "eMail", required = false) String eMail,
                         @RequestParam(value = "mobilePhone", required = false) String mobilePhone,
                         @RequestParam(value = "workNo", required = false) String workNo,
                         @RequestParam(value = "faceId", required = false) String faceId,
                         @RequestParam(value = "fingerId", required = false) String fingerId,
                         @RequestParam(value = "voiceId", required = false) String voiceId,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "userTitle", required = false) String userTitle,
                         @RequestParam(value = "creatorId", required = false) Long creatorId,
                         @RequestParam(value = "appkey", required = false) String appkey,
                         @RequestParam(value = "imageUrl", required = false) String imageUrl,
                         @RequestParam(value = "roleId", required = false) Long roleId,
                         @RequestParam(value = "orgId", required = false) Long orgId,
                         @RequestParam(value = "userStatus", required = false) Integer userStatus,
                         @RequestParam(value = "createTime", required = false) Date createTime,
                         @RequestParam(value = "updateTime", required = false) Date updateTime,
                         @RequestParam(value = "invalidTime", required = false) Date invalidTime,
                         @RequestParam(value = "lastLogin", required = false) Date lastLogin) {
        Result result = new Result();
        try {
            List<SysUser> list = sysUserService.select(userId, userName, password, trueName, userType, sex, eMail, mobilePhone, workNo, faceId, fingerId, voiceId, state, userTitle, creatorId, appkey, imageUrl, roleId, orgId, userStatus, createTime, updateTime, invalidTime, lastLogin);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "系统用户表分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody SysUser sysUser,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<Map<String, String>> list = sysUserService.selectByPage(sysUser);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据用户ID查询关联的组织结构信息")
    @RequestMapping(value = "/selectRelationOrg", method = RequestMethod.GET)
    public Result selectRelationOrg(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            SysOrg sysOrg = sysUserService.selectRelationOrg(userId);
            result.setData(sysOrg);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据用户ID查询关联菜单权限")
    @RequestMapping(value = "/selectRelationMenu", method = RequestMethod.GET)
    public Result selectRelationMenu(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            List<String> list = sysUserService.selectRelationMenu(userId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询关联菜单权限失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据用户ID查询关联区域设备权限")
    @RequestMapping(value = "/selectRelationAuthor", method = RequestMethod.GET)
    public Result selectRelationAuthor(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            List<AreaInfo> list = sysUserService.selectRelationAuthor(userId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询关联区域设备权限失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "用户登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result userLogin(HttpServletRequest request, @RequestBody Map<String, String> userMap) {
        Result result = new Result();
        try {
            if (userMap.size()>0) {
                String userName = userMap.get("userName");
                String password = userMap.get("password");
                result.setData(this.sysUserService.userLogin(userName, password));
            } else {result.setData("用户名或者密码为空！");}
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("根据密码登录失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "用户登出")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public Result userLogout(HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            result.setData(this.sysUserService.userLogout(userId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("登出失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "添加用户")
    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    public Result insertUser(@RequestBody SysUser sysUser) {
        Result result = new Result();
        try {
            Long creatorId = userManager.getCreatorId();
            sysUser.setCreatorId(creatorId);
            sysUser.setPassword("123456");
            sysUserService.insert(sysUser);
            result.setData(sysUser);
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_username") != -1) {
                result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), "用户名重复");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("添加用户失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "用户帐号解锁")
    @RequestMapping(value = "/unlockUserAccount", method = RequestMethod.PUT)
    public Result unlockUserAccount(HttpServletRequest httpServletRequest, @RequestBody Map<String, String> map) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(httpServletRequest.getHeader("userId"));
            SysUser sysUserCurrent = sysUserService.selectByPrimaryId(userId);
            if (sysUserCurrent.getRoleId() == 1234) {
                result.setData(this.sysUserService.unlockUserAccount(map));
            } else { result.setData(ResultCodeEnum.CODE10008); }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("用户帐号解锁异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("用户帐号解锁错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "用户修改密码")
    @RequestMapping(value = "/changePassword", method = RequestMethod.PUT)
    public Result changePassword(HttpServletRequest httpServletRequest, @RequestBody Map<String, String> map) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(httpServletRequest.getHeader("userId"));
            SysUser sysUserCurrent = sysUserService.selectByPrimaryId(userId);
            String oldPassword = map.get("oldPassword");
            if (sysUserCurrent.getPassword().equals(oldPassword)) {
                result.setData(sysUserService.changePassword(userId, map));
            } else {result.setData("旧密码输入错误！");}
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("用户修改密码异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("用户修改密码错误:", e);
        }
        return result;
    }

}
