package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.device.entity.TCruiseNonhomologousPointInstance;
import com.yjh.platform.module.task.entity.TCruiseTriphaseRule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lqh
 * @since 2023-03-18
 */
@Repository
public interface TCruiseTriphaseRuleDao {

    int add(TCruiseTriphaseRule tCruiseTriphaseRule);
    int deleteByPrimaryId(@Param(value = "triphaseId") Long triphaseId);
    int update(TCruiseTriphaseRule tCruiseTriphaseRule);
    TCruiseTriphaseRule selectByPrimaryId(@Param(value = "triphaseId") Long triphaseId);
    List<TCruiseTriphaseRule> select(@Param(value = "triphaseId") Long triphaseId,
                                @Param(value = "triphaseName") String triphaseName,
                                @Param(value = "deviceMeteId") Long deviceMeteId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "customId") String customId,
                                @Param(value = "instanceOneId") Long instanceOneId,
                                @Param(value = "instanceOneName") String instanceOneName,
                                @Param(value = "oneCruiseDeviceName") String oneCruiseDeviceName,
                                @Param(value = "instanceTwoId") Long instanceTwoId,
                                @Param(value = "instanceTwoName") String instanceTwoName,
                                @Param(value = "twoCruiseDeviceName") String twoCruiseDeviceName,
                                @Param(value = "instanceTriId") Long instanceTriId,
                                @Param(value = "instanceTriName") String instanceTriName,
                                @Param(value = "triCruiseDeviceName") String triCruiseDeviceName,
                                @Param(value = "identifyType") Integer identifyType,
                                @Param(value = "identifySonType") Integer identifySonType,
                                @Param(value = "triphaseType") Integer triphaseType,
                                @Param(value = "warnThreshold") String warnThreshold,
                                @Param(value = "warnLevel") Integer warnLevel);
    List<TCruiseNonhomologousPointInstance> selectByPage(@Param(value = "triphaseName") String triphaseName);

    int batchAdd(List<TCruiseTriphaseRule> list);
    int batchDelete(List<String> list);

    int checkNonhomologousPointInstanceExist(TCruiseTriphaseRule tCruiseTriphaseRule);
}
