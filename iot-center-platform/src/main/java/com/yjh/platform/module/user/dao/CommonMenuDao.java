package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.CommonMenu;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 常用功能配置表 Mapper 接口
 * </p>
 *
 * @author lqh
 * @since 2023-06-05
 */
@Repository
public interface CommonMenuDao {

    int add(CommonMenu commonMenu);
    int batchAdd(List<CommonMenu> list);

    List<CommonMenu> selectByUserId(@Param(value = "userId") Long userId);

    int deleteByUserId(@Param(value = "userId") Long userId);

}
