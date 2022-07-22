/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.SysKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/4/17
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface SysKeyDao {

    int insert(SysKey sysKey);

    int deleteByPrimaryId(@Param(value = "keyId") Long keyId);

    SysKey selectByPrimaryId(@Param(value = "keyId") Long keyId);

    List<SysKey> selectByUserId(@Param(value = "userId") Long userId);

    List<SysKey> selectAll();

    int insertUser(String uniqueUser);

    int countUser(String uniqueUser);
}
