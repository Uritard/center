package com.yjh.platform.module.user.dao;


import com.yjh.platform.module.user.entity.WarnSub;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 告警订阅信息 Mapper 接口
 * </p>
 *
 * @author lqh
 * @since 2022-08-26
 */
@Repository
public interface WarnSubDao {

    WarnSub selectByUserId(@Param(value = "userId") Long userId);

    int deleteByUserId(@Param(value = "userId") Long userId);

    int add(WarnSub warnSub);

    Integer selectAlarmNote(@Param(value = "warnId") Long warnId);

}
