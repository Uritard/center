package com.yjh.platform.module.user.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.yjh.platform.module.user.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysUserDao {
    int insert(SysUser sysUser);
    int deleteByPrimaryId(@Param(value = "userId") Long userId);
    int update(SysUser sysUser);
    SysUser selectByPrimaryId(@Param(value = "userId") Long userId);

    SysUserSelect selectByPrimaryIds(@Param(value = "userId") Long userId);

    List<SysUserSelect> selectByUserState(@Param(value= "state") Integer state);

    List<SysUser> selectUser();

    List<SysUserSelect> selectUsers();

    List<SysUserSelect> selectByUserName(@Param(value = "userName")String userName);

    List<SysUser> selectByUserNameTotal(@Param(value = "userName")String userName);

    SysUserLogin selectByUserNameL(@Param(value = "userName") String userName, @Param(value = "password") String password);

    List<SysUserSelect> select(@Param(value = "userId") Long userId,
                         @Param(value = "userName") String userName,
                         @Param(value = "password") String password,
                         @Param(value = "trueName") String trueName,
                         @Param(value = "userType") Integer userType,
                         @Param(value = "sex") Integer sex,
                         @Param(value = "eMail") String eMail,
                         @Param(value = "workNo") String workNo,
                         @Param(value = "faceId") String faceId,
                         @Param(value = "fingerId") String fingerId,
                         @Param(value = "voiceId") String voiceId,
                         @Param(value = "state") Integer state,
                         @Param(value = "userTitle") String userTitle,
                         @Param(value = "creatorId") Long creatorId,
                         @Param(value = "appkey") String appkey,
                         @Param(value = "imageUrl") String imageUrl,
                         @Param(value = "roleId") Long roleId,
                         @Param(value = "orgId") Long orgId,
                         @Param(value = "userStatus") Integer userStatus,
                         @Param(value = "createTime") Date createTime,
                         @Param(value = "updateTime") Date updateTime,
                         @Param(value = "invalidTime") Integer invalidTime,
                         @Param(value = "lastLogin") Date lastLogin);
    List<Map<String, String>> selectByPage(SysUser sysUser);

    SysOrg selectRelationOrg(@Param(value = "userId") Long userId);
    List<String> selectRelationMenu(@Param(value = "userId") Long userId);
    List<Map<String, String>> selectRelationAuthor(@Param(value = "userId") Long userId);

    SysUserLogin selectByUserNameAndL(@Param(value = "userName") String userName);
}
