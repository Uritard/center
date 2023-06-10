package com.yjh.platform.module.user.dao;


import com.yjh.platform.module.user.entity.AlarmShield;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 告警屏蔽配置 Mapper 接口
 * </p>
 *
 * @author lqh
 * @since 2023-06-05
 */
@Repository
public interface AlarmShieldDao {

    AlarmShield selectByWarnCount(@Param(value = "warnCount")String warnCount,
                                        @Param(value = "shieldType")Integer shieldType);
    List<AlarmShield> select();

    int add(AlarmShield alarmShield);

    int delete(@Param(value = "id")Long id);

    int update(AlarmShield alarmShield);
}
