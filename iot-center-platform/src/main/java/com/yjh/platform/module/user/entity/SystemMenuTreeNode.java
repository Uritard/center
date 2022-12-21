package com.yjh.platform.module.user.entity;

import lombok.Data;

import java.util.List;

@Data
public class SystemMenuTreeNode {
    private Long menuId;
    private Long upId;
    private Long roleId;
    private String menuCode;
    private String menuName;
    private String elementCode;
    private List<SystemMenuTreeNode> childrenList;
}
