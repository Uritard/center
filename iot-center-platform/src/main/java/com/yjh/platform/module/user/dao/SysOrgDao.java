package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.yjh.platform.module.user.entity.OrgInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-24
 */
@Repository
public interface SysOrgDao {

    int insert(SysOrg sysOrg);
    int deleteByPrimaryId(@Param(value = "orgId") Long orgId);
    int update(SysOrg sysOrg);
    SysOrg selectByPrimaryId(@Param(value = "orgId") Long orgId);
    List<SysOrg> select(@Param(value = "orgId") Long orgId,
                                @Param(value = "orgName") String orgName,
                                @Param(value = "orgCode") String orgCode,
                                @Param(value = "upId") Long upId,
                                @Param(value = "sort") Integer sort,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "creatorId") Long creatorId,
                                @Param(value = "orgLevel") Integer orgLevel,
                                @Param(value = "orgPath") String orgPath,
                                @Param(value = "deptName") String deptName);
    List<SysOrg> selectByPage(SysOrg sysOrg);

    List<OrgInfo> selectOrgTree();
    List<OrgInfo> selectOrgTreeByOrgName(@Param(value = "orgName") String orgName);
    List<Long> selectDownId(@Param(value = "orgId") Long orgId);
    int batchDelete(@Param(value = "list") List<Long> list);
}
