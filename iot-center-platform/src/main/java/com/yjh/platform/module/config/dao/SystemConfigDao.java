package com.yjh.platform.module.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.config.entity.ConfigTreeNode;
import com.yjh.platform.module.config.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 系统配置表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-04-12
 */
@Repository
public interface SystemConfigDao {

    List<ConfigTreeNode> selectConfigInfo();

    int update(SystemConfig systemConfig);
    int batchUpdate(List<SystemConfig> list);

    List<SystemConfig> selectAll();

}
