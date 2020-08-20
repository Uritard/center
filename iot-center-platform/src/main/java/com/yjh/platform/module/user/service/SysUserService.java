package com.yjh.platform.module.user.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.dao.SysUserDao;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-23
*/
@Service
public class SysUserService{

    @Autowired
    private SysUserDao sysUserDao;

    @Logs(title = "插入新用户", code = "sysUser")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysUser sysUser) {
        Date date = new Date();
        sysUser.setCreateTime(date);
        sysUser.setUpdateTime(date);
        return this.sysUserDao.insert(sysUser);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long userId) {
        return this.sysUserDao.deleteByPrimaryId(userId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysUser sysUser) {
        Date date = new Date();
        sysUser.setUpdateTime(date);
        return this.sysUserDao.update(sysUser);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public SysUser selectByPrimaryId(Long userId) {
        return this.sysUserDao.selectByPrimaryId(userId);
    }

    @Logs(title = "用户状态查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysUser> selectByUserState(Integer state) {
        return this.sysUserDao.selectByUserState(state);
    }

    @Logs(title = "用户名查询", code ="module")
    @Transactional(rollbackFor =Exception.class )
    public  List<SysUser> selectByUserName(String userName){
        return this.sysUserDao.selectByUserName(userName);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysUser> select(Long userId, String userName, String password, String trueName, Integer userType, Integer sex, String eMail, String mobilePhone, String workNo, String faceId, String fingerId, String voiceId, Integer state, String userTitle, Long creatorId, String appkey, String imageUrl, Long roleId, Long orgId, Integer userStatus, Date createTime, Date updateTime, Date invalidTime, Date lastLogin) {
        return sysUserDao.select(userId, userName, password, trueName, userType, sex, eMail, mobilePhone, workNo, faceId, fingerId, voiceId, state, userTitle, creatorId, appkey, imageUrl, roleId, orgId, userStatus, createTime, updateTime, invalidTime, lastLogin);
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, String>> selectByPage(SysUser sysUser) {
        if (sysUser.getUserStatus() != null && sysUser.getUserStatus()==-1) {
            sysUser.setUserStatus(null);
        }
        return sysUserDao.selectByPage(sysUser);
    }

    @Logs(title = "根据用户ID查询关联的组织结构信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public SysOrg selectRelationOrg(Long userId) {
        return this.sysUserDao.selectRelationOrg(userId);
    }

    @Logs(title = "根据用户ID查询关联菜单权限", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectRelationMenu(Long userId) {
        return this.sysUserDao.selectRelationMenu(userId);
    }

    @Logs(title = "根据用户ID查询关联区域设备权限", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectRelationAuthor(Long userId) {
        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        List<Map<String, String>> list = this.sysUserDao.selectRelationAuthor(userId);
        for(Iterator<Map<String, String>> it = list.iterator();it.hasNext();){
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

    @Logs(title = "用户登录", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public SysUser userLogin(String userName, String password) {
        SysUser sysUser = this.sysUserDao. selectByUserNameL(userName);
        if(sysUser==null) {
            throw new BusinessException("用户名不存在");
        }else {
            if (!sysUser.getPassword().equals(password)) {
                throw new BusinessException("用户密码错误");
            } else {
                Date date = new Date();
                String appKey = String.valueOf(DateTimeUtil.getSecondTimestamp(date));
                sysUser.setAppkey(appKey);
            }
        }
        return sysUser;
    }

    @Logs(title = "用户登出", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int userLogout(String userId) {
        SysUser sysUserParams = new SysUser();
        sysUserParams.setLastLogin(new Date());
        long userIdLong = Long.valueOf(userId);
        sysUserParams.setUserId(userIdLong);
        return this.sysUserDao.update(sysUserParams);
    }

    private void diGui(List<AreaInfo> areaInfoList, List<Map<String, String>> listTree) {
        for(AreaInfo areaInfo : areaInfoList){
            List<AreaInfo> childrenList = new ArrayList<>();
            for(Iterator<Map<String, String>> it = listTree.iterator();it.hasNext();){
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
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree);
            }
        }
    }

}

