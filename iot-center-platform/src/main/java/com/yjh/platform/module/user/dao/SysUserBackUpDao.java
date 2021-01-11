package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.SysUserBackUp;
import com.yjh.platform.module.user.entity.SysUserLogin;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysUserBackUpDao {
        int insert(SysUserBackUp sysUserBackUp);

        int update(SysUserBackUp sysUserBackUp);

}
