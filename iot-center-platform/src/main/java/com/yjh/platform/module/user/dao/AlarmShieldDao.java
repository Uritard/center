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

    AlarmShield selectByWarnContent(@Param(value = "warnContent")String warnContent,
                                        @Param(value = "shieldType")Integer shieldType);
    List<AlarmShield> select(@Param(value = "startTime")String startTime,
                             @Param(value = "endTime")String endTime);

    int add(AlarmShield alarmShield);

    int delete(@Param(value = "id")Long id);

    int update(AlarmShield alarmShield);
}
