package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.configuration.SecurityProperties;
import com.yjh.platform.configuration.UserManager;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.SysRoleMenuDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.service.SysUserService;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import com.yjh.platform.common.result.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;

import javax.annotation.Resource;
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
    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;


    private Logger log = LoggerFactory.getLogger(SysUserController.class);
    @Autowired
    private RedisTemplate redisTemplate;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @ApiOperation(value = "系统用户表插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增系统用户数据", content = "根据用户传递的参数新增系统用户数据", logType = 2)
    public Result insert(@RequestBody SysUser sysUser) {
        Result result = new Result();
        try {
            Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = map.get("content");
            if ("true".equals(isDecode)) {
                sysUser.setUserName(Demo.decrypt(sysUser.getUserName()));
                sysUser.setPassword(Demo.decrypt(sysUser.getPassword()));
            }
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
    @Logs(title = "删除系统用户数据", content = "根据用户传递的参数删除系统用户数据", logType = 4)
    public Result delete(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            if(userId==10001){
                result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), "此用户为系统管理员,无法删除");
            }
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
    @Logs(title = "修改系统用户数据", content = "根据用户传递的参数修改系统用户数据", logType = 3)
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
    @Logs(title = "查询系统用户数据", content = "根据用户传递的参数查询系统用户信息", logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            SysUserSelect sysUser = sysUserService.selectByPrimaryIds(userId);
            result.setData(sysUser);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "用户状态查询")
    @RequestMapping(value = "/selectByUserState", method = RequestMethod.GET)
    @Logs(title = "查询系统用户数据", content = "根据用户传递的参数查询系统用户信息", logType = 1)
    public Result selectByUserState(@RequestParam(value = "state", required = true) Integer state) {
        Result result = new Result();
        try {
            List<SysUserSelect> sysUser = sysUserService.selectByUserState(state);
            result.setData(sysUser);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "用户名查询")
    @RequestMapping(value = "/selectByUserName", method = RequestMethod.GET)
    @Logs(title = "查询系统用户数据", content = "根据用户传递的参数查询系统用户信息", logType = 1)
    public Result selectByUserName(@RequestParam(value = "userName", required = true) String userName) {
        Result result = new Result();
        try {
            Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = map.get("content");
            if ("true".equals(isDecode)) {
                userName = Demo.decrypt(userName);
                List<SysUserSelect> sysUsers = sysUserService.selectByUserName(userName);
                result.setData(sysUsers);
            } else {
                List<SysUserSelect> sysUsers = sysUserService.selectByUserName(userName);
                result.setData(sysUsers);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "系统用户表查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询系统用户数据", content = "根据用户传递的参数查询系统用户信息", logType = 1)
    public Result select(@RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "password", required = false) String password,
                         @RequestParam(value = "trueName", required = false) String trueName,
                         @RequestParam(value = "userType", required = false) Integer userType,
                         @RequestParam(value = "sex", required = false) Integer sex,
                         @RequestParam(value = "eMail", required = false) String eMail,
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
                         @RequestParam(value = "invalidTime", required = false) Integer invalidTime,
                         @RequestParam(value = "lastLogin", required = false) Date lastLogin) {
        Result result = new Result();
        try {
            Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = map.get("content");
            if ("true".equals(isDecode)) {
                userName = Demo.decrypt(userName);
                password = Demo.decrypt(password);
                List<SysUserSelect> list = sysUserService.select(userId, userName, password, trueName, userType, sex, eMail, workNo, faceId, fingerId, voiceId, state, userTitle, creatorId, appkey, imageUrl, roleId, orgId, userStatus, createTime, updateTime, invalidTime, lastLogin);
                result.setData(list);
            } else {
                List<SysUserSelect> list = sysUserService.select(userId, userName, password, trueName, userType, sex, eMail, workNo, faceId, fingerId, voiceId, state, userTitle, creatorId, appkey, imageUrl, roleId, orgId, userStatus, createTime, updateTime, invalidTime, lastLogin);

                result.setData(list);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "系统用户表分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询系统用户数据", content = "根据用户传递的参数分页查询系统用户信息", logType = 1)
    public Result selectByPage(@RequestBody SysUser sysUser
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(sysUser.getPageNum() != null ? sysUser.getPageNum() : 1, sysUser.getPageSize() != null ? sysUser.getPageSize() : 0, true, null, true);
            if (sysUser.getState() == -1) {
                sysUser.setState(null);
            }
            Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = map.get("content");
            if ("true".equals(isDecode)) {
                sysUser.setUserName(Demo.decrypt(sysUser.getUserName()));
                sysUser.setPassword(Demo.decrypt(sysUser.getPassword()));
                List<Map<String, String>> list = sysUserService.selectByPage(sysUser);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            } else {
                List<Map<String, String>> list = sysUserService.selectByPage(sysUser);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据用户ID查询关联的组织结构信息")
    @RequestMapping(value = "/selectRelationOrg", method = RequestMethod.GET)
    @Logs(title = "根据用户ID查询关联的组织结构信息", content = "根据用户传递的参数查询角色关联组织机构", logType = 1)
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
    @Logs(title = "根据用户ID查询关联菜单权限", content = "根据用户传递的参数查询角色关联菜单权限", logType = 1)
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
    @Logs(title = "根据用户ID查询关联区域设备权限", content = "根据用户传递的参数查询角色关联设备权限", logType = 1)
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
            Map resultHasLogin = this.sysUserService.userLogin(request, userMap);
            result.setData(resultHasLogin);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("登录失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "用户登出")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @Logs(title = "用户登出", content = "用户登出", logType = 5)
    public Result userLogout(HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            String token = request.getHeader("token");
            result.setData(this.sysUserService.userLogout(userId, token));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("登出失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "添加用户")
    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @Logs(title = "新增用户", content = "根据用户传递的参数新增数据", logType = 2)
    public Result insertUser(@Validated @RequestBody SysUser sysUser) {

        Result result = new Result();
        try {
            Long creatorId = userManager.getCreatorId();
            sysUser.setCreatorId(creatorId);
            // sysUser.setPassword("Yjh@123!");
            Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = map.get("content");
            String PW_PATTERN = "^(?![A-Za-z0-9]+$)(?![A-Za-z\\W]+$)(?![0-9\\W]+$)[a-zA-Z0-9\\W]{8,}$";
            if ("true".equals(isDecode)) {
                sysUser.setUserName(Demo.decrypt(sysUser.getUserName()));
                sysUser.setPassword(Demo.decrypt(sysUser.getPassword()));
            }
            if (sysUser.getUserName().contains("admin") || sysUser.getUserName().contains("administrator")) {
                result.setCode(ResultCodeEnum.CODE20017.getCode(), ResultCodeEnum.CODE20017.getName());
            } else if (sysUser.getPassword().contains(sysUser.getUserName())) {
                result.setCode(ResultCodeEnum.CODE20017.getCode(), ResultCodeEnum.CODE20017.getName());
            } else if (!sysUser.getPassword().matches(PW_PATTERN)) {
                result.setCode(ResultCodeEnum.CODE20017.getCode(), ResultCodeEnum.CODE20017.getName());
            } else {
                sysUserService.insert(sysUser);
                sysUserService.insertIntoRedis();
                result.setData(sysUser);
            }
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
    @Logs(title = "用户帐号解锁", content = "用户账号解锁", logType = 5)
    public Result unlockUserAccount(HttpServletRequest httpServletRequest, @RequestBody Map<String, String> map) {
        Result result = new Result();
        try {
            Long userId =Long.valueOf(httpServletRequest.getHeader("userId"));
            SysUser sysUserCurrent = sysUserService.selectByPrimaryId(userId);
            Map<String, String> maps = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = maps.get("content");
            String password = null;
            String locked=null;
            if ("true".equals(isDecode)) {
                password = Demo.decrypt(map.get("password"));
                locked = Demo.decrypt(map.get("lockedUserId"));
            } else {
                password = map.get("password");
                locked =map.get("lockedUserId");
            }
            if (sysUserCurrent.getRoleId() == 1234 && password.equals(Demo.decryptDB(sysUserCurrent.getPassword()))) {
                result.setData(this.sysUserService.unlockUserAccount(map));
                Map<String, Object> mapCache = new HashMap<>();
                mapCache.put("userId", locked);
                mapCache.put("expireTime", String.valueOf(System.currentTimeMillis()));
                mapCache.put("errorInputTimes", "0");
                String key = Constant.account_lock_times.replace("userAccountID",locked);
                redisTemplate.opsForHash().putAll(key, mapCache);
            } else if (!password.equals(Demo.decryptDB(sysUserCurrent.getPassword()))) {
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("code", ResultCodeEnum.CODE10106.getCode());
                mapResult.put("info", ResultCodeEnum.CODE10106.getName());
                result.setData(mapResult);
            } else if (sysUserCurrent.getRoleId() != 1234) {
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("code", ResultCodeEnum.CODE10008.getCode());
                mapResult.put("info", ResultCodeEnum.CODE10008.getName());
                result.setData(mapResult);
            }
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
    @Logs(title = "用户修改密码", content = "用户修改密码", logType = 3)
    public Result changePassword(HttpServletRequest httpServletRequest, @RequestBody Map<String, String> map) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(httpServletRequest.getHeader("userId"));
            SysUser sysUserCurrent = sysUserService.selectByPrimaryId(userId);
            String oldPassword = null;
            String newPassword = null;
            Map<String, String> enmap = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = enmap.get("content");
            if ("true".equals(isDecode)) {
                oldPassword = Demo.decrypt(map.get("oldPassword"));
                newPassword = Demo.decrypt(map.get("newPassword"));
            } else {
                oldPassword = map.get("oldPassword");
                newPassword = map.get("newPassword");
            }
            String PW_PATTERN = "^(?![A-Za-z0-9]+$)(?![A-Za-z\\W]+$)(?![0-9\\W]+$)[a-zA-Z0-9\\W]{8,}$";
            if (Demo.decryptDB(sysUserCurrent.getPassword()).equals(oldPassword)) {
                if (Demo.decryptDB(sysUserCurrent.getPassword()).equals(newPassword)) {
                    result.setCode(ResultCodeEnum.CODE20017.getCode(), ResultCodeEnum.CODE20017.getName());
                    return result;
                } else if (newPassword.contains(sysUserCurrent.getUserName())) {
                    result.setCode(ResultCodeEnum.CODE20017.getCode(), ResultCodeEnum.CODE20017.getName());
                    return result;
                } else if (!newPassword.matches(PW_PATTERN)) {
                    result.setCode(ResultCodeEnum.CODE20017.getCode(), ResultCodeEnum.CODE20017.getName());
                    return result;
                } else {
                    result.setData(sysUserService.changePassword(userId, map, sysUserCurrent.getUserName()));
                }
            } else {
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("code", ResultCodeEnum.CODE10106.getCode());
                mapResult.put("info", ResultCodeEnum.CODE10106.getName());
                result.setData(mapResult);
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("用户修改密码异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("用户修改密码错误:", e);
        }
        return result;
    }

    /**
     * java生成随机数字10位数
     */
    public static String getRandomNickname(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }


}
