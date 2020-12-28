package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.OrgInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.dao.SysOrgDao;

import java.util.*;
import java.util.stream.Collectors;

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

    @Logs(title = "插入", code = "module", content = "新增组织机构")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysOrg sysOrg) {
        return this.sysOrgDao.insert(sysOrg);
    }

    public boolean judgeOrgCode (String orgCode) {
        boolean flag = false;
        List<SysOrg> sysOrgList = this.sysOrgDao.select(null, null, orgCode, null, null, null, null, null, null, null);
        if (sysOrgList.size()>0) {
            flag=true;
        }
        return flag;
    }

    @Logs(title = "删除", code = "module", content = "删除组织机构")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long orgId) {
        List<Long> list = sysOrgDao.selectDownId(orgId);
        if(list != null && list.size() > 0){
           return sysOrgDao.batchDelete(list);
        }else {
            return this.sysOrgDao.deleteByPrimaryId(orgId);
        }
    }

    @Logs(title = "更新", code = "module", content = "更新组织机构")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysOrg sysOrg) {
        return this.sysOrgDao.update(sysOrg);
    }


    @Logs(title = "主键查询", code = "module", content = "新增组织机构")
    @Transactional(rollbackFor = Exception.class)
    public SysOrg selectByPrimaryId(Long orgId) {
        return this.sysOrgDao.selectByPrimaryId(orgId);
    }

    @Logs(title = "查询", code = "module", content = "查询组织机构")
    @Transactional(rollbackFor = Exception.class)
    public List<SysOrg> select(Long orgId, String orgName, String orgCode, Long upId, Integer sort, Date createTime, Long creatorId, Integer orgLevel, String orgPath, String deptName) {
        List<SysOrg> sysOrgList = sysOrgDao.select(orgId, orgName, orgCode, upId, sort, createTime, creatorId, orgLevel, orgPath, deptName);
        return sysOrgList;
    }

    @Logs(title = "分页查询", code = "module", content = "分页查询组织机构")
    @Transactional(rollbackFor = Exception.class)
    public List<SysOrg> selectByPage(SysOrg sysOrg) {
        List<SysOrg> sysOrgList = sysOrgDao.selectByPage(sysOrg);
        return sysOrgList;
    }

    @Logs(title = "组织机构树查询", code = "module", content = "查询组织机构树")
    @Transactional(rollbackFor = Exception.class)
    public List<OrgInfo> selectOrgTree() {
        List<OrgInfo> listTree = this.sysOrgDao.selectOrgTree();
        List<OrgInfo> orgInfoStartList = new ArrayList<>();
        Integer level = 1;
        for (OrgInfo orgInfo:listTree) {
            if (orgInfo.getUpId()==null || orgInfo.getUpId()==-1) {
                orgInfo.setLevel(level);
                orgInfoStartList.add(orgInfo);
            }
        }
        diGui(orgInfoStartList, listTree, level);
        return orgInfoStartList;
    }

    @Logs(title = "根据组织机构名称查询", code = "module", content = "根据组织机构名称模糊查询组织机构树")
    @Transactional(rollbackFor = Exception.class)
    public List<OrgInfo> selectOrgTreeByName(String orgName) {
        List<OrgInfo> listTree = new ArrayList<>();
        List<OrgInfo> listTreeAll = this.sysOrgDao.selectOrgTree();
        if (Objects.equals(null, orgName) || orgName.equals("")) {
            List<OrgInfo> orgInfoStartList = new ArrayList<>();
            Integer level = 1;
            for (OrgInfo orgInfo:listTreeAll) {
                if (orgInfo.getUpId()==null || orgInfo.getUpId()==-1) {
                    orgInfo.setLevel(level);
                    orgInfoStartList.add(orgInfo);
                }
            }
            diGui(orgInfoStartList, listTreeAll, level);
            return orgInfoStartList;
        }

        List<OrgInfo> listTreeByName = new ArrayList<>();
        listTreeByName = this.sysOrgDao.selectOrgTreeByOrgName(orgName);
        if (listTreeByName.size()>0) {
            for (OrgInfo orgInfo : listTreeByName) {
                listTree.add(orgInfo);
                if (orgInfo.getUpId() != null && orgInfo.getUpId() != -1) {
                    Long orgInfoUpId = orgInfo.getUpId();
                    System.out.println("orgInfoUpId: "+orgInfoUpId);
                    for (OrgInfo orgInfoAll : listTreeAll) {
                        if (Objects.equals(orgInfoUpId, orgInfoAll.getId())) {
                            listTree.add(orgInfoAll);
                            diGuiMoHu(orgInfoAll, listTreeAll, listTree);
                        }
                    }
                }
            }
        }
        listTree = listTree.stream().distinct().collect(Collectors.toList());
        System.out.println("listTree: "+listTree);
        List<OrgInfo> orgInfoStartList = new ArrayList<>();
        Integer level = 1;
        for (OrgInfo orgInfo:listTree) {
            if (orgInfo.getUpId()==null || orgInfo.getUpId()==-1) {
                orgInfo.setLevel(level);
                orgInfoStartList.add(orgInfo);
            }
        }
        diGui(orgInfoStartList, listTree, level);
        return orgInfoStartList;
    }

    private void diGuiMoHu(OrgInfo orgInfoAll, List<OrgInfo> listTreeAll, List<OrgInfo> listTree) {
        if (orgInfoAll.getUpId() != null && orgInfoAll.getUpId() != -1) {
            Long orgInfoUpId = orgInfoAll.getUpId();
            for (OrgInfo orgInfo : listTreeAll) {
                if (Objects.equals(orgInfoUpId, orgInfo.getId())) {
                    listTree.add(orgInfo);
                    diGuiMoHu(orgInfo, listTreeAll, listTree);
                }
            }
        }
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

