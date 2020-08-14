package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.OrgInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.dao.SysOrgDao;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-24
*/
@Service
public class SysOrgService{

    @Autowired
    private SysOrgDao sysOrgDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysOrg sysOrg) {
        return this.sysOrgDao.insert(sysOrg);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long orgId) {
        return this.sysOrgDao.deleteByPrimaryId(orgId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysOrg sysOrg) {
        return this.sysOrgDao.update(sysOrg);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public SysOrg selectByPrimaryId(Long orgId) {
        return this.sysOrgDao.selectByPrimaryId(orgId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysOrg> select(Long orgId, String orgName, String orgCode, Long upId, Integer sort, Date createTime, Long creatorId, Integer orgLevel, String orgPath, String deptName) {
        List<SysOrg> sysOrgList = sysOrgDao.select(orgId, orgName, orgCode, upId, sort, createTime, creatorId, orgLevel, orgPath, deptName);
        return sysOrgList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysOrg> selectByPage(SysOrg sysOrg) {
        List<SysOrg> sysOrgList = sysOrgDao.selectByPage(sysOrg);
        return sysOrgList;
    }

    @Logs(title = "组织机构树查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<OrgInfo> selectOrgTree() {
        List<OrgInfo> listTree = this.sysOrgDao.selectOrgTree();
        List<OrgInfo> orgInfoStartList = new ArrayList<>();
        Integer level = 1;
        for (OrgInfo orgInfo:listTree) {
            if (Objects.equals(orgInfo.getUpId(), null) || Objects.equals(orgInfo.getUpId(), "")) {
                orgInfo.setLevel(level);
                orgInfoStartList.add(orgInfo);
            }
        }
        diGui(orgInfoStartList, listTree, level);
        return orgInfoStartList;
    }

    @Logs(title = "根据组织机构名称模糊查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<OrgInfo> selectOrgTreeByOrgName(String orgName) {
        List<OrgInfo> listTree = this.sysOrgDao.selectOrgTreeByOrgName(orgName);
        List<OrgInfo> orgInfoStartList = new ArrayList<>();
        Integer level = 1;
        for (OrgInfo orgInfo:listTree) {
            if (Objects.equals(orgInfo.getUpId(), null) || Objects.equals(orgInfo.getUpId(), "")) {
                orgInfo.setLevel(level);
                orgInfoStartList.add(orgInfo);
            }
        }
        diGui(orgInfoStartList, listTree, level);
        return orgInfoStartList;
    }

    private void diGui(List<OrgInfo> areaInfoList, List<OrgInfo> listTree, Integer level) {
        for(OrgInfo areaInfo:areaInfoList){
            List<OrgInfo> childrenList = new ArrayList<>();
            for (OrgInfo orgInfo:listTree) {
                if (Objects.equals(areaInfo.getId(), orgInfo.getUpId())) {
                    orgInfo.setLevel(areaInfo.getLevel()+1);
                    childrenList.add(orgInfo);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree, level);
            }
        }
    }

}

