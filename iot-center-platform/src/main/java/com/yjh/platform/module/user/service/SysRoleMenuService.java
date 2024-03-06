package com.yjh.platform.module.user.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.dao.SysRoleMenuDao;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysRoleMenu;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.SystemMenuTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author tt
* @since 2020-07-23
*/
@Service
public class SysRoleMenuService{

    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysRoleService sysRoleService;


    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleMenu sysRoleMenu) {
        return this.sysRoleMenuDao.insert(sysRoleMenu);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long rpId) {
        return this.sysRoleMenuDao.deleteByPrimaryId(rpId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByRoleId(Long roleId) {
        return this.sysRoleMenuDao.deleteByRoleId(roleId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleMenu sysRoleMenu) {
        return this.sysRoleMenuDao.update(sysRoleMenu);
    }


    @Transactional(rollbackFor = Exception.class)
    public SysRoleMenu selectByPrimaryId(Long rpId) {
        return this.sysRoleMenuDao.selectByPrimaryId(rpId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleMenu> select(Long rpId, String menuCode, Integer sort, String elementCode, Long roleId) {
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuDao.select(rpId, menuCode, sort, elementCode, roleId);
        return sysRoleMenuList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleMenu> selectByPage(SysRoleMenu sysRoleMenu) {
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuDao.selectByPage(sysRoleMenu);
        return sysRoleMenuList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleMenu> list) {
        return this.sysRoleMenuDao.batchInsert(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<SystemMenuTreeNode> selectRoleMenuTree() {
        return this.sysRoleMenuDao.selectTreeMenu(-1L);
    }

    public Map<String, Object> roleRelationMenu(Long userId) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);
        if (sysUser == null) {
            throw new BusinessException(ResultCodeEnum.PARAMERROR, "用户不存在");
        }
        Long roleId = sysUser.getRoleId();
        Map<String, Object> roleMenuMap = new HashMap<>(4);
        List<SystemMenuTreeNode> roleMenuTree = sysRoleMenuDao.selectRoleMenuTree(-1L, roleId);
        roleMenuMap.put("roleMenuTree", roleMenuTree);

        List<String> roleMenus = sysRoleMenuDao.selectByUserId(userId, null);
        if (CollectionUtils.isEmpty(roleMenus)) {
            roleMenus = sysRoleMenuDao.selectByRoleId(roleId, null);
        }

        String[] codes = null;
        if (SysUserService.MANAGER_BUILT_IN == userId) {
            codes = new String[]{"0600", "0601", "0602"};
            CollectionUtils.addAll(roleMenus, codes);
        }
        dischangeMenu(roleMenuTree, roleId, SetUtils.hashSet(codes));

        roleMenuMap.put("roleMenus", roleMenus);

        return roleMenuMap;
    }

    /**
     * 指定用户对应菜单不可修改
     */
    public void dischangeMenu(List<SystemMenuTreeNode> roleMenuTree, Long roleId, Set<String> dischangeSet) {
        for (SystemMenuTreeNode menu : roleMenuTree) {
            boolean disabled = (CollectionUtils.isNotEmpty(dischangeSet) && dischangeSet.contains(menu.getMenuCode())) || !roleId.equals(
                menu.getBelongRole());
            if (disabled) {
                menu.setSysState(0);
            }
            if (CollectionUtils.isNotEmpty(menu.getChildrenList())) {
                dischangeMenu(menu.getChildrenList(), roleId, dischangeSet);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateRoleMenuRight(Map<String, Object> req) {
        Long userId = MapUtils.getLong(req, "userId");
        List<String> listStringChecked = (List<String>)req.get("checked");
        List<String> listStringHalfChecked = (List<String>)req.get("halfChecked");
        SysUser sysUser = sysUserDao.selectByPrimaryId(userId);
        if (sysUser == null) {
            throw new BusinessException(ResultCodeEnum.PARAMERROR, "用户不存在");
        }
        Long roleId = sysUser.getRoleId();

        //判断角色互斥
        sysRoleService.menuOverlayCheck(roleId, listStringChecked, listStringHalfChecked);

        return sysRoleService.batchInsertMenu(listStringChecked, listStringHalfChecked, roleId, userId);
    }


}

