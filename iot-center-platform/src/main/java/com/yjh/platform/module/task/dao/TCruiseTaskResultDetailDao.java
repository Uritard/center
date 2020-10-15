package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCruiseTaskResultDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface TCruiseTaskResultDetailDao {

    int insert(TCruiseTaskResultDetail tCruiseTaskResultDetail);
    int deleteByPrimaryId(@Param(value = "cruiseResultId") String cruiseResultId);
    int update(TCruiseTaskResultDetail tCruiseTaskResultDetail);
    TCruiseTaskResultDetail selectByPrimaryId(@Param(value = "cruiseResultId") String cruiseResultId);
    List<TCruiseTaskResultDetail> select(@Param(value = "cruiseResultId") String cruiseResultId,
                                @Param(value = "taskResultId") String taskResultId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "cruiseTime") Date cruiseTime,
                                @Param(value = "endTime") Date endTime,
                                @Param(value = "cruiseStatus") Integer cruiseStatus,
                                @Param(value = "remark") String remark);
    List<TCruiseTaskResultDetail> selectByPage(TCruiseTaskResultDetail tCruiseTaskResultDetail);

    int batchInsert(List<TCruiseTaskResultDetail> list);
}
