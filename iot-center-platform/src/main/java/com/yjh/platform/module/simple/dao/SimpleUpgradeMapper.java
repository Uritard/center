package com.yjh.platform.module.simple.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.simple.entity.PatrolDeviceVersion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <功能描述> 表t_patrol_device_version的数据库操作Mapper
 *
 * @author shaobinfen
 * @date 2025/6/25
 * @since [产品/模块版本](可选)
 */
public interface SimpleUpgradeMapper extends BaseMapper<PatrolDeviceVersion> {

    int insertSelective(PatrolDeviceVersion patrolDeviceVersion);

    PatrolDeviceVersion selectByPrimaryId(@Param("id") Long id);

    List<PatrolDeviceVersion> selectByPage(PatrolDeviceVersion patrolDeviceVersion);

    int deleteByPrimaryId(@Param(value = "id") Long id);

    int batchDelete(@Param(value = "list") List<String> list);
}
