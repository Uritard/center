package com.yjh.platform.module.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.user.entity.SysUserDevicePermissionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface SysUserDevicePermissionDao {
    int delete(@Param("userId")Long userId);

    int batchInsert(@Param("list")List<SysUserDevicePermissionDO> list);

    List<SysUserDevicePermissionDO> getSelectedByUserId(@Param("userId")Long userId);

}
