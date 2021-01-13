package com.yjh.accesstcp.commons.security;

import java.util.Map;

public interface UserInterface {
    //用户基础信息
    String USER_BASE = "sys_user_base:";
    //用户角色
    String USER_ROLE = "sys_user_role:";
    //用户权限
    String USER_AUTH = "sys_user_auth:";
    String USER_ID = "userId";

    /**
     * 获取用户ID
     *
     * @return
     */
    long getUserId();

    /**
     * 获取用户权限
     *
     * @param code
     * @return
     */
    Object getAuthByCode(String code);
    
    /**
     * 获取用户信息
     *
     * @return
     */
    Map getUserMap();

    /**
     * 获取用户角色
     *
     * @return
     */
    String getUserRoles();

    /**
     * 是否是超级管理员
     *
     * @return
     */
    boolean isSysAdmin();
}
