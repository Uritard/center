package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.configuration.UserManager;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.SysRoleMenuDao;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.entity.SysRoleMenu;
import com.yjh.platform.module.user.entity.SysUserLogin;
import com.yjh.platform.module.user.service.SysUserService;
import com.yjh.platform.module.user.entity.SysUser;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

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
            if (sysUser.getState()==-1) { sysUser.setState(null); }
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
            if (userMap.size()>0 && !Objects.equals(null, userMap.get("userName")) && !Objects.equals(null, userMap.get("password"))) {
                String userName = userMap.get("userName");
                String password = userMap.get("password");
                SysUserLogin sysUserLogin = this.sysUserService.userLogin(userName, password);
                log.info("sysUserLogin: "+sysUserLogin);
                if (!Objects.equals(null, sysUserLogin)) {
                    String appKey = getRandomNickname(10);
                    sysUserLogin.setAppkey(appKey);
                    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                    params.set("logType", "iot-center-platform:module");
                    params.set("ip", request.getRequestURI());
                    params.set("title", "登录");
                    params.set("state", 1);
                    params.set("userId", sysUserLogin.getUserId());
                    params.set("userName", userName);
                    params.set("content", "用户登录");
                    LogsAspect logsAspect = new LogsAspect();
                    logsAspect.post(params);
                    if (sysUserLogin.getState()==2) {
                        Map<String, Object> mapResult = new HashMap<>();
                        mapResult.put("code", ResultCodeEnum.CODE10102.getCode());
                        mapResult.put("info", ResultCodeEnum.CODE10102.getName());
                        result.setData(mapResult);
                        return result;
                    }
                    if (sysUserLogin.getState()==0) {
                        Map<String, Object> mapResult = new HashMap<>();
                        mapResult.put("code", ResultCodeEnum.CODE10101.getCode());
                        mapResult.put("info", ResultCodeEnum.CODE10101.getName());
                        result.setData(mapResult);
                        return result;
                    }
                    if (sysUserLogin.getState()==1) {
                        Map<String, Object> mapResult = new HashMap<>();
                        List<String> sysRoleMenuList = sysRoleMenuDao.selectByRoleId(sysUserLogin.getRoleId());
                        mapResult.put("roleMenuList", sysRoleMenuList);
                        mapResult.put("sysUserLogin", sysUserLogin);
                        result.setData(mapResult);
                        String userId = String.valueOf(sysUserLogin.getUserId());
                        Map<String, Object> mapAccount = new HashMap<>();
                        Map<String, Object> mapAppKey = new HashMap<>();
                        mapAccount.put("userId", userId);
                        mapAccount.put("userName", userName);
                        mapAccount.put("roleId", String.valueOf(sysUserLogin.getRoleId()));
                        mapAccount.put("appKey", appKey);
                        mapAccount.put("expireTime", String.valueOf(System.currentTimeMillis()));
                        mapAccount.put("errorInputTimes", "0");
                        String key = Constant.account_lock_times.replace("userAccountID", userId);
                        redisTemplate.opsForHash().putAll(key, mapAccount);
                        mapAppKey.put("userId", userId);
                        mapAppKey.put("userName", userName);
                        mapAppKey.put("roleId", String.valueOf(sysUserLogin.getRoleId()));
                        mapAppKey.put("appKey", appKey);
                        mapAppKey.put("expireTime", String.valueOf(System.currentTimeMillis()));
                        redisTemplate.opsForHash().putAll("appKey:"+appKey, mapAppKey);
                    }
                } else {
                    List<SysUser> sysUserList = this.sysUserService.selectByUserNameTotal(userMap.get("userName"));
                    log.info("sysUserList: "+sysUserList);
                    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                    params.set("logType", "iot-center-platform:module");
                    params.set("ip", request.getRequestURI());
                    params.set("title", "登录");
                    params.set("state", 1);
                    if (sysUserList.size()==0) {params.set("userId", "");} else {params.set("userId", sysUserList.get(0).getUserId());}
                    params.set("userName", userName);
                    params.set("content", "用户名或密码错误登录失败");
                    LogsAspect logsAspect = new LogsAspect();
                    logsAspect.post(params);
                    if (sysUserList.size()==0) {
                        Map<String, Object> mapResult = new HashMap<>();
                        mapResult.put("info", ResultCodeEnum.CODE10101.getName());
                        mapResult.put("code", ResultCodeEnum.CODE10101.getCode());
                        result.setData(mapResult);
                        return result;
                    }
                    SysUser sysUser = sysUserList.get(0);
                    String userId = String.valueOf(sysUser.getUserId());
                    String key = Constant.account_lock_times.replace("userAccountID", userId);
                    Integer errorInputTimes = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get(key, "errorInputTimes")));
                    Long expireTime = Long.valueOf(String.valueOf(redisTemplate.opsForHash().get(key, "expireTime")));
                    errorInputTimes +=1;
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", userId);
                    map.put("userName", userName);
                    map.put("roleId", String.valueOf(sysUser.getRoleId()));
                    map.put("expireTime", String.valueOf(System.currentTimeMillis()));
                    map.put("errorInputTimes", String.valueOf(errorInputTimes));
                    redisTemplate.opsForHash().putAll(key, map);
                    if (errorInputTimes>=3 && System.currentTimeMillis()-expireTime<=1800000) {
                        sysUser.setState(2);
                        this.sysUserService.update(sysUser);
                    }
                    Map<String, Object> mapResult = new HashMap<>();
                    mapResult.put("code", ResultCodeEnum.CODE10101.getCode());
                    mapResult.put("info", ResultCodeEnum.CODE10101.getName());
                    mapResult.put("errorCount", "已输入错误"+String.valueOf(errorInputTimes)+"次！");
                    result.setData(mapResult);
                    return result;
                }
            } else {
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("code", ResultCodeEnum.CODE10103.getCode());
                mapResult.put("info", ResultCodeEnum.CODE10103.getName());
                result.setData(mapResult);
                return result;
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(),e.getMessage());
            log.error("登录失败:", e);
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
            String password = map.get("password");
            if (sysUserCurrent.getRoleId() == 1234 && password.equals(sysUserCurrent.getPassword())) {
                result.setData(this.sysUserService.unlockUserAccount(map));
                Map<String, Object> mapCache = new HashMap<>();
                mapCache.put("userId", map.get("lockedUserId"));
                mapCache.put("expireTime", String.valueOf(System.currentTimeMillis()));
                mapCache.put("errorInputTimes", "0");
                String key = Constant.account_lock_times.replace("userAccountID", map.get("lockedUserId"));
                redisTemplate.opsForHash().putAll(key, mapCache);
            } else if (!password.equals(sysUserCurrent.getPassword())){
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("code", ResultCodeEnum.CODE10106.getCode());
                mapResult.put("info", ResultCodeEnum.CODE10106.getName());
                result.setData(mapResult);
            } else if (sysUserCurrent.getRoleId() != 1234){
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
    public Result changePassword(HttpServletRequest httpServletRequest, @RequestBody Map<String, String> map) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(httpServletRequest.getHeader("userId"));
            SysUser sysUserCurrent = sysUserService.selectByPrimaryId(userId);
            String oldPassword = map.get("oldPassword");
            if (sysUserCurrent.getPassword().equals(oldPassword)) {
                result.setData(sysUserService.changePassword(userId, map));
            } else {
                Map<String, Object> mapResult = new HashMap<>();
                mapResult.put("code", ResultCodeEnum.CODE10106.getCode());
                mapResult.put("info", ResultCodeEnum.CODE10106.getName());
                result.setData(mapResult);}
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
