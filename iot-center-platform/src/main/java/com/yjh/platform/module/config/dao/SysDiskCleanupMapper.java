/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.config.entity.SysDiskCleanup;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * <p>
 * 磁盘清理记录 Mapper 接口
 * </p>
 *
 * @author Chenfei
 * @since 2023-06-30
 */
public interface SysDiskCleanupMapper extends BaseMapper<SysDiskCleanup> {

    /**
     * 根据时间删除历史任务记录
     * @param expireDate 时间
     * @return 返回
     */
    int deleteTask(@Param("expireDate") Date expireDate);

    /**
     * 根据时间删除任务结果记录
     * @param expireDate 时间
     * @return 返回
     */
    int deleteTaskResult(@Param("expireDate") Date expireDate);

    /**
     * 根据时间删除日志记录
     * @param expireDate 时间
     * @return 返回
     */
    int deleteLogs(@Param("expireDate") Date expireDate);
}
