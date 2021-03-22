package com.yjh.platform.module.user.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.configuration.RedisAndYxsjUtil;
import com.yjh.platform.configuration.SecurityProperties;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.SysRoleMenuDao;
import com.yjh.platform.module.user.dao.SysUserBackUpDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.dao.SysUserDao;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author tt
 * @since 2020-07-23
 */
@Service
public class SysUserService {
    @Autowired
    private SysUserDao sysUserDao;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SysRoleMenuDao sysRoleMenuDao;
    @Resource
    private SysUserBackUpDao SysUserBackUpDao;

//    @Resource
//    private RedisAndYxsjUtil redisAndYxsjUtil;


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Transactional(rollbackFor = Exception.class)
    public int insert(SysUser sysUser) throws IOException {
        Map map = new LinkedHashMap<>();
        Date date = new Date();
        sysUser.setCreateTime(date);
        sysUser.setUpdateTime(date);
        map.put("userName", sysUser.getUserName());
        map.put("password", sysUser.getPassword());
        sysUser.setPassword(Demo.encryption(sysUser.getPassword()));
        int total = sysUserDao.insert(sysUser);
        SysUserBackUp sysUserBackUp = new SysUserBackUp();
        BeanUtils.copyProperties(sysUser, sysUserBackUp);
        sysUserBackUp.setVerfiCode(Demo.summary(map.toString()));
        SysUserBackUpDao.insert(sysUserBackUp);
        return total;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long userId) {
        redisTemplate.delete("userInfo:" + userId);
        this.SysUserBackUpDao.deleteByPrimaryId(userId);
        return this.sysUserDao.deleteByPrimaryId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(SysUser sysUser) throws IOException {
        Long roleId = sysUser.getRoleId();
        if (roleId != null) {
            redisTemplate.opsForHash().put("userInfo:" + sysUser.getUserId(), "roleId", String.valueOf(sysUser.getRoleId()));
        }
        return this.sysUserDao.update(sysUser);
    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> userLogin(HttpServletRequest request, Map<String, String> userMap) throws ParseException, IOException {
        Map<String, Object> mapResult = new HashMap<>();
        if (userMap.size() > 0 && !Objects.equals(null, userMap.get("userName")) && !Objects.equals(null, userMap.get("password"))) {
            String userName = null;
            String password = null;
            Map<String, String> enmap = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            Map<String, String> lockTimes = redisTemplate.opsForHash().entries("t_sys_param:lockTime");
            Map<String, String> loginNum = redisTemplate.opsForHash().entries("t_sys_param:loginErrorNum");
            String isLogin = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isLogin", "content"));
            String isDecode = enmap.get("content");
            if ("true".equals(isDecode)) {
                userName = Demo.decrypt(userMap.get("userName"));
                password = Demo.decrypt(userMap.get("password"));
                // String verfiCode = userMap.get("verfiCode");
            } else {
                userName = userMap.get("userName");
                password = userMap.get("password");
            }
            String replayAvoid = userMap.get("replayAvoid");
            SysUserLogin sysUserLogin = sysUserDao.selectByUserNameAndL(userName);
            if (Objects.equals(null, sysUserLogin)) {
                MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                params.set("logType", "5");
                params.set("ip", request.getRemoteHost());
                params.set("title", "登录");
                params.set("state", 1);
                params.set("userId", "");
                params.set("userName", userName);
                params.set("requestOrigin", request.getRequestURL());
                params.set("requestPath", request.getRequestURI());
                params.set("requestMethod", request.getMethod());
                params.set("content", "用户名或密码错误登录失败");
                LogsAspect logsAspect = new LogsAspect();
                logsAspect.post(params);
                mapResult.put("info", ResultCodeEnum.CODE10101.getName());
                mapResult.put("code", ResultCodeEnum.CODE10101.getCode());
                return mapResult;
            }
            SysUserBackUp sysUserBackUp = SysUserBackUpDao.selectByVerfiCode(sysUserLogin.getUserId());
            if (!Objects.equals(null, sysUserLogin) && Demo.decryptDB(sysUserBackUp.getPassword()).equals(password) && userName.equals(sysUserLogin.getUserName())) {
                if ("true".equals(isLogin)) {
                    Map<String, String> appKeymap = redisTemplate.opsForHash().entries("user:" + sysUserLogin.getUserId());
                    if (appKeymap.size() != 0) {
                        Long expireTime = Long.valueOf(String.valueOf(redisTemplate.opsForHash().get("user:" + sysUserLogin.getUserId(), "expireTime")));
                        int logoutTime = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:logoutTime", "content")));
                        if (System.currentTimeMillis() - expireTime < 60000 * logoutTime) {
                            throw new BusinessException(500, "用户已登陆");
                        }
                    }

                }
                if (!sysUserLogin.getPassword().equals(sysUserBackUp.getPassword())) {
                    SysUser sysUsers = new SysUser();
                    sysUsers.setPassword(sysUserBackUp.getPassword());
                    sysUsers.setUserId(sysUserLogin.getUserId());
                    sysUserDao.update(sysUsers);
                }
                String userAvoid = Constant.userInfo.get(String.valueOf(sysUserLogin.getUserId()));
                if (!replayAvoid.equals(userAvoid)) {
                    Constant.userInfo.put(String.valueOf(sysUserLogin.getUserId()), replayAvoid);
                    String appKey = getRandomNickname(10);
                    sysUserLogin.setAppkey(appKey);
                    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                    params.set("logType", "5");
                    params.set("ip", request.getRemoteHost());
                    params.set("title", "登录");
                    params.set("state", 1);
                    params.set("userId", sysUserLogin.getUserId());
                    params.set("userName", userName);
                    params.set("requestOrigin", request.getRequestURL());
                    params.set("requestPath", request.getRequestURI());
                    params.set("requestMethod", request.getMethod());
                    params.set("content", "用户登录");
                    LogsAspect logsAspect = new LogsAspect();
                    logsAspect.post(params);
                    String userIds = String.valueOf(sysUserLogin.getUserId());
                    String keys = Constant.account_lock_time.replace("userAccountID", userIds);
                    //系统当前时间
                    String timeStr1 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(timeStr1);
                    int yxTime = DateTimeUtil.daysBetween(sysUserLogin.getUpdateTime(), date);
                    if (redisTemplate.hasKey(keys)) {//如果key存在
                        redisTemplate.delete(keys);
                    }
                    if (sysUserLogin.getState() == 2) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        //锁定时间
                        Calendar calendarOne = Calendar.getInstance();
                        calendarOne.setTime(sysUserLogin.getLockTime());
                        Long lockTime = DateTimeUtil.sencondsBetween(calendarOne, calendar);
                        if (lockTime < Integer.valueOf(lockTimes.get("content")) * 60) { //如果锁定时间小于1200S
                            mapResult.put("errorCount", "账户已被锁定！");
                            mapResult.put("code", ResultCodeEnum.CODE10102.getCode());
                            mapResult.put("info", ResultCodeEnum.CODE10102.getName());
                            return mapResult;
                        } else {
                            if (yxTime >= sysUserLogin.getInvalidTime()) {
                                mapResult.put("pwdExpirationTip", "当前密码长时间未更换，需更换");
                            }
                            List<String> sysRoleMenuList = sysRoleMenuDao.selectByRoleId(sysUserLogin.getRoleId());
                            mapResult.put("roleMenuList", sysRoleMenuList);
                            mapResult.put("sysUserLogin", sysUserLogin);
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
                            redisTemplate.opsForHash().putAll("appKey:" + userId + ":" + appKey, mapAppKey);
                            if ("true".equals(isLogin)) {
                                Map<String, Object> mapUser = new HashMap<>();
                                mapUser.put("expireTime", String.valueOf(System.currentTimeMillis()));
                                redisTemplate.opsForHash().putAll("user:" + userId, mapUser);
                            }
                        }

                    }
                    if (sysUserLogin.getState() == 0) {
                        mapResult.put("code", ResultCodeEnum.CODE10101.getCode());
                        mapResult.put("info", ResultCodeEnum.CODE10101.getName());
                        return mapResult;
                    }
                    if (sysUserLogin.getState() == 1) {
                        if (yxTime >= sysUserLogin.getInvalidTime()) {
                            mapResult.put("pwdExpirationTip", "当前密码长时间未更换，需更换");
                        }
                        List<String> sysRoleMenuList = sysRoleMenuDao.selectByRoleId(sysUserLogin.getRoleId());
                        mapResult.put("roleMenuList", sysRoleMenuList);
                        mapResult.put("sysUserLogin", sysUserLogin);
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
                        redisTemplate.opsForHash().putAll("appKey:" + userId + ":" + appKey, mapAppKey);
                        if ("true".equals(isLogin)) {
                            Map<String, Object> mapUser = new HashMap<>();
                            mapUser.put("expireTime", String.valueOf(System.currentTimeMillis()));
                            redisTemplate.opsForHash().putAll("user:" + userId, mapUser);
                        }
                    }
                } else {
                    throw new BusinessException(500, "用户已登陆");
                }
            } else {
                //登陆错误判断用户是否存在
                List<SysUser> sysUserList = sysUserDao.selectByUserNameTotal(userName);
                MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                params.set("logType", "5");
                params.set("ip", request.getRemoteHost());
                params.set("title", "登录");
                params.set("state", 1);
                if (sysUserList.size() == 0) {
                    params.set("userId", "");
                } else {
                    params.set("userId", sysUserList.get(0).getUserId());
                }
                params.set("userName", userName);
                params.set("requestOrigin", request.getRequestURL());
                params.set("requestPath", request.getRequestURI());
                params.set("requestMethod", request.getMethod());
                params.set("content", "用户名或密码错误登录失败");
                LogsAspect logsAspect = new LogsAspect();
                logsAspect.post(params);
                if (sysUserList.size() == 0) {
                    mapResult.put("info", ResultCodeEnum.CODE10101.getName());
                    mapResult.put("code", ResultCodeEnum.CODE10101.getCode());
//                        result.setData(mapResult);
                    return mapResult;
                }
                SysUser sysUser = sysUserList.get(0);
                String userId = String.valueOf(sysUser.getUserId());
                String key = Constant.account_lock_time.replace("userAccountID", userId);
                Integer num = (Integer) redisTemplate.opsForValue().get(key);
                if (num == null) { //第一次访问错误
                    redisTemplate.opsForValue().set(key, 1);
                    num = 1;
                } else if (num + 1 >= Integer.valueOf(loginNum.get("content"))) {//超过10次账户锁定
                    if (!userId.equals("10001")) { //admin用户不可锁定
                        SysUser user = new SysUser();
                        user.setState(2);
                        Date date = new Date();
                        user.setLockTime(date);
                        user.setUserId(sysUserList.get(0).getUserId());
                        sysUserDao.update(user);
                    }
                    redisTemplate.opsForValue().increment(key, 1);
                    num = num + 1;
                    mapResult.put("errorCount", "账户已被锁定！");
                    mapResult.put("code", ResultCodeEnum.CODE10102.getCode());
                    mapResult.put("info", ResultCodeEnum.CODE10102.getName());
                    return mapResult;
                } else {
                    redisTemplate.opsForValue().increment(key, 1);
                    num = num + 1;
                }
                mapResult.put("code", ResultCodeEnum.CODE10101.getCode());
                mapResult.put("info", ResultCodeEnum.CODE10101.getName());
                mapResult.put("errorCount", "已输入错误" + String.valueOf(num) + "次！");
                return mapResult;

            }
        } else {
            mapResult.put("code", ResultCodeEnum.CODE10103.getCode());
            mapResult.put("info", ResultCodeEnum.CODE10103.getName());
            return mapResult;
        }

        return mapResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUserSelect selectByPrimaryIds(Long userId) {
        return this.sysUserDao.selectByPrimaryIds(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser selectByPrimaryId(Long userId) {
        return this.sysUserDao.selectByPrimaryId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysUserSelect> selectByUserState(Integer state) {
        if (state.equals(-1)) {
            return this.sysUserDao.selectUsers();
        }
        return this.sysUserDao.selectByUserState(state);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysUserSelect> selectByUserName(String userName) {
        return this.sysUserDao.selectByUserName(userName);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysUser> selectByUserNameTotal(String userName) {
        return this.sysUserDao.selectByUserNameTotal(userName);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysUserSelect> select(Long userId, String userName, String password, String trueName, Integer userType, Integer sex, String eMail, String workNo, String faceId, String fingerId, String voiceId, Integer state, String userTitle, Long creatorId, String appkey, String imageUrl, Long roleId, Long orgId, Integer userStatus, Date createTime, Date updateTime, Integer invalidTime, Date lastLogin) {
        return sysUserDao.select(userId, userName, password, trueName, userType, sex, eMail, workNo, faceId, fingerId, voiceId, state, userTitle, creatorId, appkey, imageUrl, roleId, orgId, userStatus, createTime, updateTime, invalidTime, lastLogin);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysUserSelect> selectByPage(SysUser sysUser) {
        if (sysUser.getUserStatus() != null && sysUser.getUserStatus() == -1) {
            sysUser.setUserStatus(null);
        }
        return sysUserDao.selectByPage(sysUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysOrg selectRelationOrg(Long userId) {
        return this.sysUserDao.selectRelationOrg(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> selectRelationMenu(Long userId) {
        return this.sysUserDao.selectRelationMenu(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectRelationAuthor(Long userId) {
        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        List<Map<String, String>> list = this.sysUserDao.selectRelationAuthor(userId);
        for (Iterator<Map<String, String>> it = list.iterator(); it.hasNext(); ) {
            Map<String, String> areaInfoMap = it.next();
            if (Objects.equals(areaInfoMap.get("upId"), null) || Objects.equals(areaInfoMap.get("upId"), "")) {
                AreaInfo areaInfoCountry = new AreaInfo();
                areaInfoCountry.setLabel(areaInfoMap.get("label"));
                areaInfoCountry.setId(Long.valueOf(areaInfoMap.get("Id")));
                areaInfoCountry.setInfoType(areaInfoMap.get("infoType"));
                areaInfoCountry.setUpName(areaInfoMap.get("upName"));
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        diGui(areaInfoCountryList, list);
        return areaInfoCountryList;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUserLogin userLogin(String userName, String password) {
        return this.sysUserDao.selectByUserNameL(userName, password);
    }

    @Transactional(rollbackFor = Exception.class)
    public int userLogout(String userId, String token) {
        SysUser sysUserParams = new SysUser();
        sysUserParams.setLastLogin(new Date());
        long userIdLong = Long.valueOf(userId);
        sysUserParams.setUserId(userIdLong);
        redisTemplate.delete("appKey:" + userId + ":" + token);
        redisTemplate.delete("user:" + userId);
        return this.sysUserDao.update(sysUserParams);
    }

    @Transactional(rollbackFor = Exception.class)
    public int unlockUserAccount(Map<String, String> map) throws IOException {
        SysUser sysUserLocked = new SysUser();
        sysUserLocked.setState(1);
        Map<String, String> maps = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
        String isDecode = maps.get("content");
        if ("true".equals(isDecode)) {
            sysUserLocked.setUserId(Long.valueOf(Demo.decrypt(map.get("lockedUserId"))));
        } else {
            sysUserLocked.setUserId(Long.valueOf(map.get("lockedUserId")));
        }
        Date date = new Date();
        sysUserLocked.setUpdateTime(date);
        return this.sysUserDao.update(sysUserLocked);
    }

    @Transactional(rollbackFor = Exception.class)
    public int changePassword(Long userId, Map<String, String> map, String userName) throws IOException {
        Map linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("userName", userName);
        SysUser sysUser = new SysUser();
        Map<String, String> enmap = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
        String isDecode = enmap.get("content");
        if ("true".equals(isDecode)) {
            linkedHashMap.put("password", Demo.decrypt(map.get("newPassword")));
            sysUser.setPassword(Demo.encryption(Demo.decrypt(map.get("newPassword"))));
        } else {
            linkedHashMap.put("password", map.get("newPassword"));
            sysUser.setPassword(Demo.encryption(map.get("newPassword")));
        }
        sysUser.setUserId(userId);
        Date date = new Date();
        sysUser.setUpdateTime(date);
        SysUserBackUp sysUserBackUp = new SysUserBackUp();
        BeanUtils.copyProperties(sysUser, sysUserBackUp);
        sysUserBackUp.setVerfiCode(Demo.summary(linkedHashMap.toString()));
        SysUserBackUpDao.update(sysUserBackUp);
        return this.sysUserDao.update(sysUser);
    }

    private void diGui(List<AreaInfo> areaInfoList, List<Map<String, String>> listTree) {
        for (AreaInfo areaInfo : areaInfoList) {
            List<AreaInfo> childrenList = new ArrayList<>();
            for (Iterator<Map<String, String>> it = listTree.iterator(); it.hasNext(); ) {
                Map<String, String> areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.get("upId"))) {
                    AreaInfo areaInfoTem = new AreaInfo();
                    areaInfoTem.setUpId(Long.valueOf(areaInfoMap.get("upId")));
                    areaInfoTem.setId(Long.valueOf(areaInfoMap.get("Id")));
                    areaInfoTem.setLabel(areaInfoMap.get("label"));
                    areaInfoTem.setInfoType(areaInfoMap.get("infoType"));
                    areaInfoTem.setUpName(areaInfoMap.get("upName"));
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size() > 0) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertIntoRedis() {
        List<SysUser> list = this.sysUserDao.selectUser();
        for (SysUser item : list) {
            Map map = new HashMap();
            map.put("userId", item.getUserId());
            map.put("userName", item.getUserName());
            map.put("roleId", item.getRoleId());
            String str = "userInfo:" + item.getUserId();
            redisTemplate.opsForHash().putAll(str, Object2Map.toStringMap(map));
        }
        return 1;
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

